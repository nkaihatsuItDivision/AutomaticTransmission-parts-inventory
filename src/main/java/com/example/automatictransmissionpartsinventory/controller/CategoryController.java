package com.example.automatictransmissionpartsinventory.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.automatictransmissionpartsinventory.entity.Category;
import com.example.automatictransmissionpartsinventory.service.CategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * カテゴリコントローラ
 * AT部品カテゴリ管理機能のWeb制御層
 * 
 * @author Phase 8.3 Development Team
 * @version 1.0
 * @since 2025-09-05
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
public class CategoryController {

    private final CategoryService categoryService;

    // ========================================
    // カテゴリ一覧・詳細表示
    // ========================================
    
    /**
     * カテゴリ一覧画面表示
     * @param model ビューモデル
     * @return カテゴリ一覧画面
     */
    @GetMapping
    public String listCategories(Model model) {
        log.info("カテゴリ一覧画面を表示中...");
        
        try {
            // 階層構造のカテゴリツリーを取得
            List<Category> categoryTree = categoryService.findCategoryTree();
            
            // 統計情報を取得
            CategoryService.CategoryStatistics statistics = categoryService.getCategoryStatistics();
            
            model.addAttribute("categoryTree", categoryTree);
            model.addAttribute("statistics", statistics);
            model.addAttribute("pageTitle", "カテゴリ管理");
            
            log.info("カテゴリ一覧画面表示完了: {}件のカテゴリ", statistics.totalCategories());
            return "admin/categories/list";
            
        } catch (Exception e) {
            log.error("カテゴリ一覧取得エラー", e);
            model.addAttribute("errorMessage", "カテゴリ一覧の取得に失敗しました: " + e.getMessage());
            return "admin/categories/list";
        }
    }
    
    /**
     * カテゴリ詳細画面表示
     * @param id カテゴリID
     * @param model ビューモデル
     * @return カテゴリ詳細画面
     */
    @GetMapping("/{id}")
    public String showCategory(@PathVariable Long id, Model model) {
        log.info("カテゴリ詳細画面を表示中: ID={}", id);
        
        try {
            Optional<Category> categoryOpt = categoryService.findById(id);
            
            if (categoryOpt.isEmpty()) {
                log.warn("カテゴリが見つかりません: ID={}", id);
                return "redirect:/admin/categories?error=notfound";
            }
            
            Category category = categoryOpt.get();
            
            // 子カテゴリを取得
            List<Category> childCategories = categoryService.findChildCategories(id);
            
            model.addAttribute("category", category);
            model.addAttribute("childCategories", childCategories);
            model.addAttribute("pageTitle", "カテゴリ詳細: " + category.getName());
            
            log.info("カテゴリ詳細画面表示完了: {}", category.getName());
            return "admin/categories/detail";
            
        } catch (Exception e) {
            log.error("カテゴリ詳細取得エラー: ID={}", id, e);
            return "redirect:/admin/categories?error=detail_error";
        }
    }
    
    // ========================================
    // カテゴリ作成
    // ========================================
    
    /**
     * カテゴリ新規作成フォーム表示
     * @param model ビューモデル
     * @return カテゴリ作成フォーム画面
     */
    @GetMapping("/new")
    public String newCategoryForm(Model model) {
        log.info("カテゴリ新規作成フォームを表示中...");
        
        try {
            Category category = new Category();
            List<Category> parentCategories = categoryService.findActiveParentCategories();
            
            model.addAttribute("category", category);
            model.addAttribute("parentCategories", parentCategories);
            model.addAttribute("pageTitle", "新規カテゴリ作成");
            model.addAttribute("isEdit", false);
            
            log.info("カテゴリ新規作成フォーム表示完了");
            return "admin/categories/form";
            
        } catch (Exception e) {
            log.error("カテゴリ作成フォーム表示エラー", e);
            return "redirect:/admin/categories?error=form_error";
        }
    }
    
