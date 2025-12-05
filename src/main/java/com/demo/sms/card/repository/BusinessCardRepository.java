package com.demo.sms.card.repository;

import com.demo.sms.card.entity.BusinessCard;
import com.demo.sms.card.entity.BusinessCard.Category;
import com.demo.sms.card.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessCardRepository extends JpaRepository<BusinessCard, Long> {

    List<BusinessCard> findByUser(User user);

    List<BusinessCard> findByUserAndCategory(User user, Category category);

    List<BusinessCard> findByUserOrderByCreatedAtDesc(User user);

    List<BusinessCard> findByUserOrderByCreatedAtAsc(User user);

    List<BusinessCard> findByUserOrderByNameAsc(User user);

    long countByUser(User user);

    // 이름 + 휴대폰
    Optional<BusinessCard> findByUserAndNameAndMobilePhone(User user, String name, String mobilePhone);

    // 이름 + 이메일
    Optional<BusinessCard> findByUserAndNameAndEmail(User user, String name, String email);

    // 휴대폰
    Optional<BusinessCard> findByUserAndMobilePhone(User user, String mobilePhone);

    // 이름 + 회사
    Optional<BusinessCard> findByUserAndNameAndCompany(User user, String name, String company);

    // 이전 명함 찾기
    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND b.id < :id ORDER BY b.id DESC LIMIT 1")
    Optional<BusinessCard> findPreviousCard(@Param("user") User user, @Param("id") Long id);

    // 다음 명함 찾기
    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND b.id > :id ORDER BY b.id ASC LIMIT 1")
    Optional<BusinessCard> findNextCard(@Param("user") User user, @Param("id") Long id);

    // 이름으로 검색 + 정렬
    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY b.name ASC")
    List<BusinessCard> searchByNameOrderByName(@Param("user") User user, @Param("keyword") String keyword);

    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY b.createdAt DESC")
    List<BusinessCard> searchByNameOrderByCreatedDesc(@Param("user") User user, @Param("keyword") String keyword);

    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY b.createdAt ASC")
    List<BusinessCard> searchByNameOrderByCreatedAsc(@Param("user") User user, @Param("keyword") String keyword);

    // 회사로 검색 + 정렬
    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND LOWER(b.company) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY b.name ASC")
    List<BusinessCard> searchByCompanyOrderByName(@Param("user") User user, @Param("keyword") String keyword);

    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND LOWER(b.company) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY b.createdAt DESC")
    List<BusinessCard> searchByCompanyOrderByCreatedDesc(@Param("user") User user, @Param("keyword") String keyword);

    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND LOWER(b.company) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY b.createdAt ASC")
    List<BusinessCard> searchByCompanyOrderByCreatedAsc(@Param("user") User user, @Param("keyword") String keyword);

    // 직함으로 검색 + 정렬
    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND LOWER(b.position) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY b.name ASC")
    List<BusinessCard> searchByPositionOrderByName(@Param("user") User user, @Param("keyword") String keyword);

    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND LOWER(b.position) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY b.createdAt DESC")
    List<BusinessCard> searchByPositionOrderByCreatedDesc(@Param("user") User user, @Param("keyword") String keyword);

    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND LOWER(b.position) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY b.createdAt ASC")
    List<BusinessCard> searchByPositionOrderByCreatedAsc(@Param("user") User user, @Param("keyword") String keyword);

    // 주소로 검색 + 정렬
    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND LOWER(b.address) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY b.name ASC")
    List<BusinessCard> searchByAddressOrderByName(@Param("user") User user, @Param("keyword") String keyword);

    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND LOWER(b.address) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY b.createdAt DESC")
    List<BusinessCard> searchByAddressOrderByCreatedDesc(@Param("user") User user, @Param("keyword") String keyword);

    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND LOWER(b.address) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY b.createdAt ASC")
    List<BusinessCard> searchByAddressOrderByCreatedAsc(@Param("user") User user, @Param("keyword") String keyword);

    // 전체 필드 검색 + 정렬
    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND " +
            "(LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.company) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.position) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY b.name ASC")
    List<BusinessCard> searchByKeywordOrderByName(@Param("user") User user, @Param("keyword") String keyword);

    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND " +
            "(LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.company) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.position) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY b.createdAt DESC")
    List<BusinessCard> searchByKeywordOrderByCreatedDesc(@Param("user") User user, @Param("keyword") String keyword);

    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND " +
            "(LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.company) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.position) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY b.createdAt ASC")
    List<BusinessCard> searchByKeywordOrderByCreatedAsc(@Param("user") User user, @Param("keyword") String keyword);

    // 카테고리 + 검색 + 정렬
    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND b.category = :category ORDER BY b.name ASC")
    List<BusinessCard> findByUserAndCategoryOrderByName(@Param("user") User user, @Param("category") Category category);

    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND b.category = :category ORDER BY b.createdAt DESC")
    List<BusinessCard> findByUserAndCategoryOrderByCreatedDesc(@Param("user") User user, @Param("category") Category category);

    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND b.category = :category ORDER BY b.createdAt ASC")
    List<BusinessCard> findByUserAndCategoryOrderByCreatedAsc(@Param("user") User user, @Param("category") Category category);


