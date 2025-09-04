package com.example.automatictransmissionpartsinventory.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
    
    // 複合検索（部品名 + 製造者）
    @Query("SELECT ap FROM AutomativePart ap WHERE " +
           "(:partName IS NULL OR ap.partName LIKE %:partName%) AND " +
           "(:manufacturer IS NULL OR ap.manufacturer = :manufacturer) AND " +
           "(:minPrice IS NULL OR ap.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR ap.price <= :maxPrice)")
    List<AutomativePart> findByConditions(
            @Param("partName") String partName,
            @Param("manufacturer") String manufacturer,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );
    
    // 部品番号の重複チェック
    boolean existsByPartNumber(String partNumber);
}