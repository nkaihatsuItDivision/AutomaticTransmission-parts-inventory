package com.example.automatictransmissionpartsinventory.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.automatictransmissionpartsinventory.entity.Category;
import com.example.automatictransmissionpartsinventory.repository.CategoryRepository;
import com.example.automatictransmissionpartsinventory.service.CategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * カテゴリサービス実装クラス
 * AT部品カテゴリのビジネスロジック実装
 * 
 * @author Phase 8.3 Development Team
 * @version 1.0
 * @since 2025-09-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    // ========================================
    // CRUD操作
    // ========================================
    
    @Override
    public List<Category> findAll() {
        log.debug("全カテゴリを取得中...");
        List<Category> categories = categoryRepository.findAll();
        log.info("全カテゴリ取得完了: {}件", categories.size());
        return categories;
    }
    
    @Override
    public Optional<Category> findById(Long id) {
        log.debug("カテゴリをIDで検索中: {}", id);
        
        if (id == null) {
            log.warn("カテゴリIDが nullです");
            return Optional.empty();
        }
        
        Optional<Category> category = categoryRepository.findById(id);
        
        if (category.isPresent()) {
            log.debug("カテゴリが見つかりました: {}", category.get().getName());
        } else {
            log.warn("カテゴリが見つかりません: ID={}", id);
        }
        
        return category;
    }
    
    @Override
    public Optional<Category> findByName(String name) {
        log.debug("カテゴリを名前で検索中: {}", name);
        
        if (name == null || name.trim().isEmpty()) {
            log.warn("カテゴリ名が空です");
            return Optional.empty();
        }
        
        return categoryRepository.findByName(name.trim());
    }
    
    @Override
    @Transactional
    public Category save(Category category) {
        log.debug("カテゴリを保存中: {}", category.getName());
        
        // バリデーション
        validateCategory(category);
        
        // 名前の重複チェック
        if (isNameExists(category.getName(), category.getId())) {
            throw new IllegalArgumentException("カテゴリ名が重複しています: " + category.getName());
        }
        
        Category savedCategory = categoryRepository.save(category);
        
        log.info("カテゴリ保存完了: ID={}, Name={}", savedCategory.getId(), savedCategory.getName());
        return savedCategory;
    }
    
    @Override
    @Transactional
    public void deleteById(Long id) {
        log.debug("カテゴリを削除中: ID={}", id);
        
        if (id == null) {
            throw new IllegalArgumentException("カテゴリIDが nullです");
        }
        
        // 削除可能性チェック
        if (!isDeletable(id)) {
            throw new IllegalArgumentException("このカテゴリは削除できません（子カテゴリまたは部品が存在します）: ID=" + id);
        }
        
        Optional<Category> category = findById(id);
        if (category.isEmpty()) {
            throw new IllegalArgumentException("削除対象のカテゴリが見つかりません: ID=" + id);
        }
        
        categoryRepository.deleteById(id);
        log.info("カテゴリ削除完了: ID={}, Name={}", id, category.get().getName());
    }
    
    // ========================================
    // 階層構造管理
    // ========================================
    
    @Override
    public List<Category> findParentCategories() {
        log.debug("大分類カテゴリを取得中...");
        List<Category> parentCategories = categoryRepository.findParentCategories();
        log.info("大分類カテゴリ取得完了: {}件", parentCategories.size());
        return parentCategories;
    }
    
    @Override
    public List<Category> findActiveParentCategories() {
        log.debug("有効な大分類カテゴリを取得中...");
        List<Category> activeParents = categoryRepository.findActiveParentCategories();
        log.info("有効な大分類カテゴリ取得完了: {}件", activeParents.size());
        return activeParents;
    }
    
    @Override
    public List<Category> findChildCategories(Long parentId) {
        log.debug("子カテゴリを取得中: 親ID={}", parentId);
        
        if (parentId == null) {
            log.warn("親カテゴリIDが nullです");
            return List.of();
        }
        
        List<Category> childCategories = categoryRepository.findByParentId(parentId);
        log.info("子カテゴリ取得完了: 親ID={}, 子数={}", parentId, childCategories.size());
        return childCategories;
    }
    
    @Override
    public List<Category> findCategoryTree() {
        log.debug("カテゴリツリーを取得中...");
        
        // 大分類を取得
        List<Category> parentCategories = categoryRepository.findParentCategories();
        
        // 各大分類に対して子カテゴリを設定
        for (Category parent : parentCategories) {
            List<Category> children = categoryRepository.findByParentOrderByDisplayOrder(parent);
            parent.setChildren(children);
        }
        
        log.info("カテゴリツリー取得完了: {}件の大分類", parentCategories.size());
        return parentCategories;
    }
    
    // ========================================
    // 検索・フィルタリング
    // ========================================
    
    @Override
    public List<Category> findActiveCategories() {
        log.debug("有効なカテゴリを取得中...");
        List<Category> activeCategories = categoryRepository.findByIsActiveOrderByDisplayOrder(true);
        log.info("有効なカテゴリ取得完了: {}件", activeCategories.size());
        return activeCategories;
    }
    
    @Override
    public List<Category> searchCategories(String keyword) {
        log.debug("カテゴリを検索中: キーワード={}", keyword);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            log.debug("キーワードが空のため、全カテゴリを返します");
            return findAll();
        }
        
        String trimmedKeyword = keyword.trim();
        List<Category> searchResults = categoryRepository.findByNameOrDescriptionContaining(trimmedKeyword);
        log.info("カテゴリ検索完了: キーワード={}, 結果数={}", trimmedKeyword, searchResults.size());
        return searchResults;
    }
    
    @Override
    public List<Category> findCategoriesWithParts() {
        log.debug("部品が割り当てられているカテゴリを取得中...");
        List<Category> categoriesWithParts = categoryRepository.findCategoriesWithParts();
        log.info("部品割り当て済みカテゴリ取得完了: {}件", categoriesWithParts.size());
        return categoriesWithParts;
    }
    
    @Override
    public List<Category> findDeletableCategories() {
        log.debug("削除可能なカテゴリを取得中...");
        List<Category> deletableCategories = categoryRepository.findDeletableCategories();
        log.info("削除可能カテゴリ取得完了: {}件", deletableCategories.size());
        return deletableCategories;
    }
    
    // ========================================
    // 統計・集計
    // ========================================
    
    @Override
    public CategoryStatistics getCategoryStatistics() {
        log.debug("カテゴリ統計情報を取得中...");
        
        long totalCategories = categoryRepository.count();
        long parentCategories = categoryRepository.countParentCategories();
        long childCategories = categoryRepository.countChildCategories();
        long categoriesWithParts = categoryRepository.countCategoriesWithParts();
        long categoriesWithoutParts = totalCategories - categoriesWithParts;
        long deletableCategories = categoryRepository.findDeletableCategories().size();
        
        CategoryStatistics statistics = new CategoryStatistics(
            totalCategories,
            parentCategories,
            childCategories,
            categoriesWithParts,
            categoriesWithoutParts,
            deletableCategories
        );
        
        log.info("カテゴリ統計情報取得完了: {}", statistics);
        return statistics;
    }
    
    @Override
    public boolean isDeletable(Long id) {
        log.debug("カテゴリの削除可能性をチェック中: ID={}", id);
        
        if (id == null) {
            return false;
        }
        
        Optional<Category> categoryOpt = findById(id);
        if (categoryOpt.isEmpty()) {
            return false;
        }
        
        Category category = categoryOpt.get();
        boolean deletable = category.isDeletable();
        
        log.debug("削除可能性チェック結果: ID={}, 削除可能={}", id, deletable);
        return deletable;
    }
    
    @Override
    public boolean isNameExists(String name, Long excludeId) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        Optional<Category> existingCategory = categoryRepository.findByName(name.trim());
        
        if (existingCategory.isEmpty()) {
            return false;
        }
        
        // 更新時は自分自身のIDを除外
        if (excludeId != null && existingCategory.get().getId().equals(excludeId)) {
            return false;
        }
        
        return true;
    }
    
    // ========================================
    // プライベートメソッド
    // ========================================
    
    /**
     * カテゴリのバリデーション
     * @param category バリデーション対象カテゴリ
     * @throws IllegalArgumentException バリデーションエラーの場合
     */
    private void validateCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("カテゴリが nullです");
        }
        
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("カテゴリ名は必須です");
        }
        
        if (category.getName().length() > 100) {
            throw new IllegalArgumentException("カテゴリ名は100文字以内で入力してください");
        }
        
        // 階層構造のバリデーション（3階層以上は禁止）
        if (category.getParent() != null && category.getParent().getParent() != null) {
            throw new IllegalArgumentException("3階層以上のカテゴリは作成できません");
        }
        
        // 自己参照チェック
        if (category.getParent() != null && 
            category.getId() != null && 
            category.getId().equals(category.getParent().getId())) {
            throw new IllegalArgumentException("自分自身を親カテゴリに設定することはできません");
        }
        
        log.debug("カテゴリバリデーション完了: {}", category.getName());
    }
}