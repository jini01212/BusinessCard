package com.demo.sms.card.service;

import com.demo.sms.card.dto.BusinessCardDto;
import com.demo.sms.card.dto.UploadResult;
import com.demo.sms.card.entity.BusinessCard;
import com.demo.sms.card.entity.BusinessCard.Category;
import com.demo.sms.card.entity.User;
import com.demo.sms.card.repository.BusinessCardRepository;
import com.demo.sms.card.util.ExcelUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BusinessCardService {

    private final BusinessCardRepository businessCardRepository;
    private final ExcelUtil excelUtil;

    public BusinessCard saveCard(BusinessCardDto dto, User user) {
        BusinessCard card = convertToEntity(dto, user);
        return businessCardRepository.save(card);
    }

    public BusinessCard getCard(Long id, User user) {
        BusinessCard card = businessCardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("명함을 찾을 수 없습니다."));

        if (!card.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("접근 권한이 없습니다.");
        }

        return card;
    }

    public Optional<BusinessCard> getPreviousCard(Long currentId, User user) {
        return businessCardRepository.findPreviousCard(user, currentId);
    }

    public Optional<BusinessCard> getNextCard(Long currentId, User user) {
        return businessCardRepository.findNextCard(user, currentId);
    }

    public BusinessCard updateCard(Long id, BusinessCardDto dto, User user) {
        BusinessCard card = getCard(id, user);

        card.setName(dto.getName());
        card.setCompany(dto.getCompany());
        card.setDepartment(dto.getDepartment());
        card.setPosition(dto.getPosition());
        card.setAddress(dto.getAddress());
        card.setOfficePhone(dto.getOfficePhone());
        card.setOfficeFax(dto.getOfficeFax());
        card.setMobilePhone(dto.getMobilePhone());
        card.setEmail(dto.getEmail());
        card.setWebsite(dto.getWebsite());
        card.setCategory(dto.getCategory());
        card.setNotes(dto.getNotes());

        return businessCardRepository.save(card);
    }

    public void deleteCard(Long id, User user) {
        BusinessCard card = getCard(id, user);
        businessCardRepository.delete(card);
    }

    /**
     * 엑셀 업로드
     */

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z.]{2,}");


    public UploadResult uploadExcel(MultipartFile file, Category category, User user, String duplicateAction) throws IOException {
        UploadResult result = UploadResult.builder().build();

        // ExcelUtil을 사용하여 파일 파싱
        List<BusinessCardDto> dtoList = excelUtil.parseExcelFile(file);
        result.setTotalRows(dtoList.size());

        for (int i = 0; i < dtoList.size(); i++) {
            BusinessCardDto dto = dtoList.get(i);
            int rowNumber = i + 2;

            dto.setCategory(category);

            // 이름 필수 체크
            if (dto.getName() == null || dto.getName().trim().isEmpty()) {
                result.getErrors().add(String.format("행 %d: 이름이 누락되었습니다", rowNumber));
                continue;
            }

            // 데이터 정규화
            String name = dto.getName().trim();
            String company = (dto.getCompany() != null && !dto.getCompany().trim().isEmpty())
                    ? dto.getCompany().trim() : null;
            String mobilePhone = (dto.getMobilePhone() != null && !dto.getMobilePhone().trim().isEmpty())
                    ? dto.getMobilePhone().trim() : null;
            String email = (dto.getEmail() != null && !dto.getEmail().trim().isEmpty())
                    ? dto.getEmail().trim() : null;
            String officePhone = (dto.getOfficePhone() != null && !dto.getOfficePhone().trim().isEmpty())
                    ? dto.getOfficePhone().trim() : null;

            //이메일 정규식으로 추출
            if (email != null) {
                Pattern EMAIL_PATTERN = Pattern.compile("[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,6}");
                Matcher matcher = EMAIL_PATTERN.matcher(email);
                String lastEmail = null;
                while (matcher.find()) {
                    lastEmail = matcher.group();
                }
                email = lastEmail != null ? lastEmail.trim().toLowerCase() : null;
            }
            dto.setEmail(email);

            // 중복 체크
            Optional<BusinessCard> existing = findExistingCard(user, name, company, mobilePhone, email, officePhone);

            if (existing.isPresent()) {
                if ("overwrite".equals(duplicateAction)) {
                    // 덮어쓰기
                    BusinessCard existingCard = existing.get();

                    // 기존 레코드 삭제
                    businessCardRepository.delete(existingCard);
                    businessCardRepository.flush();

                    // 새로운 레코드 생성
                    BusinessCard newCard = convertToEntity(dto, user);
                    businessCardRepository.save(newCard);

                    result.setUpdateCount(result.getUpdateCount() + 1);
                } else {
                    // 건너뛰기
                    String duplicateInfo = formatDuplicateInfo(name, company, mobilePhone, email, officePhone);
                    result.getDuplicates().add(duplicateInfo);
                    result.setSkipCount(result.getSkipCount() + 1);
                }
            } else {
                // 새로운 데이터 저장
                BusinessCard newCard = convertToEntity(dto, user);
                businessCardRepository.save(newCard);
                result.setSuccessCount(result.getSuccessCount() + 1);
            }
        }

        return result;
    }

    /**
     * 기존 명함 찾기
     */
    private Optional<BusinessCard> findExistingCard(User user, String name, String company,
                                                    String mobilePhone, String email, String officePhone) {
        // 이름 + 휴대폰
        if (mobilePhone != null) {
            Optional<BusinessCard> found = businessCardRepository
                    .findByUserAndNameAndMobilePhone(user, name, mobilePhone);
            if (found.isPresent()) {
                return found;
            }
        }

        // 이름 + 이메일
        if (email != null) {
            Optional<BusinessCard> found = businessCardRepository
                    .findByUserAndNameAndEmail(user, name, email);
            if (found.isPresent()) {
                return found;
            }
        }

        // 휴대폰 단독
        if (mobilePhone != null) {
            Optional<BusinessCard> found = businessCardRepository
                    .findByUserAndMobilePhone(user, mobilePhone);
            if (found.isPresent()) {
                return found;
            }
        }

        // 이름 + 회사 (전화번호와 이메일이 둘 다 없을 때만)
        if (company != null && mobilePhone == null && email == null) {
            Optional<BusinessCard> found = businessCardRepository
                    .findByUserAndNameAndCompany(user, name, company);
            if (found.isPresent()) {
                return found;
            }
        }

        return Optional.empty();
    }

    /**
     * 중복 정보 포맷팅
     */
    private String formatDuplicateInfo(String name, String company, String mobilePhone,
                                       String email, String officePhone) {
        StringBuilder sb = new StringBuilder();

        if (name != null) {
            sb.append(name);
        }

        List<String> infos = new ArrayList<>();
        if (mobilePhone != null) infos.add("휴대폰: " + mobilePhone);
        if (company != null) infos.add("회사: " + company);
        if (email != null) infos.add("이메일: " + email);
        if (officePhone != null) infos.add("근무처: " + officePhone);

        if (!infos.isEmpty()) {
            sb.append(" (").append(String.join(", ", infos)).append(")");
        }

        return sb.toString();
    }

    private BusinessCard convertToEntity(BusinessCardDto dto, User user) {
        return BusinessCard.builder()
                .name(dto.getName())
                .company(dto.getCompany())
                .department(dto.getDepartment())
                .position(dto.getPosition())
                .address(dto.getAddress())
                .officePhone(dto.getOfficePhone())
                .officeFax(dto.getOfficeFax())
                .mobilePhone(dto.getMobilePhone())
                .email(dto.getEmail())
                .website(dto.getWebsite())
                .category(dto.getCategory())
                .notes(dto.getNotes())
                .user(user)
                .build();
    }

    private void updateBusinessCardFromDto(BusinessCard card, BusinessCardDto dto) {
        card.setName(dto.getName());
        card.setCompany(dto.getCompany());
        card.setDepartment(dto.getDepartment());
        card.setPosition(dto.getPosition());
        card.setAddress(dto.getAddress());
        card.setOfficePhone(dto.getOfficePhone());
        card.setOfficeFax(dto.getOfficeFax());
        card.setMobilePhone(dto.getMobilePhone());
        card.setEmail(dto.getEmail());
        card.setWebsite(dto.getWebsite());
        card.setCategory(dto.getCategory());
        card.setNotes(dto.getNotes());
    }

    public List<BusinessCard> getAllCards(User user) {
        return businessCardRepository.findByUser(user);
    }

    /**
     * 검색 + 정렬
     */
    public List<BusinessCard> searchCards(User user, Category category, String keyword, String searchField, String sortBy) {
        // 정렬 기본값
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "recent";
        }

        // 키워드가 없으면 카테고리 또는 전체 조회
        if (keyword == null || keyword.trim().isEmpty()) {
            if (category != null) {
                return switch (sortBy) {
                    case "name" -> businessCardRepository.findByUserAndCategoryOrderByName(user, category);
                    case "oldest" -> businessCardRepository.findByUserAndCategoryOrderByCreatedAsc(user, category);
                    default -> businessCardRepository.findByUserAndCategoryOrderByCreatedDesc(user, category);
                };
            } else {
                return switch (sortBy) {
                    case "name" -> businessCardRepository.findByUserOrderByNameAsc(user);
                    case "oldest" -> businessCardRepository.findByUserOrderByCreatedAtAsc(user);
                    default -> businessCardRepository.findByUserOrderByCreatedAtDesc(user);
                };
            }
        }

        // 검색 필드 기본값
        if (searchField == null || searchField.trim().isEmpty()) {
            searchField = "all";
        }



