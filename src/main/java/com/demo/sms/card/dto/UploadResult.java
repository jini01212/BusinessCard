package com.demo.sms.card.dto;

import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadResult {
    private int totalRows;
    private int successCount;
    private int updateCount;
    private int skipCount;

    @Builder.Default
    private List<String> errors = new ArrayList<>();

    @Builder.Default
    private List<String> duplicates = new ArrayList<>();
}