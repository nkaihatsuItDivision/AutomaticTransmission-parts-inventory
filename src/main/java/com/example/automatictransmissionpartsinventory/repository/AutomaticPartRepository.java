package com.example.automatictransmissionpartsinventory.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.automatictransmissionpartsinventory.dto.AdvancedSearchCriteria;
import com.example.automatictransmissionpartsinventory.entity.AutomativePart;

@Repository
public interface AutomaticPartRepository extends JpaRepository<AutomativePart, Long> {
    
    // 部品番号での検索
    Optional<AutomativePart> findByPartNumber(String partNumber);
    
    // 部品名での部分一致検索
    List<AutomativePart> findByPartNameContaining(String partName);
    
    // 製造者での検索
    List<AutomativePart> findByManufacturer(String manufacturer);
    
    // 価格範囲での検索
    List<AutomativePart> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
 // カテゴリによる検索
    List<AutomativePart> findByCategoryId(Long categoryId);

    // カテゴリが未設定の部品検索  
    List<AutomativePart> findByCategoryIsNull();


    
    // 複合検索（部品名 + 製造者）
    @Query("SELECT ap FROM AutomativePart ap WHERE " +
    	       "(:partName IS NULL OR ap.partName LIKE %:partName%) AND " +
    	       "(:manufacturer IS NULL OR ap.manufacturer = :manufacturer) AND " +
    	       "(:minPrice IS NULL OR ap.price >= :minPrice) AND " +
    	       "(:maxPrice IS NULL OR ap.price <= :maxPrice) AND " +
    	       "(:categoryId IS NULL OR ap.category.id = :categoryId)")
    	List<AutomativePart> findByConditions(
    	        @Param("partName") String partName,
    	        @Param("manufacturer") String manufacturer,
    	        @Param("minPrice") BigDecimal minPrice,
    	        @Param("maxPrice") BigDecimal maxPrice,
    	        @Param("categoryId") Long categoryId
    	);
    
    // 部品番号の重複チェック
    boolean existsByPartNumber(String partNumber);
    
    // カテゴリ別の部品数カウント
    long countByCategoryId(Long categoryId);

    // 特定カテゴリの部品存在チェック
    boolean existsByCategoryId(Long categoryId);
 // ========================================
    // Phase 8.3 Step 6-1で追加: 高度検索機能
    // ========================================
    
    /**
     * 高度検索機能 - 検索結果件数取得
     * 複数の検索条件を組み合わせた検索の件数を取得
     */
    @Query("SELECT COUNT(ap) FROM AutomativePart ap " +
           "LEFT JOIN ap.category c " +
           "WHERE (:partNumber IS NULL OR LOWER(ap.partNumber) LIKE LOWER(CONCAT('%', :partNumber, '%'))) " +
           "AND (:partName IS NULL OR LOWER(ap.partName) LIKE LOWER(CONCAT('%', :partName, '%'))) " +
           "AND (:manufacturer IS NULL OR LOWER(ap.manufacturer) LIKE LOWER(CONCAT('%', :manufacturer, '%'))) " +
           "AND (:categoryId IS NULL OR ap.category.id = :categoryId) " +
           "AND (:categoryName IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :categoryName, '%'))) " +
           "AND (:minPrice IS NULL OR ap.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR ap.price <= :maxPrice) " +
           "AND (:createdAfter IS NULL OR ap.createdAt >= :createdAfter) " +
           "AND (:createdBefore IS NULL OR ap.createdAt <= :createdBefore) " +
           "AND (:updatedAfter IS NULL OR ap.updatedAt >= :updatedAfter) " +
           "AND (:updatedBefore IS NULL OR ap.updatedAt <= :updatedBefore)")
    long countByAdvancedCriteria(@Param("partNumber") String partNumber,
                                 @Param("partName") String partName,
                                 @Param("manufacturer") String manufacturer,
                                 @Param("categoryId") Long categoryId,
                                 @Param("categoryName") String categoryName,
                                 @Param("minPrice") BigDecimal minPrice,
                                 @Param("maxPrice") BigDecimal maxPrice,
                                 @Param("createdAfter") LocalDateTime createdAfter,
                                 @Param("createdBefore") LocalDateTime createdBefore,
                                 @Param("updatedAfter") LocalDateTime updatedAfter,
                                 @Param("updatedBefore") LocalDateTime updatedBefore);