    /**
     * カテゴリ新規作成処理
     * @param category カテゴリデータ
     * @param result バリデーション結果
     * @param model ビューモデル
     * @param redirectAttributes リダイレクト属性
     * @return リダイレクト先またはフォーム画面
     */
    @PostMapping("/new")
    public String createCategory(@Validated @ModelAttribute Category category,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        log.info("カテゴリ新規作成処理開始: {}", category.getName());
        
        try {
            // バリデーションエラーがある場合
            if (result.hasErrors()) {
                log.warn("カテゴリ作成バリデーションエラー: {}", result.getAllErrors());
                List<Category> parentCategories = categoryService.findActiveParentCategories();
                model.addAttribute("parentCategories", parentCategories);
                model.addAttribute("pageTitle", "新規カテゴリ作成");
                model.addAttribute("isEdit", false);
                return "admin/categories/form";
            }
            
            // カテゴリを保存
            Category savedCategory = categoryService.save(category);
            
            log.info("カテゴリ新規作成完了: ID={}, Name={}", savedCategory.getId(), savedCategory.getName());
            redirectAttributes.addFlashAttribute("successMessage", 
                "カテゴリ「" + savedCategory.getName() + "」を作成しました。");
            
            return "redirect:/admin/categories";
            
        } catch (IllegalArgumentException e) {
            log.warn("カテゴリ作成ビジネスロジックエラー: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            List<Category> parentCategories = categoryService.findActiveParentCategories();
            model.addAttribute("parentCategories", parentCategories);
            model.addAttribute("pageTitle", "新規カテゴリ作成");
            model.addAttribute("isEdit", false);
            return "admin/categories/form";
            
        } catch (Exception e) {
            log.error("カテゴリ作成予期しないエラー", e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "カテゴリの作成に失敗しました: " + e.getMessage());
            return "redirect:/admin/categories";
        }
    }
    
    // ========================================
    // カテゴリ編集
    // ========================================
    
    /**
     * カテゴリ編集フォーム表示
     * @param id カテゴリID
     * @param model ビューモデル
     * @return カテゴリ編集フォーム画面
     */
    @GetMapping("/{id}/edit")
    public String editCategoryForm(@PathVariable Long id, Model model) {
        log.info("カテゴリ編集フォームを表示中: ID={}", id);
        
        try {
            Optional<Category> categoryOpt = categoryService.findById(id);
            
            if (categoryOpt.isEmpty()) {
                log.warn("編集対象カテゴリが見つかりません: ID={}", id);
                return "redirect:/admin/categories?error=notfound";
            }
            
            Category category = categoryOpt.get();
            List<Category> parentCategories = categoryService.findActiveParentCategories();
            
            model.addAttribute("category", category);
            model.addAttribute("parentCategories", parentCategories);
            model.addAttribute("pageTitle", "カテゴリ編集: " + category.getName());
            model.addAttribute("isEdit", true);
            
            log.info("カテゴリ編集フォーム表示完了: {}", category.getName());
            return "admin/categories/form";
            
        } catch (Exception e) {
            log.error("カテゴリ編集フォーム表示エラー: ID={}", id, e);
            return "redirect:/admin/categories?error=form_error";
        }
    }
    
    /**
     * カテゴリ編集処理
     * @param id カテゴリID
     * @param category カテゴリデータ
     * @param result バリデーション結果
     * @param model ビューモデル
     * @param redirectAttributes リダイレクト属性
     * @return リダイレクト先またはフォーム画面
     */
    @PostMapping("/{id}/edit")
    public String updateCategory(@PathVariable Long id,
                               @Validated @ModelAttribute Category category,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        log.info("カテゴリ編集処理開始: ID={}, Name={}", id, category.getName());
        
        try {
            // IDをセット
            category.setId(id);
            
            // バリデーションエラーがある場合
            if (result.hasErrors()) {
                log.warn("カテゴリ編集バリデーションエラー: {}", result.getAllErrors());
                List<Category> parentCategories = categoryService.findActiveParentCategories();
                model.addAttribute("parentCategories", parentCategories);
                model.addAttribute("pageTitle", "カテゴリ編集: " + category.getName());
                model.addAttribute("isEdit", true);
                return "admin/categories/form";
            }
            
            // カテゴリを更新
            Category updatedCategory = categoryService.save(category);
            
            log.info("カテゴリ編集完了: ID={}, Name={}", updatedCategory.getId(), updatedCategory.getName());
            redirectAttributes.addFlashAttribute("successMessage", 
                "カテゴリ「" + updatedCategory.getName() + "」を更新しました。");
            
            return "redirect:/admin/categories";
            
        } catch (IllegalArgumentException e) {
            log.warn("カテゴリ編集ビジネスロジックエラー: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            List<Category> parentCategories = categoryService.findActiveParentCategories();
            model.addAttribute("parentCategories", parentCategories);
            model.addAttribute("pageTitle", "カテゴリ編集");
            model.addAttribute("isEdit", true);
            return "admin/categories/form";
            
        } catch (Exception e) {
            log.error("カテゴリ編集予期しないエラー: ID={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "カテゴリの更新に失敗しました: " + e.getMessage());
            return "redirect:/admin/categories";
        }
    }
    
    // ========================================
    // カテゴリ削除
    // ========================================
    
    /**
     * カテゴリ削除処理
     * @param id カテゴリID
     * @param redirectAttributes リダイレクト属性
     * @return カテゴリ一覧へのリダイレクト
     */
    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("カテゴリ削除処理開始: ID={}", id);
        
        try {
            // 削除前にカテゴリ情報を取得（ログ用）
            Optional<Category> categoryOpt = categoryService.findById(id);
            String categoryName = categoryOpt.map(Category::getName).orElse("不明");
            
            // カテゴリを削除
            categoryService.deleteById(id);
            
            log.info("カテゴリ削除完了: ID={}, Name={}", id, categoryName);
            redirectAttributes.addFlashAttribute("successMessage", 
                "カテゴリ「" + categoryName + "」を削除しました。");
            
        } catch (IllegalArgumentException e) {
            log.warn("カテゴリ削除ビジネスロジックエラー: ID={}, Error={}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            
        } catch (Exception e) {
            log.error("カテゴリ削除予期しないエラー: ID={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "カテゴリの削除に失敗しました: " + e.getMessage());
        }
        
        return "redirect:/admin/categories";
    }
    
    // ========================================
    // Ajax API
    // ========================================
    
    /**
     * 子カテゴリ取得API（Ajax用）
     * @param parentId 親カテゴリID
     * @return 子カテゴリリスト（JSON）
     */
    @GetMapping("/api/children/{parentId}")
    @ResponseBody
    public ResponseEntity<List<Category>> getChildCategories(@PathVariable Long parentId) {
        log.debug("子カテゴリAPI呼び出し: 親ID={}", parentId);
        
        try {
            List<Category> childCategories = categoryService.findChildCategories(parentId);
            log.debug("子カテゴリAPI応答: 親ID={}, 子数={}", parentId, childCategories.size());
            return ResponseEntity.ok(childCategories);
            
        } catch (Exception e) {
            log.error("子カテゴリAPI エラー: 親ID={}", parentId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * カテゴリ検索API（Ajax用）
     * @param keyword 検索キーワード
     * @return 検索結果カテゴリリスト（JSON）
     */
    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<List<Category>> searchCategories(@RequestParam(required = false) String keyword) {
        log.debug("カテゴリ検索API呼び出し: キーワード={}", keyword);
        
        try {
            List<Category> searchResults = categoryService.searchCategories(keyword);
            log.debug("カテゴリ検索API応答: キーワード={}, 結果数={}", keyword, searchResults.size());
            return ResponseEntity.ok(searchResults);
            
        } catch (Exception e) {
            log.error("カテゴリ検索API エラー: キーワード={}", keyword, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * カテゴリ統計情報API（Ajax用）
     * @return カテゴリ統計情報（JSON）
     */
    @GetMapping("/api/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCategoryStatistics() {
        log.debug("カテゴリ統計API呼び出し");
        
        try {
            CategoryService.CategoryStatistics statistics = categoryService.getCategoryStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalCategories", statistics.totalCategories());
            response.put("parentCategories", statistics.parentCategories());
            response.put("childCategories", statistics.childCategories());
            response.put("categoriesWithParts", statistics.categoriesWithParts());
            response.put("categoriesWithoutParts", statistics.categoriesWithoutParts());
            response.put("deletableCategories", statistics.deletableCategories());
            
            log.debug("カテゴリ統計API応答: {}", statistics);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("カテゴリ統計API エラー", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}