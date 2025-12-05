package com.demo.sms.card.controller;

import com.demo.sms.card.dto.BusinessCardDto;
import com.demo.sms.card.dto.UploadResult;
import com.demo.sms.card.entity.BusinessCard;
import com.demo.sms.card.entity.BusinessCard.Category;
import com.demo.sms.card.entity.User;
import com.demo.sms.card.service.BusinessCardService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/cards")
@RequiredArgsConstructor
public class BusinessCardController {

    private final BusinessCardService businessCardService;

    @ModelAttribute
    public void addUserToModel(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        model.addAttribute("user", user);
    }

    @GetMapping
    public String redirectToList() {
        return "redirect:/cards/list";
    }

    @GetMapping("/list")
    public String listCardsWithPaging(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "all") String searchField,
            @RequestParam(required = false, defaultValue = "recent") String sortBy,
            @RequestParam(required = false, defaultValue = "1") int page,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("user");

        int pageSize = 20;

        // 전체 검색 결과
        List<BusinessCard> allCards = businessCardService.searchCards(user, category, keyword, searchField, sortBy);

        // 총 페이지 수 계산
        int totalCards = allCards.size();
        int totalPages = (int) Math.ceil((double) totalCards / pageSize);

        // 페이지 유효성 검사
        if (page < 1) page = 1;
        if (page > totalPages && totalPages > 0) page = totalPages;