// 카테고리 + 검색 + 정렬
    // 이름
    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND b.category = :category AND LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY b.name ASC")
    List<BusinessCard> searchByNameAndCategoryOrderByName(@Param("user") User user,
                                                          @Param("keyword") String keyword,
                                                          @Param("category") Category category);

    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND b.category = :category AND LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY b.createdAt DESC")
    List<BusinessCard> searchByNameAndCategoryOrderByCreatedDesc(@Param("user") User user,
                                                                 @Param("keyword") String keyword,
                                                                 @Param("category") Category category);

    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND b.category = :category AND LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY b.createdAt ASC")
    List<BusinessCard> searchByNameAndCategoryOrderByCreatedAsc(@Param("user") User user,
                                                                @Param("keyword") String keyword,
                                                                @Param("category") Category category);

    // 회사
    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND b.category = :category AND LOWER(b.company) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY b.name ASC")
    List<BusinessCard> searchByCompanyAndCategoryOrderByName(@Param("user") User user,
                                                             @Param("keyword") String keyword,
                                                             @Param("category") Category category);

    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND b.category = :category AND LOWER(b.company) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY b.createdAt DESC")
    List<BusinessCard> searchByCompanyAndCategoryOrderByCreatedDesc(@Param("user") User user,
                                                                    @Param("keyword") String keyword,
                                                                    @Param("category") Category category);

    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND b.category = :category AND LOWER(b.company) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY b.createdAt ASC")
    List<BusinessCard> searchByCompanyAndCategoryOrderByCreatedAsc(@Param("user") User user,
                                                                   @Param("keyword") String keyword,
                                                                   @Param("category") Category category);

    // 직함
    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND b.category = :category AND LOWER(b.position) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY b.name ASC")
    List<BusinessCard> searchByPositionAndCategoryOrderByName(@Param("user") User user,
                                                              @Param("keyword") String keyword,
                                                              @Param("category") Category category);

    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND b.category = :category AND LOWER(b.position) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY b.createdAt DESC")
    List<BusinessCard> searchByPositionAndCategoryOrderByCreatedDesc(@Param("user") User user,
                                                                     @Param("keyword") String keyword,
                                                                     @Param("category") Category category);

    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND b.category = :category AND LOWER(b.position) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY b.createdAt ASC")
    List<BusinessCard> searchByPositionAndCategoryOrderByCreatedAsc(@Param("user") User user,
                                                                    @Param("keyword") String keyword,
                                                                    @Param("category") Category category);

    // 주소
    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND b.category = :category AND LOWER(b.address) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY b.name ASC")
    List<BusinessCard> searchByAddressAndCategoryOrderByName(@Param("user") User user,
                                                             @Param("keyword") String keyword,
                                                             @Param("category") Category category);

    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND b.category = :category AND LOWER(b.address) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY b.createdAt DESC")
    List<BusinessCard> searchByAddressAndCategoryOrderByCreatedDesc(@Param("user") User user,
                                                                    @Param("keyword") String keyword,
                                                                    @Param("category") Category category);

    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND b.category = :category AND LOWER(b.address) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY b.createdAt ASC")
    List<BusinessCard> searchByAddressAndCategoryOrderByCreatedAsc(@Param("user") User user,
                                                                   @Param("keyword") String keyword,
                                                                   @Param("category") Category category);

    // 전체(all)
    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND b.category = :category AND " +
            "(LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.company) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.position) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY b.name ASC")
    List<BusinessCard> searchByKeywordAndCategoryOrderByName(@Param("user") User user,
                                                             @Param("keyword") String keyword,
                                                             @Param("category") Category category);

    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND b.category = :category AND " +
            "(LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.company) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.position) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY b.createdAt DESC")
    List<BusinessCard> searchByKeywordAndCategoryOrderByCreatedDesc(@Param("user") User user,
                                                                    @Param("keyword") String keyword,
                                                                    @Param("category") Category category);

    @Query("SELECT b FROM BusinessCard b WHERE b.user = :user AND b.category = :category AND " +
            "(LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.company) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.position) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY b.createdAt ASC")
    List<BusinessCard> searchByKeywordAndCategoryOrderByCreatedAsc(@Param("user") User user,
                                                                   @Param("keyword") String keyword,
                                                                   @Param("category") Category category);

}