//        // 검색 + 정렬
//        return switch (searchField) {
//            case "name" -> switch (sortBy) {
//                case "name" -> businessCardRepository.searchByNameOrderByName(user, keyword);
//                case "oldest" -> businessCardRepository.searchByNameOrderByCreatedAsc(user, keyword);
//                default -> businessCardRepository.searchByNameOrderByCreatedDesc(user, keyword);
//            };
//            case "company" -> switch (sortBy) {
//                case "name" -> businessCardRepository.searchByCompanyOrderByName(user, keyword);
//                case "oldest" -> businessCardRepository.searchByCompanyOrderByCreatedAsc(user, keyword);
//                default -> businessCardRepository.searchByCompanyOrderByCreatedDesc(user, keyword);
//            };
//            case "position" -> switch (sortBy) {
//                case "name" -> businessCardRepository.searchByPositionOrderByName(user, keyword);
//                case "oldest" -> businessCardRepository.searchByPositionOrderByCreatedAsc(user, keyword);
//                default -> businessCardRepository.searchByPositionOrderByCreatedDesc(user, keyword);
//            };
//            case "address" -> switch (sortBy) {
//                case "name" -> businessCardRepository.searchByAddressOrderByName(user, keyword);
//                case "oldest" -> businessCardRepository.searchByAddressOrderByCreatedAsc(user, keyword);
//                default -> businessCardRepository.searchByAddressOrderByCreatedDesc(user, keyword);
//            };
//            default -> switch (sortBy) {
//                case "name" -> businessCardRepository.searchByKeywordOrderByName(user, keyword);
//                case "oldest" -> businessCardRepository.searchByKeywordOrderByCreatedAsc(user, keyword);
//                default -> businessCardRepository.searchByKeywordOrderByCreatedDesc(user, keyword);
//            };
//        };

        // 검색 + 정렬 + 카테고리
        return switch (searchField != null ? searchField : "all") {
            case "name" -> switch (sortBy != null ? sortBy : "recent") {
                case "name" -> category != null
                        ? businessCardRepository.searchByNameAndCategoryOrderByName(user, keyword, category)
                        : businessCardRepository.searchByNameOrderByName(user, keyword);
                case "oldest" -> category != null
                        ? businessCardRepository.searchByNameAndCategoryOrderByCreatedAsc(user, keyword, category)
                        : businessCardRepository.searchByNameOrderByCreatedAsc(user, keyword);
                default -> category != null
                        ? businessCardRepository.searchByNameAndCategoryOrderByCreatedDesc(user, keyword, category)
                        : businessCardRepository.searchByNameOrderByCreatedDesc(user, keyword);
            };
            case "company" -> switch (sortBy != null ? sortBy : "recent") {
                case "name" -> category != null
                        ? businessCardRepository.searchByCompanyAndCategoryOrderByName(user, keyword, category)
                        : businessCardRepository.searchByCompanyOrderByName(user, keyword);
                case "oldest" -> category != null
                        ? businessCardRepository.searchByCompanyAndCategoryOrderByCreatedAsc(user, keyword, category)
                        : businessCardRepository.searchByCompanyOrderByCreatedAsc(user, keyword);
                default -> category != null
                        ? businessCardRepository.searchByCompanyAndCategoryOrderByCreatedDesc(user, keyword, category)
                        : businessCardRepository.searchByCompanyOrderByCreatedDesc(user, keyword);
            };
            case "position" -> switch (sortBy != null ? sortBy : "recent") {
                case "name" -> category != null
                        ? businessCardRepository.searchByPositionAndCategoryOrderByName(user, keyword, category)
                        : businessCardRepository.searchByPositionOrderByName(user, keyword);
                case "oldest" -> category != null
                        ? businessCardRepository.searchByPositionAndCategoryOrderByCreatedAsc(user, keyword, category)
                        : businessCardRepository.searchByPositionOrderByCreatedAsc(user, keyword);
                default -> category != null
                        ? businessCardRepository.searchByPositionAndCategoryOrderByCreatedDesc(user, keyword, category)
                        : businessCardRepository.searchByPositionOrderByCreatedDesc(user, keyword);
            };
            case "address" -> switch (sortBy != null ? sortBy : "recent") {
                case "name" -> category != null
                        ? businessCardRepository.searchByAddressAndCategoryOrderByName(user, keyword, category)
                        : businessCardRepository.searchByAddressOrderByName(user, keyword);
                case "oldest" -> category != null
                        ? businessCardRepository.searchByAddressAndCategoryOrderByCreatedAsc(user, keyword, category)
                        : businessCardRepository.searchByAddressOrderByCreatedAsc(user, keyword);
                default -> category != null
                        ? businessCardRepository.searchByAddressAndCategoryOrderByCreatedDesc(user, keyword, category)
                        : businessCardRepository.searchByAddressOrderByCreatedDesc(user, keyword);
            };
            default -> switch (sortBy != null ? sortBy : "recent") {
                case "name" -> category != null
                        ? businessCardRepository.searchByKeywordAndCategoryOrderByName(user, keyword, category)
                        : businessCardRepository.searchByKeywordOrderByName(user, keyword);
                case "oldest" -> category != null
                        ? businessCardRepository.searchByKeywordAndCategoryOrderByCreatedAsc(user, keyword, category)
                        : businessCardRepository.searchByKeywordOrderByCreatedAsc(user, keyword);
                default -> category != null
                        ? businessCardRepository.searchByKeywordAndCategoryOrderByCreatedDesc(user, keyword, category)
                        : businessCardRepository.searchByKeywordOrderByCreatedDesc(user, keyword);
            };
        };





    }

    public Map<Category, Long> getCategoryStatistics(User user) {
        List<BusinessCard> allCards = businessCardRepository.findByUser(user);
        return allCards.stream()
                .collect(Collectors.groupingBy(BusinessCard::getCategory, Collectors.counting()));
    }

    public List<BusinessCard> getRecentCards(User user, int limit) {
        return businessCardRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    public long getTotalCount(User user) {
        return businessCardRepository.countByUser(user);
    }

    /**
     * ExcelUtil을 사용한 엑셀 다운로드
     */
    public byte[] exportToExcel(List<BusinessCard> cards) throws IOException {
        return excelUtil.generateExcelFile(cards);
    }

    /**
     * 중복 명함 찾기
     */
    public Map<String, List<BusinessCard>> findDuplicates(User user) {
        List<BusinessCard> allCards = businessCardRepository.findByUser(user);
        Map<String, List<BusinessCard>> duplicates = new HashMap<>();

        // 이름 + 휴대폰으로 그룹화
        Map<String, List<BusinessCard>> groupedByNameAndPhone = allCards.stream()
                .filter(card -> card.getName() != null && card.getMobilePhone() != null)
                .collect(Collectors.groupingBy(card ->
                        card.getName() + "|" + card.getMobilePhone()));

        groupedByNameAndPhone.forEach((key, cards) -> {
            if (cards.size() > 1) {
                duplicates.put(key, cards);
            }
        });

//        // 이름 + 회사로 그룹화
//        Map<String, List<BusinessCard>> groupedByNameAndCompany = allCards.stream()
//                .filter(card -> card.getName() != null && card.getCompany() != null)
//                .collect(Collectors.groupingBy(card ->
//                        card.getName() + "|" + card.getCompany()));
//
//        groupedByNameAndCompany.forEach((key, cards) -> {
//            if (cards.size() > 1 && !duplicates.containsKey(key)) {
//                duplicates.put(key, cards);
//            }
//        });

        // 이름 + 이메일로 그룹화
        Map<String, List<BusinessCard>> groupedByNameAndEmail = allCards.stream()
                .filter(card -> card.getName() != null && card.getEmail() != null)
                .collect(Collectors.groupingBy(card ->
                        card.getName() + "|" + card.getEmail()));

        groupedByNameAndEmail.forEach((key, cards) -> {
            if (cards.size() > 1 && !duplicates.containsKey(key)) {
                duplicates.put(key, cards);
            }
        });

        return duplicates;
    }

    /**
     * 중복 명함 삭제
     */
    public int cleanDuplicates(User user, String strategy) {
        Map<String, List<BusinessCard>> duplicates = findDuplicates(user);
        int deletedCount = 0;

        for (List<BusinessCard> cards : duplicates.values()) {
            if (cards.size() <= 1) continue;

            // 정렬
            cards.sort(Comparator.comparing(BusinessCard::getCreatedAt));

            // 남길 카드 결정
            BusinessCard keepCard = "oldest".equals(strategy) ? cards.get(0) : cards.get(cards.size() - 1);

            // 나머지 삭제
            for (BusinessCard card : cards) {
                if (!card.getId().equals(keepCard.getId())) {
                    businessCardRepository.delete(card);
                    deletedCount++;
                }
            }
        }

        return deletedCount;
    }

    /**
     * 이메일 저장
     */
    public byte[] exportEmailsToTxt(List<BusinessCard> cards, String excludeCompanies, boolean semicolon) throws IOException {
        // 제외할 회사명 리스트 생성
        Set<String> excludeSet = new HashSet<>();
        if (excludeCompanies != null && !excludeCompanies.trim().isEmpty()) {
            String[] companies = excludeCompanies.split(",");
            for (String company : companies) {
                excludeSet.add(company.trim().toLowerCase());
            }
        }

        // 중복 제거
        Set<String> uniqueEmails = new LinkedHashSet<>();


        for (BusinessCard card : cards) {

            if (card.getCompany() != null &&
                    excludeSet.contains(card.getCompany().trim().toLowerCase())) {
                continue;
            }

            // 이메일이 존재하면 추가
            if (card.getEmail() != null && !card.getEmail().trim().isEmpty()) {
                uniqueEmails.add(card.getEmail().trim());
            }
        }

        // TXT 파일 생성 (10개씩 줄바꿈)
        StringBuilder content = new StringBuilder();
        int count = 0;

        for (String email : uniqueEmails) {
            if (count > 0 && count % 10 == 0) {
                content.append("\n");
            }

            content.append(email);

            if (semicolon) {
                content.append(";");
            }

            if (count % 10 != 9) {
                content.append(" ");
            }

            count++;
        }

        return content.toString().getBytes("UTF-8");
    }

}