    /**
     * 高度検索機能 - データ取得（ページネーション対応）
     * 複数の検索条件を組み合わせた検索でデータを取得
     */
    @Query("SELECT ap FROM AutomativePart ap " +
           "LEFT JOIN FETCH ap.category c " +
           "WHERE (:partNumber IS NULL OR LOWER(ap.partNumber) LIKE LOWER(CONCAT('%', :partNumber, '%'))) " +
           "AND (:partName IS NULL OR LOWER(ap.partName) LIKE LOWER(CONCAT('%', :partName, '%'))) " +
           "AND (:manufacturer IS NULL OR LOWER(ap.manufacturer) LIKE LOWER(CONCAT('%', :manufacturer, '%'))) " +
           "AND (:categoryId IS NULL OR ap.category.id = :categoryId) " +
           "AND (:categoryName IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :categoryName, '%'))) " +
           "AND (:minPrice IS NULL OR ap.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR ap.price <= :maxPrice) " +
           "AND (:createdAfter IS NULL OR ap.createdAt >= :createdAfter) " +
           "AND (:createdBefore IS NULL OR ap.createdAt <= :createdBefore) " +
           "AND (:updatedAfter IS NULL OR ap.updatedAt >= :updatedAfter) " +
           "AND (:updatedBefore IS NULL OR ap.updatedAt <= :updatedBefore)")
    Page<AutomativePart> findByAdvancedCriteria(@Param("partNumber") String partNumber,
                                               @Param("partName") String partName,
                                               @Param("manufacturer") String manufacturer,
                                               @Param("categoryId") Long categoryId,
                                               @Param("categoryName") String categoryName,
                                               @Param("minPrice") BigDecimal minPrice,
                                               @Param("maxPrice") BigDecimal maxPrice,
                                               @Param("createdAfter") LocalDateTime createdAfter,
                                               @Param("createdBefore") LocalDateTime createdBefore,
                                               @Param("updatedAfter") LocalDateTime updatedAfter,
                                               @Param("updatedBefore") LocalDateTime updatedBefore,
                                               Pageable pageable);

    /**
     * 高度検索機能 - 動的ソート対応メソッド
     * AdvancedSearchCriteriaオブジェクトを受け取り、適切にソートして検索実行
     */
    default Page<AutomativePart> findByAdvancedCriteriaWithSort(AdvancedSearchCriteria criteria) {
        // デフォルト設定の適用
        criteria.setDefaultSort();
        criteria.setDefaultPagination();
        
        // 並び替え条件の構築
        Sort sort = Sort.unsorted();
        if (criteria.getSortBy() != null && !criteria.getSortBy().trim().isEmpty()) {
            Sort.Direction direction = "ASC".equalsIgnoreCase(criteria.getSortOrder()) 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
            
            // 並び替えフィールドのマッピング（エイリアス対応）
            String sortField = mapSortField(criteria.getSortBy());
            sort = Sort.by(direction, sortField);
        }
        
        // ページネーション設定
        Pageable pageable = (Pageable) PageRequest.of(criteria.getPage(), criteria.getSize(), sort);
        
        // 検索実行
        return findByAdvancedCriteria(
            criteria.getPartNumber(),
            criteria.getPartName(),
            criteria.getManufacturer(),
            criteria.getCategoryId(),
            criteria.getCategoryName(),
            criteria.getMinPrice(),
            criteria.getMaxPrice(),
            criteria.getCreatedAfterAsDateTime(),      // ← LocalDateTime型
            criteria.getCreatedBeforeAsDateTime(),     // ← LocalDateTime型
            criteria.getUpdatedAfterAsDateTime(),      // ← LocalDateTime型
            criteria.getUpdatedBeforeAsDateTime(),     // ← LocalDateTime型
            pageable
        );
    }

    /**
     * ソートフィールドのマッピング処理
     * フロントエンドから送られてくるフィールド名をエンティティのフィールド名にマッピング
     */
    default String mapSortField(String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "partnumber":
            case "part_number":
                return "partNumber";
            case "partname":
            case "part_name":
                return "partName";
            case "manufacturer":
                return "manufacturer";
            case "price":
                return "price";
            case "categoryname":
            case "category_name":
                return "category.name";
            case "createdat":
            case "created_at":
                return "createdAt";
            case "updatedat":
            case "updated_at":
                return "updatedAt";
            default:
                return "updatedAt"; // デフォルトは更新日時でソート
        }
    }

    /**
     * 統計情報用 - カテゴリ別部品数取得
     * Phase 8.3 Step 6-2で使用予定
     */
    @Query("SELECT c.name, COUNT(ap) FROM AutomativePart ap " +
           "RIGHT JOIN ap.category c " +
           "GROUP BY c.id, c.name " +
           "ORDER BY COUNT(ap) DESC")
    List<Object[]> countPartsByCategory();

    /**
     * 統計情報用 - メーカー別部品数取得
     * Phase 8.3 Step 6-2で使用予定
     */
    @Query("SELECT ap.manufacturer, COUNT(ap) FROM AutomativePart ap " +
           "GROUP BY ap.manufacturer " +
           "ORDER BY COUNT(ap) DESC")
    List<Object[]> countPartsByManufacturer();

    /**
     * 統計情報用 - 価格帯別部品数取得
     * Phase 8.3 Step 6-2で使用予定
     */
    @Query("SELECT " +
           "CASE " +
           "  WHEN ap.price < 1000 THEN '1000円未満' " +
           "  WHEN ap.price < 5000 THEN '1000-5000円' " +
           "  WHEN ap.price < 10000 THEN '5000-10000円' " +
           "  WHEN ap.price < 50000 THEN '10000-50000円' " +
           "  ELSE '50000円以上' " +
           "END as priceRange, " +
           "COUNT(ap) " +
           "FROM AutomativePart ap " +
           "GROUP BY " +
           "CASE " +
           "  WHEN ap.price < 1000 THEN '1000円未満' " +
           "  WHEN ap.price < 5000 THEN '1000-5000円' " +
           "  WHEN ap.price < 10000 THEN '5000-10000円' " +
           "  WHEN ap.price < 50000 THEN '10000-50000円' " +
           "  ELSE '50000円以上' " +
           "END " +
           "ORDER BY MIN(ap.price)")
    List<Object[]> countPartsByPriceRange();
}