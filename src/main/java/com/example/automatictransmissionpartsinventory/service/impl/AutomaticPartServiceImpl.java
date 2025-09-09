package com.example.automatictransmissionpartsinventory.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.automatictransmissionpartsinventory.dto.AdvancedSearchCriteria;
import com.example.automatictransmissionpartsinventory.entity.AutomativePart;
import com.example.automatictransmissionpartsinventory.exception.ServiceException;
import com.example.automatictransmissionpartsinventory.repository.AutomaticPartRepository;
import com.example.automatictransmissionpartsinventory.service.AutomaticPartService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AT部品管理サービス実装クラス
 * ビジネスロジックの具体的な実装
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AutomaticPartServiceImpl implements AutomaticPartService {

    private final AutomaticPartRepository automaticPartRepository;

    @Override
    public AutomativePart registerPart(AutomativePart automativePart) throws ServiceException {
        log.info("AT部品登録開始: {}", automativePart.getPartNumber());
        
        try {
            // バリデーション
            validatePartData(automativePart, null);
            
            // 部品番号重複チェック
            if (isPartNumberDuplicated(automativePart.getPartNumber(), null)) {
                throw new ServiceException(
                    "部品番号 '" + automativePart.getPartNumber() + "' は既に登録されています。",
                    ServiceException.DUPLICATE_PART_NUMBER
                );
            }
            
            // 登録実行
            AutomativePart savedPart = automaticPartRepository.save(automativePart);
            
            log.info("AT部品登録完了: ID={}, 部品番号={}", savedPart.getId(), savedPart.getPartNumber());
            return savedPart;
            
        } catch (DataAccessException e) {
            log.error("AT部品登録中にデータベースエラーが発生: {}", e.getMessage());
            throw new ServiceException(
                "AT部品の登録中にエラーが発生しました。",
                ServiceException.DATABASE_ERROR,
                e
            );
        }
    }

    @Override
    public AutomativePart updatePart(Long id, AutomativePart updatedPart) throws ServiceException {
        log.info("AT部品更新開始: ID={}", id);
        
        try {
            // 既存データの存在確認
            Optional<AutomativePart> existingPartOpt = automaticPartRepository.findById(id);
            if (existingPartOpt.isEmpty()) {
                throw new ServiceException(
                    "ID " + id + " のAT部品が見つかりません。",
                    ServiceException.PART_NOT_FOUND
                );
            }
            
            AutomativePart existingPart = existingPartOpt.get();
            
            // バリデーション
            validatePartData(updatedPart, id);
            
            // 部品番号重複チェック（自分以外で同じ部品番号がないか）
            if (isPartNumberDuplicated(updatedPart.getPartNumber(), id)) {
                throw new ServiceException(
                    "部品番号 '" + updatedPart.getPartNumber() + "' は既に他のAT部品で使用されています。",
                    ServiceException.DUPLICATE_PART_NUMBER
                );
            }
            
            // データ更新
            existingPart.setPartNumber(updatedPart.getPartNumber());
            existingPart.setPartName(updatedPart.getPartName());
            existingPart.setPrice(updatedPart.getPrice());
            existingPart.setDescription(updatedPart.getDescription());
            existingPart.setManufacturer(updatedPart.getManufacturer());
            existingPart.setCategory(updatedPart.getCategory());
            
            // 更新実行
            AutomativePart savedPart = automaticPartRepository.save(existingPart);
            
            log.info("AT部品更新完了: ID={}, 部品番号={}", savedPart.getId(), savedPart.getPartNumber());
            return savedPart;
            
        } catch (DataAccessException e) {
            log.error("AT部品更新中にデータベースエラーが発生: {}", e.getMessage());
            throw new ServiceException(
                "AT部品の更新中にエラーが発生しました。",
                ServiceException.DATABASE_ERROR,
                e
            );
        }
    }

    @Override
    public void deletePart(Long id) throws ServiceException {
        log.info("AT部品削除開始: ID={}", id);
        
        try {
            // 存在確認
            if (!automaticPartRepository.existsById(id)) {
                throw new ServiceException(
                    "ID " + id + " のAT部品が見つかりません。",
                    ServiceException.PART_NOT_FOUND
                );
            }
            
            // 削除実行
            automaticPartRepository.deleteById(id);
            
            log.info("AT部品削除完了: ID={}", id);
            
        } catch (DataAccessException e) {
            log.error("AT部品削除中にデータベースエラーが発生: {}", e.getMessage());
            throw new ServiceException(
                "AT部品の削除中にエラーが発生しました。",
                ServiceException.DATABASE_ERROR,
                e
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AutomativePart> findById(Long id) {
        log.debug("AT部品ID検索: ID={}", id);
        return automaticPartRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AutomativePart> findAllParts() {
        log.debug("全AT部品取得");
        return automaticPartRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AutomativePart> findByPartNumber(String partNumber) {
        log.debug("AT部品番号検索: {}", partNumber);
        if (!StringUtils.hasText(partNumber)) {
            return Optional.empty();
        }
        return automaticPartRepository.findByPartNumber(partNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AutomativePart> findByPartNameContaining(String partName) {
        log.debug("AT部品名部分一致検索: {}", partName);
        if (!StringUtils.hasText(partName)) {
            return List.of();
        }
        return automaticPartRepository.findByPartNameContaining(partName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AutomativePart> findByManufacturer(String manufacturer) {
        log.debug("製造者検索: {}", manufacturer);
        if (!StringUtils.hasText(manufacturer)) {
            return List.of();
        }
        return automaticPartRepository.findByManufacturer(manufacturer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AutomativePart> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("価格範囲検索: {}円 - {}円", minPrice, maxPrice);
        return automaticPartRepository.findByPriceBetween(minPrice, maxPrice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AutomativePart> findByConditions(String partName, String manufacturer, 
            BigDecimal minPrice, BigDecimal maxPrice, 
            Long categoryId) {
    	log.debug("複合条件検索: 部品名={}, 製造者={}, 価格範囲={}-{}, カテゴリID={}",
    	          partName, manufacturer, minPrice, maxPrice, categoryId);

        try {
            // 全件取得
            List<AutomativePart> allParts = automaticPartRepository.findAll();
            List<AutomativePart> filteredParts = new ArrayList<>();
            
            log.debug("全部品数: {}件", allParts.size());
            
            // 条件フィルタリング
            for (AutomativePart part : allParts) {
                boolean matches = true;
                
                // 部品名での絞り込み（部分一致、大文字小文字無視）
                if (partName != null && !partName.trim().isEmpty()) {
                    if (part.getPartName() == null || 
                        !part.getPartName().toLowerCase().contains(partName.toLowerCase())) {
                        matches = false;
                        log.trace("部品名不一致: {} (検索語: {})", part.getPartName(), partName);
                    }
                }
                
                // メーカーでの絞り込み（部分一致、大文字小文字無視）
                if (matches && manufacturer != null && !manufacturer.trim().isEmpty()) {
                    if (part.getManufacturer() == null || 
                        !part.getManufacturer().toLowerCase().contains(manufacturer.toLowerCase())) {
                        matches = false;
                        log.trace("メーカー不一致: {} (検索語: {})", part.getManufacturer(), manufacturer);
                    }
                }
                
                // ★追加: カテゴリでの絞り込み
                if (matches && categoryId != null) {
                    if (part.getCategory() == null || 
                        !part.getCategory().getId().equals(categoryId)) {
                        matches = false;
                        log.trace("カテゴリ不一致: {} (検索カテゴリID: {})", 
                                 part.getCategory() != null ? part.getCategory().getId() : "null", categoryId);
                    }
                }
                
                // 最低価格での絞り込み
                if (matches && minPrice != null) {
                    if (part.getPrice() == null || part.getPrice().compareTo(minPrice) < 0) {
                        matches = false;
                        log.trace("最低価格不一致: {} (最低価格: {})", part.getPrice(), minPrice);
                    }
                }
                
                // 最高価格での絞り込み
                if (matches && maxPrice != null) {
                    if (part.getPrice() == null || part.getPrice().compareTo(maxPrice) > 0) {
                        matches = false;
                        log.trace("最高価格不一致: {} (最高価格: {})", part.getPrice(), maxPrice);
                    }
                }
                
                // すべての条件をクリアした場合に結果に追加
                if (matches) {
                    filteredParts.add(part);
                    log.trace("条件一致: 部品番号={}, 部品名={}, 価格={}", 
                             part.getPartNumber(), part.getPartName(), part.getPrice());
                }
            }
            
            log.debug("検索完了 - 該当部品数: {}件", filteredParts.size());
            return filteredParts;
            
        } catch (Exception e) {
            log.error("複合検索でエラーが発生", e);
            throw new RuntimeException("検索処理中にエラーが発生しました", e);
        }
    }
    
    
    @Override
    @Transactional(readOnly = true)
    public boolean isPartNumberDuplicated(String partNumber, Long excludeId) {
        if (!StringUtils.hasText(partNumber)) {
            return false;
        }
        
        Optional<AutomativePart> existingPart = automaticPartRepository.findByPartNumber(partNumber);
        if (existingPart.isEmpty()) {
            return false;
        }
        
        // 更新時は自分自身を除外
        if (excludeId != null && existingPart.get().getId().equals(excludeId)) {
            return false;
        }
        
        return true;
    }

    /**
     * AT部品データのバリデーション
     * @param automativePart バリデーション対象のAT部品
     * @param excludeId 更新時の除外ID
     * @throws ServiceException バリデーションエラー時
     */
    private void validatePartData(AutomativePart automativePart, Long excludeId) throws ServiceException {
        if (automativePart == null) {
            throw new ServiceException("AT部品データが指定されていません。", ServiceException.VALIDATION_ERROR);
        }
        
        // 部品番号チェック
        if (!StringUtils.hasText(automativePart.getPartNumber())) {
            throw new ServiceException("部品番号は必須です。", ServiceException.VALIDATION_ERROR);
        }
        
        // 部品名チェック
        if (!StringUtils.hasText(automativePart.getPartName())) {
            throw new ServiceException("部品名は必須です。", ServiceException.VALIDATION_ERROR);
        }
        
        // 価格チェック
        if (automativePart.getPrice() == null) {
            throw new ServiceException("価格は必須です。", ServiceException.VALIDATION_ERROR);
        }
        
        if (automativePart.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException("価格は0以上である必要があります。", ServiceException.VALIDATION_ERROR);
        }
    }
 // ========================================
    // Phase 8.3 Step 6-1で追加: 高度検索機能実装
    // ========================================

    /**
     * 高度検索機能の実装
     * 複数の検索条件を組み合わせた検索を実行
     */
    @Override
    public Page<AutomativePart> searchByAdvancedCriteria(AdvancedSearchCriteria criteria) throws ServiceException {
        try {
            log.info("高度検索を開始します。検索条件: {}", criteria);
            
            // 検索条件の妥当性チェック
            Map<String, String> validationErrors = validateSearchCriteria(criteria);
            if (!validationErrors.isEmpty()) {
                log.warn("検索条件に不正な値があります: {}", validationErrors);
                throw new ServiceException("検索条件が不正です: " + validationErrors.toString());
            }
            
            // 検索条件の前処理
            preprocessSearchCriteria(criteria);
            
            // 空の検索条件の場合は全件取得
            if (criteria.isEmpty()) {
                log.info("検索条件が空のため、全件取得を実行します");
                return getAllPartsWithPagination(criteria);
            }
            
            // 高度検索の実行
            log.info("条件指定での高度検索を実行します");
            Page<AutomativePart> results = automaticPartRepository.findByAdvancedCriteriaWithSort(criteria);
            
            log.info("高度検索が完了しました。結果件数: {}, 総ページ数: {}", 
                       results.getTotalElements(), results.getTotalPages());
            
            return results;
            
        } catch (Exception e) {
            log.error("高度検索処理中にエラーが発生しました。検索条件: {}", criteria, e);
            throw new ServiceException("検索処理に失敗しました: " + e.getMessage());
        }
    }

    /**
     * 高度検索の検索結果件数取得
     */
    @Override
    public long countByAdvancedCriteria(AdvancedSearchCriteria criteria) throws ServiceException {
        try {
            log.info("検索結果件数の取得を開始します");
            
            // 検索条件の前処理
            preprocessSearchCriteria(criteria);
            
            // 空の検索条件の場合は全件数
            if (criteria.isEmpty()) {
                long totalCount = automaticPartRepository.count();
                log.info("全件数を取得しました: {}", totalCount);
                return totalCount;
            }
            
            // 条件指定での件数取得
            long count = automaticPartRepository.countByAdvancedCriteria(
                criteria.getPartNumber(),
                criteria.getPartName(),
                criteria.getManufacturer(),
                criteria.getCategoryId(),
                criteria.getCategoryName(),
                criteria.getMinPrice(),
                criteria.getMaxPrice(),
                criteria.getCreatedAfterAsDateTime(),      // LocalDateTime型
                criteria.getCreatedBeforeAsDateTime(),     // LocalDateTime型
                criteria.getUpdatedAfterAsDateTime(),      // LocalDateTime型
                criteria.getUpdatedBeforeAsDateTime()      // LocalDateTime型
            );
            
            log.info("条件指定での検索結果件数: {}", count);
            return count;
            
        } catch (Exception e) {
            log.error("検索件数取得中にエラーが発生しました", e);
            return 0;
        }
    }

    /**
     * 検索条件の妥当性チェック（修正版）
     */
    @Override
    public Map<String, String> validateSearchCriteria(AdvancedSearchCriteria criteria) throws ServiceException {
        log.info("検索条件の妥当性チェック開始: {}", criteria);
        
        Map<String, String> errors = new HashMap<>();
        
        try {
            // 価格範囲のチェック
            if (criteria.getMinPrice() != null && criteria.getMaxPrice() != null) {
                if (criteria.getMinPrice().compareTo(criteria.getMaxPrice()) > 0) {
                    errors.put("priceRange", "最低価格は最高価格以下で入力してください");
                }
            }
            
            // 価格の妥当性チェック
            if (criteria.getMinPrice() != null && criteria.getMinPrice().compareTo(BigDecimal.ZERO) < 0) {
                errors.put("minPrice", "最低価格は0以上で入力してください");
            }
            
            if (criteria.getMaxPrice() != null && criteria.getMaxPrice().compareTo(BigDecimal.ZERO) < 0) {
                errors.put("maxPrice", "最高価格は0以上で入力してください");
            }
            
            // 日付範囲のチェック（修正：String型として処理）
            if (criteria.getCreatedAfter() != null && !criteria.getCreatedAfter().trim().isEmpty() &&
                criteria.getCreatedBefore() != null && !criteria.getCreatedBefore().trim().isEmpty()) {
                
                try {
                    LocalDate dateAfter = LocalDate.parse(criteria.getCreatedAfter());
                    LocalDate dateBefore = LocalDate.parse(criteria.getCreatedBefore());
                    
                    if (dateAfter.isAfter(dateBefore)) {
                        errors.put("dateRange", "開始日は終了日以前で入力してください");
                    }
                } catch (Exception e) {
                    errors.put("dateFormat", "日付形式が正しくありません（YYYY-MM-DD形式で入力してください）");
                }
            }
            
            // ページネーションのチェック
            if (criteria.getPage() != null && criteria.getPage() < 0) {
                errors.put("page", "ページ番号は0以上で入力してください");
            }
            
            if (criteria.getSize() != null && criteria.getSize() <= 0) {
                errors.put("size", "ページサイズは1以上で入力してください");
            }
            
            log.info("検索条件の妥当性チェック完了: エラー数={}", errors.size());
            
        } catch (Exception e) {
            log.error("検索条件の妥当性チェック中にエラーが発生", e);
            errors.put("general", "検索条件の検証中にエラーが発生しました");
        }
        
        return errors;
    }
    /**
     * 検索統計情報の取得
     */
    @Override
    public Map<String, Object> getSearchStatistics(AdvancedSearchCriteria criteria) throws ServiceException {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            // 検索実行時刻
            statistics.put("searchTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            // 検索結果件数
            long totalResults = countByAdvancedCriteria(criteria);
            statistics.put("totalResults", totalResults);
            
            // 検索条件の要約
            Map<String, Object> criteriasSummary = new HashMap<>();
            if (criteria.getPartNumber() != null && !criteria.getPartNumber().trim().isEmpty()) {
                criteriasSummary.put("partNumber", criteria.getPartNumber());
            }
            if (criteria.getPartName() != null && !criteria.getPartName().trim().isEmpty()) {
                criteriasSummary.put("partName", criteria.getPartName());
            }
            if (criteria.getManufacturer() != null && !criteria.getManufacturer().trim().isEmpty()) {
                criteriasSummary.put("manufacturer", criteria.getManufacturer());
            }
            if (criteria.getCategoryId() != null) {
                criteriasSummary.put("categoryId", criteria.getCategoryId());
            }
            if (criteria.getMinPrice() != null || criteria.getMaxPrice() != null) {
                String priceRange = "";
                if (criteria.getMinPrice() != null) {
                    priceRange += criteria.getMinPrice() + "円以上";
                }
                if (criteria.getMaxPrice() != null) {
                    if (!priceRange.isEmpty()) priceRange += " ";
                    priceRange += criteria.getMaxPrice() + "円以下";
                }
                criteriasSummary.put("priceRange", priceRange);
            }
            
            statistics.put("searchCriteria", criteriasSummary);
            
            // パフォーマンス情報
            statistics.put("isEmpty", criteria.isEmpty());
            statistics.put("hasDateFilter", criteria.getCreatedAfter() != null || criteria.getCreatedBefore() != null || 
                                           criteria.getUpdatedAfter() != null || criteria.getUpdatedBefore() != null);
            statistics.put("hasPriceFilter", criteria.getMinPrice() != null || criteria.getMaxPrice() != null);
            
        } catch (Exception e) {
            log.error("検索統計情報の取得中にエラーが発生しました", e);
            statistics.put("error", "統計情報の取得に失敗しました");
        }
        
        return statistics;
    }

    /**
     * 検索条件の前処理
     * 入力値の正規化や空文字の処理などを行う
     */
    private void preprocessSearchCriteria(AdvancedSearchCriteria criteria) {
        // 空文字をnullに変換
        if (criteria.getPartNumber() != null && criteria.getPartNumber().trim().isEmpty()) {
            criteria.setPartNumber(null);
        }
        if (criteria.getPartName() != null && criteria.getPartName().trim().isEmpty()) {
            criteria.setPartName(null);
        }
        if (criteria.getManufacturer() != null && criteria.getManufacturer().trim().isEmpty()) {
            criteria.setManufacturer(null);
        }
        if (criteria.getCategoryName() != null && criteria.getCategoryName().trim().isEmpty()) {
            criteria.setCategoryName(null);
        }
        if (criteria.getSortBy() != null && criteria.getSortBy().trim().isEmpty()) {
            criteria.setSortBy(null);
        }
        if (criteria.getSortOrder() != null && criteria.getSortOrder().trim().isEmpty()) {
            criteria.setSortOrder(null);
        }
        
        // デフォルト値の設定
        criteria.setDefaultSort();
        criteria.setDefaultPagination();
    }

    /**
     * 全件取得（ページネーション対応）
     */
    private Page<AutomativePart> getAllPartsWithPagination(AdvancedSearchCriteria criteria) {
        criteria.setDefaultSort();
        criteria.setDefaultPagination();
        
        Sort sort = Sort.unsorted();
        if (criteria.getSortBy() != null && !criteria.getSortBy().trim().isEmpty()) {
            Sort.Direction direction = "ASC".equalsIgnoreCase(criteria.getSortOrder()) 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(direction, criteria.getSortBy());
        }
        
        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), sort);
        return automaticPartRepository.findAll(pageable);
    }
}