        // 현재 페이지 데이터
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, totalCards);
        List<BusinessCard> cards = totalCards > 0 ? allCards.subList(start, end) : new ArrayList<>();

        // 페이지 번호 목록 생성 (최대 10개씩)
        int startPage = ((page - 1) / 10) * 10 + 1;
        int endPage = Math.min(startPage + 9, totalPages);

        model.addAttribute("cards", cards);
        model.addAttribute("categories", Category.values());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("keyword", keyword);
        model.addAttribute("searchField", searchField);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("categoryStats", businessCardService.getCategoryStatistics(user));
        model.addAttribute("recentCards", businessCardService.getRecentCards(user, 5));
        model.addAttribute("totalCount", businessCardService.getTotalCount(user));
        model.addAttribute("searchCount", totalCards);

        // 페이징 정보
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("hasPrev", page > 1);
        model.addAttribute("hasNext", page < totalPages);

        // 검색 컨텍스트 저장
        session.setAttribute("lastSearchCategory", category);
        session.setAttribute("lastSearchKeyword", keyword);
        session.setAttribute("lastSearchField", searchField);
        session.setAttribute("lastSearchSortBy", sortBy);

        return "cards/list";
    }

    @GetMapping("/{id}")
    public String viewCard(@PathVariable Long id,
                           HttpSession session,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");

        try {
            BusinessCard card = businessCardService.getCard(id, user);

            // 이전/다음 명함
            Optional<BusinessCard> previousCard = businessCardService.getPreviousCard(id, user);
            Optional<BusinessCard> nextCard = businessCardService.getNextCard(id, user);

            model.addAttribute("card", card);
            model.addAttribute("previousCard", previousCard.orElse(null));
            model.addAttribute("nextCard", nextCard.orElse(null));
            model.addAttribute("categories", Category.values());

            // 검색 컨텍스트 전달
            model.addAttribute("lastSearchCategory", session.getAttribute("lastSearchCategory"));
            model.addAttribute("lastSearchKeyword", session.getAttribute("lastSearchKeyword"));
            model.addAttribute("lastSearchField", session.getAttribute("lastSearchField"));
            model.addAttribute("lastSearchSortBy", session.getAttribute("lastSearchSortBy"));

            return "cards/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cards/list";
        }
    }

    @GetMapping("/duplicates")
    public String duplicatesPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        Map<String, List<BusinessCard>> duplicates = businessCardService.findDuplicates(user);

        model.addAttribute("duplicates", duplicates);
        model.addAttribute("totalDuplicates", duplicates.values().stream().mapToInt(List::size).sum());

        return "cards/duplicates";
    }

    @PostMapping("/duplicates/clean")
    public String cleanDuplicates(@RequestParam String strategy,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");

        try {
            int deletedCount = businessCardService.cleanDuplicates(user, strategy);
            redirectAttributes.addFlashAttribute("success",
                    String.format("중복 제거 완료: %d개의 명함이 삭제되었습니다.", deletedCount));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "중복 제거 중 오류가 발생했습니다: " + e.getMessage());
        }

        return "redirect:/cards/list";
    }

    @PostMapping("/duplicates/delete/{id}")
    public String deleteDuplicateCard(@PathVariable Long id,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");

        try {
            businessCardService.deleteCard(id, user);
            redirectAttributes.addFlashAttribute("success", "명함이 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "명함 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }

        return "redirect:/cards/duplicates";
    }

    @GetMapping("/add")
    public String addCardPage(Model model) {
        model.addAttribute("categories", Category.values());
        return "cards/add";
    }

    @PostMapping("/add")
    public String addCard(@ModelAttribute BusinessCardDto dto,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");

        try {
            businessCardService.saveCard(dto, user);
            redirectAttributes.addFlashAttribute("success", "명함이 성공적으로 등록되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "명함 등록 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/cards/add";
        }

        return "redirect:/cards/list";
    }

    @GetMapping("/edit/{id}")
    public String editCardPage(@PathVariable Long id,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");

        try {
            BusinessCard card = businessCardService.getCard(id, user);
            model.addAttribute("card", card);
            model.addAttribute("categories", Category.values());
            return "cards/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cards/list";
        }
    }

    @PostMapping("/edit/{id}")
    public String editCard(@PathVariable Long id,
                           @ModelAttribute BusinessCardDto dto,
                           @RequestParam(required = false, defaultValue = "false") boolean fromView,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");

        try {
            businessCardService.updateCard(id, dto, user);
            redirectAttributes.addFlashAttribute("success", "명함이 성공적으로 수정되었습니다.");

            if (fromView) {
                return "redirect:/cards/" + id;
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "명함 수정 중 오류가 발생했습니다: " + e.getMessage());
        }

        return "redirect:/cards/list";
    }

    @PostMapping("/delete/{id}")
    public String deleteCard(@PathVariable Long id,
                             @RequestParam(required = false, defaultValue = "false") boolean fromView,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");

        try {
            businessCardService.deleteCard(id, user);
            redirectAttributes.addFlashAttribute("success", "명함이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "명함 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }

        // 상세 페이지에서 삭제한 경우 검색 결과로 돌아가기
        if (fromView) {
            Category category = (Category) session.getAttribute("lastSearchCategory");
            String keyword = (String) session.getAttribute("lastSearchKeyword");
            String searchField = (String) session.getAttribute("lastSearchField");
            String sortBy = (String) session.getAttribute("lastSearchSortBy");

            String url = "/cards/list?";
            if (category != null) url += "category=" + category + "&";
            if (keyword != null) url += "keyword=" + keyword + "&";
            if (searchField != null) url += "searchField=" + searchField + "&";
            if (sortBy != null) url += "sortBy=" + sortBy;

            return "redirect:" + url;
        }

        return "redirect:/cards/list";
    }

    @GetMapping("/upload")
    public String uploadPage(Model model) {
        model.addAttribute("categories", Category.values());
        return "cards/upload";
    }

    @PostMapping("/upload")
    public String uploadExcel(@RequestParam("file") MultipartFile file,
                              @RequestParam("category") Category category,
                              @RequestParam(value = "duplicateAction", defaultValue = "skip") String duplicateAction,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");

        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "파일을 선택해주세요.");
                return "redirect:/cards/upload";
            }

            if (!file.getOriginalFilename().endsWith(".xlsx")) {
                redirectAttributes.addFlashAttribute("error", "엑셀 파일(.xlsx)만 업로드 가능합니다.");
                return "redirect:/cards/upload";
            }

            UploadResult result = businessCardService.uploadExcel(file, category, user, duplicateAction);

            // 세션에 결과 저장
            session.setAttribute("uploadResult", result);
            session.setAttribute("uploadFileName", file.getOriginalFilename());
            session.setAttribute("uploadCategory", category.getDisplayName());

            return "redirect:/cards/upload/result";

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "파일 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/cards/upload";
        }
    }

    @GetMapping("/upload/result")
    public String uploadResult(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        UploadResult result = (UploadResult) session.getAttribute("uploadResult");
        String fileName = (String) session.getAttribute("uploadFileName");
        String categoryName = (String) session.getAttribute("uploadCategory");

        if (result == null) {
            redirectAttributes.addFlashAttribute("error", "업로드 결과를 찾을 수 없습니다.");
            return "redirect:/cards/upload";
        }

        model.addAttribute("result", result);
        model.addAttribute("fileName", fileName);
        model.addAttribute("categoryName", categoryName);

        // 세션에서 제거
        session.removeAttribute("uploadResult");
        session.removeAttribute("uploadFileName");
        session.removeAttribute("uploadCategory");

        return "cards/upload-result";
    }

    //엑셀 다운로드
    @GetMapping("/download")
    public ResponseEntity<ByteArrayResource> downloadExcel(@RequestParam(required = false) Category category,
                                                           @RequestParam(required = false) String keyword,
                                                           @RequestParam(required = false, defaultValue = "all") String searchField,
                                                           @RequestParam(required = false, defaultValue = "recent") String sortBy,
                                                           HttpSession session) {
        User user = (User) session.getAttribute("user");

        try {
            List<BusinessCard> cards = businessCardService.searchCards(user, category, keyword, searchField, sortBy);
            byte[] excelData = businessCardService.exportToExcel(cards);

            ByteArrayResource resource = new ByteArrayResource(excelData);

            String filename = "명함목록_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                    ".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(excelData.length)
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    //이메일 다운로드
    @GetMapping("/download-emails")
    public ResponseEntity<ByteArrayResource> downloadEmails(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "all") String searchField,
            @RequestParam(required = false, defaultValue = "recent") String sortBy,
            @RequestParam(required = false) String excludeCompanies,
            @RequestParam(required = false, defaultValue = "false") boolean semicolon,
            HttpSession session) {

        User user = (User) session.getAttribute("user");

        try {
            List<BusinessCard> cards = businessCardService.searchCards(user, category, keyword, searchField, sortBy);

            // 이메일 TXT 파일 생성
            byte[] emailData = businessCardService.exportEmailsToTxt(cards, excludeCompanies, semicolon);

            ByteArrayResource resource = new ByteArrayResource(emailData);

            String filename = "이메일목록_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                    ".txt";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.TEXT_PLAIN.parseMediaType("text/plain; charset-UTF-8"))
                    .contentLength(emailData.length)
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @ExceptionHandler(IllegalStateException.class)
    public String handleUnauthorized() {
        return "redirect:/login";
    }
}