package com.demo.sms.card.dto;

import com.demo.sms.card.entity.BusinessCard.Category;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessCardDto {
    private Long id;
    private String name;
    private String company;
    private String department;
    private String position;
    private String address;
    private String officePhone;
    private String officeFax;
    private String mobilePhone;
    private String email;
    private String website;
    private Category category;
    private String notes;
}