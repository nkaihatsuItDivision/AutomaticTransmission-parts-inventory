package com.example.automatictransmissionpartsinventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 高度検索条件を格納するDTOクラス
 * Phase 8.3 Step 6-1で追加
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdvancedSearchCriteria {
    
    // 基本検索条件
    private String partNumber;      // 部品番号（部分一致）
    private String partName;        // 部品名（部分一致）
    private String manufacturer;    // メーカー（部分一致）
    
    // カテゴリ検索
    private Long categoryId;        // カテゴリID（完全一致）
    private String categoryName;    // カテゴリ名（部分一致）
    
    // 価格範囲検索
    private BigDecimal minPrice;    // 最低価格
    private BigDecimal maxPrice;    // 最高価格
    
    // 日付範囲検索
    private String createdAfter;   // 登録日以降
    private String createdBefore;  // 登録日以前
    private String updatedAfter;   // 更新日以降
    private String updatedBefore;  // 更新日以前
    
    // 並び替え条件
    private String sortBy;          // 並び替えフィールド
    private String sortOrder;       // 並び替え順序（ASC/DESC）
    
    // ページネーション
    private Integer page;           // ページ番号（0から開始）
    private Integer size;           // 1ページあたりの件数
    
    // 検索履歴用
    private String searchName;      // 検索条件名（お気に入り用）
    
    /**
     * 空の検索条件かどうかを判定
     */
    public boolean isEmpty() {
        return (partNumber == null || partNumber.trim().isEmpty()) &&
               (partName == null || partName.trim().isEmpty()) &&
               (manufacturer == null || manufacturer.trim().isEmpty()) &&
               categoryId == null &&
               (categoryName == null || categoryName.trim().isEmpty()) &&
               minPrice == null &&
               maxPrice == null &&
               createdAfter == null &&
               createdBefore == null &&
               updatedAfter == null &&
               updatedBefore == null;
    }
    
    /**
     * デフォルトの並び替え条件を設定
     */
    public void setDefaultSort() {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            this.sortBy = "updatedAt";
            this.sortOrder = "DESC";
        }
    }
    
    /**
     * デフォルトのページネーション設定
     */
    public void setDefaultPagination() {
        if (page == null || page < 0) {
            this.page = 0;
        }
        if (size == null || size <= 0) {
            this.size = 20; // デフォルトで20件表示
        }
    }
    /**
     * String型の日付をLocalDateTimeに変換するヘルパーメソッド
     */
    public LocalDateTime getCreatedAfterAsDateTime() {
        if (createdAfter == null || createdAfter.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(createdAfter).atStartOfDay();
        } catch (Exception e) {
            return null;
        }
    }

    public LocalDateTime getCreatedBeforeAsDateTime() {
        if (createdBefore == null || createdBefore.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(createdBefore).atTime(23, 59, 59);
        } catch (Exception e) {
            return null;
        }
    }

    public LocalDateTime getUpdatedAfterAsDateTime() {
        if (updatedAfter == null || updatedAfter.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(updatedAfter).atStartOfDay();
        } catch (Exception e) {
            return null;
        }
    }

    public LocalDateTime getUpdatedBeforeAsDateTime() {
        if (updatedBefore == null || updatedBefore.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(updatedBefore).atTime(23, 59, 59);
        } catch (Exception e) {
            return null;
        }
    }
}