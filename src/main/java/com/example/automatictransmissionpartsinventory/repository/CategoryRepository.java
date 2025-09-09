package com.example.automatictransmissionpartsinventory.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.automatictransmissionpartsinventory.entity.Category;

/**
 * カテゴリリポジトリインターフェース
 * AT部品カテゴリのデータアクセス層
 * 
 * @author Phase 8.3 Development Team
 * @version 1.0
 * @since 2025-09-05
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // ========================================
    // 基本的な検索メソッド
    // ========================================
    
    /**
     * カテゴリ名による検索（完全一致）
     * @param name カテゴリ名
     * @return カテゴリ（存在しない場合はEmpty）
     */
    Optional<Category> findByName(String name);
    
    /**
     * カテゴリ名による存在確認
     * @param name カテゴリ名
     * @return 存在する場合true
     */
    boolean existsByName(String name);
    
    /**
     * 有効なカテゴリのみを取得
     * @param isActive 有効フラグ
     * @return 有効なカテゴリリスト
     */
    List<Category> findByIsActiveOrderByDisplayOrder(Boolean isActive);
    
    // ========================================
    // 階層構造関連の検索メソッド
    // ========================================
    
    /**
     * 大分類カテゴリ（親カテゴリ）を取得
     * @return 大分類カテゴリリスト（表示順でソート）
     */
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL ORDER BY c.displayOrder")
    List<Category> findParentCategories();
    
    /**
     * 有効な大分類カテゴリのみを取得
     * @return 有効な大分類カテゴリリスト
     */
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.isActive = true ORDER BY c.displayOrder")
    List<Category> findActiveParentCategories();
    
    /**
     * 指定した親カテゴリの子カテゴリを取得
     * @param parent 親カテゴリ
     * @return 子カテゴリリスト（表示順でソート）
     */
    List<Category> findByParentOrderByDisplayOrder(Category parent);
    
    /**
     * 指定した親カテゴリIDの子カテゴリを取得
     * @param parentId 親カテゴリID
     * @return 子カテゴリリスト
     */
    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId ORDER BY c.displayOrder")
    List<Category> findByParentId(@Param("parentId") Long parentId);
    
    /**
     * 指定した親カテゴリの有効な子カテゴリを取得
     * @param parent 親カテゴリ
     * @return 有効な子カテゴリリスト
     */
    @Query("SELECT c FROM Category c WHERE c.parent = :parent AND c.isActive = true ORDER BY c.displayOrder")
    List<Category> findActiveChildCategories(@Param("parent") Category parent);
    
    // ========================================
    // 統計・集計関連メソッド
    // ========================================
    
    /**
     * 大分類カテゴリ数をカウント
     * @return 大分類カテゴリ数
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.parent IS NULL")
    long countParentCategories();
    
    /**
     * 小分類カテゴリ数をカウント
     * @return 小分類カテゴリ数
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.parent IS NOT NULL")
    long countChildCategories();
    
    /**
     * 指定した親カテゴリの子カテゴリ数をカウント
     * @param parent 親カテゴリ
     * @return 子カテゴリ数
     */
    long countByParent(Category parent);
    
    /**
     * 部品が割り当てられているカテゴリ数をカウント
     * @return 部品が割り当てられているカテゴリ数
     */
    @Query("SELECT COUNT(DISTINCT c) FROM Category c JOIN c.automotiveParts ap")
    long countCategoriesWithParts();
    
    // ========================================
    // 部品関連の検索メソッド
    // ========================================
    
    /**
     * 部品が割り当てられているカテゴリを取得
     * @return 部品が割り当てられているカテゴリリスト
     */
    @Query("SELECT DISTINCT c FROM Category c JOIN c.automotiveParts ap ORDER BY c.displayOrder")
    List<Category> findCategoriesWithParts();
    
    /**
     * 部品が割り当てられていないカテゴリを取得
     * @return 部品が割り当てられていないカテゴリリスト
     */
    @Query("SELECT c FROM Category c WHERE c.id NOT IN (SELECT DISTINCT ap.category.id FROM AutomativePart ap WHERE ap.category IS NOT NULL) ORDER BY c.displayOrder")
    List<Category> findCategoriesWithoutParts();
    
    // ========================================
    // カスタム検索メソッド
    // ========================================
    
    /**
     * カテゴリ名による部分一致検索
     * @param keyword 検索キーワード
     * @return 部分一致するカテゴリリスト
     */
    @Query("SELECT c FROM Category c WHERE c.name LIKE %:keyword% ORDER BY c.displayOrder")
    List<Category> findByNameContaining(@Param("keyword") String keyword);
    
    /**
     * カテゴリ名または説明による部分一致検索
     * @param keyword 検索キーワード
     * @return 部分一致するカテゴリリスト
     */
    @Query("SELECT c FROM Category c WHERE c.name LIKE %:keyword% OR c.description LIKE %:keyword% ORDER BY c.displayOrder")
    List<Category> findByNameOrDescriptionContaining(@Param("keyword") String keyword);
    
    /**
     * 表示順による範囲検索
     * @param minOrder 最小表示順
     * @param maxOrder 最大表示順
     * @return 指定範囲の表示順のカテゴリリスト
     */
    List<Category> findByDisplayOrderBetweenOrderByDisplayOrder(Integer minOrder, Integer maxOrder);
    
    /**
     * 階層構造での完全なカテゴリツリーを取得
     * 親カテゴリと子カテゴリを一度に取得（N+1問題回避）
     * @return 階層構造のカテゴリリスト
     */
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE c.parent IS NULL ORDER BY c.displayOrder")
    List<Category> findCategoryTreeWithChildren();
    
    /**
     * 削除可能なカテゴリを検索
     * 条件：子カテゴリなし かつ 部品割り当てなし
     * @return 削除可能なカテゴリリスト
     */
    @Query("SELECT c FROM Category c WHERE c.id NOT IN (SELECT DISTINCT c2.parent.id FROM Category c2 WHERE c2.parent IS NOT NULL) AND c.id NOT IN (SELECT DISTINCT ap.category.id FROM AutomativePart ap WHERE ap.category IS NOT NULL)")
    List<Category> findDeletableCategories();
}