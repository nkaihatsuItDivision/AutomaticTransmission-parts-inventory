package com.example.automatictransmissionpartsinventory.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
                                               BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("複合条件検索: 部品名={}, 製造者={}, 価格範囲={}-{}",
                  partName, manufacturer, minPrice, maxPrice);

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
}