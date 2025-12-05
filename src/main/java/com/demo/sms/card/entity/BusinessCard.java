package com.demo.sms.card.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "business_card")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 100)
    private String company;

    @Column(length = 50)
    private String department;

    @Column(length = 50)
    private String position;

    @Column(length = 800)
    private String address;

    @Column(name = "office_phone", length = 500)
    private String officePhone;

    @Column(name = "office_fax", length = 500)
    private String officeFax;

    @Column(name = "mobile_phone", length = 500)
    private String mobilePhone;

    @Column(length = 100)
    private String email;

    @Column(length = 100)
    private String website;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category;

    @Column(length = 500)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Category {
        COMPANY("일반기업"),
        SCHOOL("학교"),
        ASSOCIATION("협회"),
        GOVERNMENT("관공서");

        private final String displayName;

        Category(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}