package com.demo.sms.card.util;

import com.demo.sms.card.dto.BusinessCardDto;
import com.demo.sms.card.entity.BusinessCard;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelUtil {

    /**
     * 엑셀 파일을 읽어서 BusinessCardDto 리스트로 변환
     */
    public List<BusinessCardDto> parseExcelFile(MultipartFile file) throws IOException {
        List<BusinessCardDto> cardList = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            // 첫 번째 행은 헤더로 간주하고 스킵하지 않음
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                BusinessCardDto dto = BusinessCardDto.builder()
                        .name(getCellValueAsString(row.getCell(0)))
                        .company(getCellValueAsString(row.getCell(1)))
                        .department(getCellValueAsString(row.getCell(2)))
                        .position(getCellValueAsString(row.getCell(3)))
                        .address(getCellValueAsString(row.getCell(4)))
                        .officePhone(getCellValueAsString(row.getCell(5)))
                        .officeFax(getCellValueAsString(row.getCell(6)))
                        .mobilePhone(getCellValueAsString(row.getCell(7)))
                        .email(getCellValueAsString(row.getCell(8)))
                        .website(getCellValueAsString(row.getCell(9)))
                        .notes(getCellValueAsString(row.getCell(10)))
                        // category는 Service에서 설정
                        .build();

                // 이름이 비어있지 않은 경우만 추가
                if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
                    cardList.add(dto);
                }
            }
        }

        return cardList;
    }

    /**
     * BusinessCard 엔티티 리스트를 엑셀 파일로 변환
     */
    public byte[] generateExcelFile(List<BusinessCard> cardList) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("명함 목록");

            // 헤더 스타일 설정
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // 헤더 행 생성
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "이름", "회사", "부서", "직함", "주소",
                    "근무처 전화", "근무처 팩스", "휴대폰", "이메일",
                    "웹사이트", "분류", "비고"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000);  // 컬럼 너비 설정
            }

            // 데이터 행 생성
            int rowNum = 1;
            for (BusinessCard card : cardList) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(card.getName() != null ? card.getName() : "");
                row.createCell(1).setCellValue(card.getCompany() != null ? card.getCompany() : "");
                row.createCell(2).setCellValue(card.getDepartment() != null ? card.getDepartment() : "");
                row.createCell(3).setCellValue(card.getPosition() != null ? card.getPosition() : "");
                row.createCell(4).setCellValue(card.getAddress() != null ? card.getAddress() : "");
                row.createCell(5).setCellValue(card.getOfficePhone() != null ? card.getOfficePhone() : "");
                row.createCell(6).setCellValue(card.getOfficeFax() != null ? card.getOfficeFax() : "");
                row.createCell(7).setCellValue(card.getMobilePhone() != null ? card.getMobilePhone() : "");
                row.createCell(8).setCellValue(card.getEmail() != null ? card.getEmail() : "");
                row.createCell(9).setCellValue(card.getWebsite() != null ? card.getWebsite() : "");
                row.createCell(10).setCellValue(card.getCategory() != null ? card.getCategory().getDisplayName() : "");
                row.createCell(11).setCellValue(card.getNotes() != null ? card.getNotes() : "");
            }

            // 자동 너비 조정
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Cell 값을 문자열로 변환하는 헬퍼 메서드 (DataFormatter 사용)
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        // DataFormatter를 사용하여 셀 형식에 맞게 문자열 변환
        DataFormatter formatter = new DataFormatter();
        String value = formatter.formatCellValue(cell).trim();

        return value.isEmpty() ? null : value;
    }
}