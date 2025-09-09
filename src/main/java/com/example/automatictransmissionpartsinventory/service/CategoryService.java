package com.example.automatictransmissionpartsinventory.service;

import java.util.List;
import java.util.Optional;

import com.example.automatictransmissionpartsinventory.entity.Category;

/**
 * カテゴリサービスインターフェース
 * AT部品カテゴリのビジネスロジック層
 * 
 * @author Phase 8.3 Development Team
 * @version 1.0
 * @since 2025-09-05
 */
public interface CategoryService {

    // ========================================
    // CRUD操作
    // ========================================
    
    /**
     * 全カテゴリを取得
     * @return 全カテゴリリスト（階層順）
     */
    List<Category> findAll();
    
    /**
     * IDによるカテゴリ取得
     * @param id カテゴリID
     * @return カテゴリ（存在しない場合はEmpty）
     */
    Optional<Category> findById(Long id);
    
    /**
     * カテゴリ名による検索
     * @param name カテゴリ名
     * @return カテゴリ（存在しない場合はEmpty）
     */
    Optional<Category> findByName(String name);
    
    /**
     * カテゴリを保存（新規作成・更新）
     * @param category カテゴリエンティティ
     * @return 保存されたカテゴリ
     */
    Category save(Category category);
    
    /**
     * カテゴリを削除
     * @param id カテゴリID
     * @throws IllegalArgumentException 削除不可の場合
     */
    void deleteById(Long id);
    
    // ========================================
    // 階層構造管理
    // ========================================
    
    /**
     * 大分類カテゴリを取得
     * @return 大分類カテゴリリスト
     */
    List<Category> findParentCategories();
    
    /**
     * 有効な大分類カテゴリを取得
     * @return 有効な大分類カテゴリリスト
     */
    List<Category> findActiveParentCategories();
    
    /**
     * 指定した親カテゴリの子カテゴリを取得
     * @param parentId 親カテゴリID
     * @return 子カテゴリリスト
     */
    List<Category> findChildCategories(Long parentId);
    
    /**
     * 階層構造のカテゴリツリーを取得
     * @return カテゴリツリー
     */
    List<Category> findCategoryTree();
    
    // ========================================
    // 検索・フィルタリング
    // ========================================
    
    /**
     * 有効なカテゴリのみを取得
     * @return 有効なカテゴリリスト
     */
    List<Category> findActiveCategories();
    
    /**
     * キーワードによるカテゴリ検索
     * @param keyword 検索キーワード
     * @return 検索結果カテゴリリスト
     */
    List<Category> searchCategories(String keyword);
    
    /**
     * 部品が割り当てられているカテゴリを取得
     * @return 部品割り当て済みカテゴリリスト
     */
    List<Category> findCategoriesWithParts();
    
    /**
     * 削除可能なカテゴリを取得
     * @return 削除可能カテゴリリスト
     */
    List<Category> findDeletableCategories();
    
    // ========================================
    // 統計・集計
    // ========================================
    
    /**
     * カテゴリ統計情報を取得
     * @return 統計情報マップ
     */
    CategoryStatistics getCategoryStatistics();
    
    /**
     * カテゴリが削除可能かどうかを判定
     * @param id カテゴリID
     * @return 削除可能な場合true
     */
    boolean isDeletable(Long id);
    
    /**
     * カテゴリ名の重複チェック
     * @param name カテゴリ名
     * @param excludeId 除外するカテゴリID（更新時に使用）
     * @return 重複している場合true
     */
    boolean isNameExists(String name, Long excludeId);
    
    // ========================================
    // 内部クラス：統計情報
    // ========================================
    
    /**
     * カテゴリ統計情報を格納するレコードクラス
     */
    public static record CategoryStatistics(
        long totalCategories,           // 全カテゴリ数
        long parentCategories,          // 大分類カテゴリ数
        long childCategories,           // 小分類カテゴリ数
        long categoriesWithParts,       // 部品割り当て済みカテゴリ数
        long categoriesWithoutParts,    // 部品未割り当てカテゴリ数
        long deletableCategories        // 削除可能カテゴリ数
    ) {}
}