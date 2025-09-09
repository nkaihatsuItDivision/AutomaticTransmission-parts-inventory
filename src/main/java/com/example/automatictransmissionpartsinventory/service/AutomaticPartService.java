package com.example.automatictransmissionpartsinventory.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;

import com.example.automatictransmissionpartsinventory.dto.AdvancedSearchCriteria;
import com.example.automatictransmissionpartsinventory.entity.AutomativePart;
import com.example.automatictransmissionpartsinventory.exception.ServiceException;

/**
 * AT部品管理サービスインターフェース
 * ビジネスロジックの抽象化
 */
public interface AutomaticPartService {

    /**
     * 新しいAT部品を登録
     * @param automativePart 登録するAT部品データ
     * @return 登録後のAT部品（IDが設定された状態）
     * @throws ServiceException 登録処理エラー時
     * @throws com.example.automatictransmissionpartsinventory.exception.ServiceException 
     */
    AutomativePart registerPart(AutomativePart automativePart) throws ServiceException, com.example.automatictransmissionpartsinventory.exception.ServiceException;

    /**
     * AT部品情報を更新
     * @param id 更新対象のID
     * @param updatedPart 更新するAT部品データ
     * @return 更新後のAT部品
     * @throws ServiceException 更新処理エラー時
     * @throws com.example.automatictransmissionpartsinventory.exception.ServiceException 
     */
    AutomativePart updatePart(Long id, AutomativePart updatedPart) throws ServiceException, com.example.automatictransmissionpartsinventory.exception.ServiceException;

    /**
     * AT部品を削除
     * @param id 削除対象のID
     * @throws ServiceException 削除処理エラー時
     * @throws com.example.automatictransmissionpartsinventory.exception.ServiceException 
     */
    void deletePart(Long id) throws ServiceException, com.example.automatictransmissionpartsinventory.exception.ServiceException;

    /**
     * IDでAT部品を検索
     * @param id 検索対象のID
     * @return AT部品（見つからない場合はOptional.empty()）
     */
    Optional<AutomativePart> findById(Long id);

    /**
     * すべてのAT部品を取得
     * @return AT部品リスト
     */
    List<AutomativePart> findAllParts();

    /**
     * 部品番号でAT部品を検索
     * @param partNumber 部品番号
     * @return AT部品（見つからない場合はOptional.empty()）
     */
    Optional<AutomativePart> findByPartNumber(String partNumber);

    /**
     * 部品名の部分一致検索
     * @param partName 部品名（部分一致）
     * @return 該当するAT部品リスト
     */
    List<AutomativePart> findByPartNameContaining(String partName);

    /**
     * 製造者でAT部品を検索
     * @param manufacturer 製造者名
     * @return 該当するAT部品リスト
     */
    List<AutomativePart> findByManufacturer(String manufacturer);

    /**
     * 価格範囲でAT部品を検索
     * @param minPrice 最低価格
     * @param maxPrice 最高価格
     * @return 該当するAT部品リスト
     */
    List<AutomativePart> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * 複合条件でAT部品を検索
     * @param partName 部品名（部分一致、nullの場合は条件に含めない）
     * @param manufacturer 製造者（完全一致、nullの場合は条件に含めない）
     * @param minPrice 最低価格（nullの場合は条件に含めない）
     * @param maxPrice 最高価格（nullの場合は条件に含めない）
     * @param categoryId 
     * @return 該当するAT部品リスト
     */
    List<AutomativePart> findByConditions(String partName, String manufacturer, 
            BigDecimal minPrice, BigDecimal maxPrice, 
            Long categoryId);

    /**
     * 部品番号の重複チェック
     * @param partNumber チェック対象の部品番号
     * @param excludeId 除外するID（更新時に自分自身を除外するため、nullの場合は新規登録）
     * @return 重複している場合true
     */
    boolean isPartNumberDuplicated(String partNumber, Long excludeId);
 // ========================================
    // Phase 8.3 Step 6-1で追加: 高度検索機能
    // ========================================
    
    /**
     * 高度検索機能
     * 複数の検索条件を組み合わせた検索を実行
     * 
     * @param criteria 検索条件
     * @return 検索結果（ページネーション対応）
     */
    Page<AutomativePart> searchByAdvancedCriteria(AdvancedSearchCriteria criteria) throws ServiceException;
    
    /**
     * 高度検索の検索結果件数取得
     * 
     * @param criteria 検索条件
     * @return 検索結果の総件数
     */
    long countByAdvancedCriteria(AdvancedSearchCriteria criteria) throws ServiceException;
    
    /**
     * 検索条件の妥当性チェック
     * 
     * @param criteria 検索条件
     * @return 妥当性チェック結果
     */
    Map<String, String> validateSearchCriteria(AdvancedSearchCriteria criteria) throws ServiceException;
    
    /**
     * 検索統計情報の取得
     * 
     * @param criteria 検索条件
     * @return 検索統計情報
     */
    Map<String, Object> getSearchStatistics(AdvancedSearchCriteria criteria) throws ServiceException;
}