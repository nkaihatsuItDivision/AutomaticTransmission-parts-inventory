package com.example.automatictransmissionpartsinventory.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.automatictransmissionpartsinventory.dto.AdvancedSearchCriteria;
import com.example.automatictransmissionpartsinventory.entity.AutomativePart;
import com.example.automatictransmissionpartsinventory.entity.Category;
import com.example.automatictransmissionpartsinventory.exception.ServiceException;
import com.example.automatictransmissionpartsinventory.service.AutomaticPartService;
import com.example.automatictransmissionpartsinventory.service.CategoryService;
import com.example.automatictransmissionpartsinventory.service.impl.AutomaticPartCsvService;

import lombok.extern.slf4j.Slf4j;
/**
 * AT部品在庫管理システム - Controller層
 * 
 * Phase 6で実装するWeb制御機能
 * - HTTPリクエスト・レスポンス処理
 * - Service層との連携
 * - View層へのデータ受け渡し
 * - エラーハンドリング
 * 
 * @author Phase 6開発者
 * @version 1.0
 */
@Controller
@RequestMapping("/parts")
@Slf4j
public class AutomaticPartController {
	
	@Autowired
	private CategoryService categoryService;

    @Autowired
    private AutomaticPartService automaticPartService;
    
    @Autowired
    private AutomaticPartCsvService automaticPartCsvService;

    // ========================================
    // 1. 一覧表示機能
    // ========================================
    
    /**
     * 部品一覧表示
     * URL: GET /parts
     */
    @GetMapping
    public String list(Model model) {
        log.info("部品一覧表示処理開始");
        
        try {
            List<AutomativePart> parts = automaticPartService.findAllParts();
            model.addAttribute("parts", parts);
            
            // ★追加: カテゴリ一覧をモデルに追加（フィルタ用）
            List<Category> categories = categoryService.findActiveCategories();
            model.addAttribute("categories", categories);
            
            log.info("部品一覧表示処理完了: {} 件取得", parts.size());
            return "parts/list";
            
        } catch (Exception e) {
            log.error("部品一覧表示エラー", e);
            model.addAttribute("errorMessage", "データ取得に失敗しました");
            return "error";
        }
    }

    // ========================================
    // 2. 詳細表示機能
    // ========================================
    
    /**
     * 部品詳細表示
     * URL: GET /parts/{id}
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        log.info("部品詳細表示処理開始: ID={}", id);
        
        try {
            Optional<AutomativePart> partOptional = automaticPartService.findById(id);
            
            if (partOptional.isPresent()) {
                model.addAttribute("part", partOptional.get());
                log.info("部品詳細表示処理完了: ID={}", id);
                return "parts/detail";
            } else {
                log.warn("部品が見つかりません: ID={}", id);
                model.addAttribute("errorMessage", "指定された部品が見つかりません");
                return "error";
            }
            
        } catch (Exception e) {
            log.error("部品詳細表示エラー: ID={}", id, e);
            model.addAttribute("errorMessage", "データ取得に失敗しました");
            return "error";
        }
    }

    // ========================================
    // 3. 新規登録画面表示
    // ========================================
    
    /**
     * 新規登録画面表示
     * URL: GET /parts/new
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        log.info("新規登録画面表示処理開始");
        
        // 空のAutomaticPartオブジェクトを作成してフォームに渡す
        model.addAttribute("part", new AutomativePart());
        
        // ★追加: カテゴリ一覧を取得してフォームに渡す
        try {
            List<Category> categories = categoryService.findActiveCategories();
            model.addAttribute("categories", categories);
        } catch (Exception e) {
            log.warn("カテゴリ一覧取得でエラーが発生しましたが、処理を続行します", e);
            model.addAttribute("categories", List.of()); // 空リストで継続
        }
        
        log.info("新規登録画面表示処理完了");
        return "parts/form";
    }

    // ========================================
    // 4. 新規登録処理
    // ========================================
    
    /**
     * 新規登録処理
     * URL: POST /parts
     */
    @PostMapping
    public String create(@Valid AutomativePart part, BindingResult result, Model model) {
        log.info("新規登録処理開始: {}", part.getPartName());
        
        try {
            // バリデーションエラーチェック
            if (result.hasErrors()) {
                log.warn("バリデーションエラー: {}", result.getAllErrors());
                return "parts/form";
            }
            
            // 部品番号の重複チェック
            if (automaticPartService.isPartNumberDuplicated(part.getPartNumber(), null)) {
                log.warn("部品番号重複: {}", part.getPartNumber());
                result.rejectValue("partNumber", "duplicate.part.partNumber", "この部品番号は既に使用されています");
                return "parts/form";
            }
            
            // 新規登録実行
            AutomativePart savedPart = automaticPartService.registerPart(part);
            
            log.info("新規登録処理完了: ID={}, 部品番号={}", savedPart.getId(), savedPart.getPartNumber());
            
            // PRGパターン：POST-Redirect-Get
            return "redirect:/parts";
            
        } catch (ServiceException e) {
            log.error("新規登録処理エラー（ServiceException）: {}", part.getPartName(), e);
            model.addAttribute("errorMessage", "登録処理に失敗しました: " + e.getMessage());
            return "parts/form";
            
        } catch (Exception e) {
            log.error("新規登録処理エラー（予期せぬ例外）: {}", part.getPartName(), e);
            model.addAttribute("errorMessage", "システムエラーが発生しました");
            return "parts/form";
        }
    }

    // ========================================
    // 5. 編集画面表示
    // ========================================
    
    /**
     * 編集画面表示
     * URL: GET /parts/{id}/edit
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        log.info("編集画面表示処理開始: ID={}", id);
        
        try {
            Optional<AutomativePart> partOptional = automaticPartService.findById(id);
            
            if (partOptional.isPresent()) {
                model.addAttribute("part", partOptional.get());
                model.addAttribute("isEdit", true); // 編集モードのフラグ
                
                // ★追加: カテゴリ一覧を取得してフォームに渡す
                List<Category> categories = categoryService.findActiveCategories();
                model.addAttribute("categories", categories);
                
                log.info("編集画面表示処理完了: ID={}", id);
                return "parts/form";
            } else {
                log.warn("編集対象部品が見つかりません: ID={}", id);
                model.addAttribute("errorMessage", "指定された部品が見つかりません");
                return "error";
            }
            
        } catch (Exception e) {
            log.error("編集画面表示エラー: ID={}", id, e);
            model.addAttribute("errorMessage", "データ取得に失敗しました");
            return "error";
        }
    }

    // ========================================
    // 6. 更新処理
    // ========================================
    
    /**
     * 更新処理
     * URL: POST /parts/{id}
     */
    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid AutomativePart part, 
                        BindingResult result, Model model) {
        log.info("更新処理開始: ID={}, 部品名={}", id, part.getPartName());
        
        try {
            // バリデーションエラーチェック
            if (result.hasErrors()) {
                log.warn("バリデーションエラー: {}", result.getAllErrors());
                model.addAttribute("isEdit", true);
                return "parts/form";
            }
            
            // 部品番号の重複チェック（自身を除く）
            if (automaticPartService.isPartNumberDuplicated(part.getPartNumber(), id)) {
                log.warn("部品番号重複: {}", part.getPartNumber());
                result.rejectValue("partNumber", "duplicate.part.partNumber", "この部品番号は既に使用されています");
                model.addAttribute("isEdit", true);
                return "parts/form";
            }
            
            // 更新処理実行
            AutomativePart updatedPart = automaticPartService.updatePart(id, part);
            
            log.info("更新処理完了: ID={}, 部品番号={}", updatedPart.getId(), updatedPart.getPartNumber());
            
            // PRGパターン：POST-Redirect-Get
            return "redirect:/parts";
            
        } catch (ServiceException e) {
            log.error("更新処理エラー（ServiceException）: ID={}", id, e);
            model.addAttribute("errorMessage", "更新処理に失敗しました: " + e.getMessage());
            model.addAttribute("isEdit", true);
            return "parts/form";
            
        } catch (Exception e) {
            log.error("更新処理エラー（予期せぬ例外）: ID={}", id, e);
            model.addAttribute("errorMessage", "システムエラーが発生しました");
            model.addAttribute("isEdit", true);
            return "parts/form";
        }
    }

    // ========================================
    // 7. 削除処理
    // ========================================
    
    /**
     * 削除処理
     * URL: POST /parts/{id}/delete
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        log.info("削除処理開始: ID={}", id);
        
        try {
            automaticPartService.deletePart(id);
            
            log.info("削除処理完了: ID={}", id);
            
            // PRGパターン：POST-Redirect-Get
            return "redirect:/parts";
            
        } catch (ServiceException e) {
            log.error("削除処理エラー（ServiceException）: ID={}", id, e);
            // 削除失敗時は一覧画面にリダイレクトしてエラーメッセージを表示
            // 実際の実装では、リダイレクト先でエラーメッセージを表示する仕組みが必要
            return "redirect:/parts?error=delete";
            
        } catch (Exception e) {
            log.error("削除処理エラー（予期せぬ例外）: ID={}", id, e);
            return "redirect:/parts?error=system";
        }
    }

    // ========================================
    // 8. 検索機能
    // ========================================
    
    /**
     * 検索機能
     * URL: GET /parts/search
     */
    @GetMapping("/search")
    public String search(@RequestParam(required = false) String partName,
                        @RequestParam(required = false) String manufacturer,
                        @RequestParam(required = false) BigDecimal minPrice,
                        @RequestParam(required = false) BigDecimal maxPrice,
                        @RequestParam(required = false) Long categoryId, // ★追加
                        Model model) {
        
        log.info("検索処理開始: 部品名={}, メーカー={}, 最低価格={}, 最高価格={}, カテゴリID={}", 
                partName, manufacturer, minPrice, maxPrice, categoryId);
        
        try {
            List<AutomativePart> searchResults;
            
            // パラメータがすべて空の場合は全件取得
            boolean hasSearchCondition = (partName != null && !partName.trim().isEmpty()) ||
                                        (manufacturer != null && !manufacturer.trim().isEmpty()) ||
                                        minPrice != null || maxPrice != null ||
                                        categoryId != null; // ★追加
            
            if (hasSearchCondition) {
                searchResults = automaticPartService.findByConditions(partName, manufacturer, minPrice, maxPrice, categoryId); // ★修正
                log.info("検索処理完了: {} 件取得", searchResults.size());
            } else {
                searchResults = automaticPartService.findAllParts();
                log.info("全件取得完了: {} 件取得", searchResults.size());
            }
            
            // 検索結果をモデルに追加
            model.addAttribute("parts", searchResults);
            
            // 検索条件を再表示のためにモデルに追加
            model.addAttribute("searchPartName", partName);
            model.addAttribute("searchManufacturer", manufacturer);
            model.addAttribute("searchMinPrice", minPrice);
            model.addAttribute("searchMaxPrice", maxPrice);
            model.addAttribute("searchCategoryId", categoryId); // ★追加
            
            // ★追加: カテゴリ一覧と選択カテゴリをモデルに追加
            List<Category> categories = categoryService.findActiveCategories();
            model.addAttribute("categories", categories);
            
            if (categoryId != null) {
                categoryService.findById(categoryId).ifPresent(category -> 
                    model.addAttribute("selectedCategory", category));
            }
            
            return "parts/list";
            
        } catch (Exception e) {
            log.error("検索処理エラー", e);
            model.addAttribute("errorMessage", "検索処理に失敗しました");
            return "error";
        }
    }
 // ========================================
 // 9. CSV エクスポート機能
 // ========================================

 /**
  * CSVエクスポート処理
  * URL: GET /parts/export/csv
  */
 @GetMapping("/export/csv")
 public ResponseEntity<byte[]> exportCSV() {
     log.info("CSV エクスポート処理開始");
     
     try {
         byte[] csvData = automaticPartCsvService.exportPartsToCSV();
         
         // ファイル名に現在の日時を含める
         String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
         String filename = "parts_export_" + timestamp + ".csv";
         
         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
         headers.setContentDispositionFormData("attachment", filename);
         headers.setContentLength(csvData.length);
         
         log.info("CSV エクスポート処理完了: ファイル名={}", filename);
         
         return ResponseEntity.ok()
                 .headers(headers)
                 .body(csvData);
                 
     } catch (ServiceException e) {
         log.error("CSV エクスポート処理でビジネスエラーが発生: {}", e.getMessage());
         return ResponseEntity.badRequest()
                 .body(("エラー: " + e.getMessage()).getBytes());
     } catch (Exception e) {
         log.error("CSV エクスポート処理で予期せぬエラーが発生", e);
         return ResponseEntity.internalServerError()
                 .body("エクスポート処理に失敗しました".getBytes());
     }
 }

 // ========================================
 // 10. CSV インポート画面表示
 // ========================================

 /**
  * CSVインポート画面表示
  * URL: GET /parts/import/csv
  */
 @GetMapping("/import/csv")
 public String showImportForm(Model model) {
     log.info("CSV インポート画面表示");
     return "parts/csv-import";
 }

 // ========================================
 // 11. CSV インポート処理
 // ========================================

 /**
  * CSVインポート処理
  * URL: POST /parts/import/csv
  */
 @PostMapping("/import/csv")
 public String importCSV(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
     log.info("CSV インポート処理開始: ファイル名={}", file.getOriginalFilename());
     
     try {
         AutomaticPartCsvService.CsvImportResult result = automaticPartCsvService.importPartsFromCSV(file);
         
         if (result.isSuccess() && !result.hasSkipped()) {
             // 完全成功
        	 redirectAttributes.addFlashAttribute("successMessage", 
                     String.format("CSV インポートが完了しました（成功: %d件）", result.getSuccessCount()));
         } else {
             // 部分的成功またはスキップあり
             StringBuilder message = new StringBuilder();
             message.append(String.format("CSV インポートが完了しました（成功: %d件", result.getSuccessCount()));
             
             if (result.hasErrors()) {
                 message.append(String.format(", エラー: %d件", result.getErrorCount()));
             }
             
             if (result.hasSkipped()) {
                 message.append(String.format(", スキップ: %d件", result.getSkipCount()));
             }
             
             message.append("）");
             
             redirectAttributes.addFlashAttribute("infoMessage", message.toString());
             
             // エラー詳細の追加
             if (result.hasErrors()) {
            	 redirectAttributes.addFlashAttribute("errorDetails", result.getErrorMessages());
             }
             
             if (result.hasSkipped()) {
            	 redirectAttributes.addFlashAttribute("skipDetails", result.getSkippedMessages());
             }
         }
         
         log.info("CSV インポート処理完了: 成功={}, エラー={}, スキップ={}", 
                 result.getSuccessCount(), result.getErrorCount(), result.getSkipCount());
         
         return "redirect:/parts";
         
     } catch (ServiceException e) {
    	    log.error("CSV インポート処理でビジネスエラーが発生: {}", e.getMessage());
    	    redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    	    return "redirect:/parts/import/csv";
    	} catch (Exception e) {
    	    log.error("CSV インポート処理で予期せぬエラーが発生", e);
    	    redirectAttributes.addFlashAttribute("errorMessage", "インポート処理に失敗しました");
    	    return "redirect:/parts/import/csv";
    	}
  }
//========================================
 // Phase 8.3 Step 6-1で追加: 高度検索機能
 // ========================================

 /**
  * 高度検索画面の表示
  * GET /parts/advanced-search
  */
 @GetMapping("/advanced-search")
 public String showAdvancedSearch(Model model, @ModelAttribute AdvancedSearchCriteria criteria) {
     log.info("高度検索画面表示開始: {}", criteria);
     
     try {
         // ★★★ 修正: 検索条件が実際に指定されている場合のみ検索実行 ★★★
         boolean hasActualSearchConditions = hasSearchConditions(criteria);
         
         if (hasActualSearchConditions) {
             // デフォルト値設定（必須）
             criteria.setDefaultPagination();
             criteria.setDefaultSort();
             
             // 検索を実行
             Page<AutomativePart> parts = automaticPartService.searchByAdvancedCriteria(criteria);
             long totalCount = automaticPartService.countByAdvancedCriteria(criteria);
             
             // 検索統計情報の取得
             Map<String, Object> statistics = automaticPartService.getSearchStatistics(criteria);
             
             model.addAttribute("parts", parts);
             model.addAttribute("totalCount", totalCount);
             model.addAttribute("searchExecuted", true);
             model.addAttribute("statistics", statistics);
             
             // ページネーション情報をModelに追加
             model.addAttribute("currentPage", criteria.getPage());
             model.addAttribute("totalPages", parts.getTotalPages());
             model.addAttribute("pageSize", criteria.getSize());
             model.addAttribute("hasNext", parts.hasNext());
             model.addAttribute("hasPrevious", parts.hasPrevious());
             
             log.info("検索完了: 結果{}件, ページ{}/{}", totalCount, criteria.getPage() + 1, parts.getTotalPages());
         } else {
             // 初回表示：検索フォームのみ表示
             log.info("初回表示: 検索フォームのみ表示");
             model.addAttribute("searchExecuted", false);
         }
         
     } catch (Exception e) {
         log.error("高度検索画面表示でエラーが発生しました", e);
         model.addAttribute("error", "検索中にエラーが発生しました: " + e.getMessage());
         model.addAttribute("searchExecuted", false);
     }
     
     // カテゴリ一覧（検索条件用）
     List<Category> categories = categoryService.findActiveCategories();
     model.addAttribute("categories", categories);
     
     // 検索条件をモデルに追加
     model.addAttribute("criteria", criteria);
     
     // ソート選択肢の設定
     addSortOptionsToModel(model);
     
     return "parts/advanced-search";
 }

 // ★★★ 追加: 実際の検索条件があるかチェックするメソッド ★★★
 private boolean hasSearchConditions(AdvancedSearchCriteria criteria) {
     return (criteria.getPartNumber() != null && !criteria.getPartNumber().trim().isEmpty()) ||
            (criteria.getPartName() != null && !criteria.getPartName().trim().isEmpty()) ||
            (criteria.getManufacturer() != null && !criteria.getManufacturer().trim().isEmpty()) ||
            criteria.getCategoryId() != null ||
            criteria.getMinPrice() != null ||
            criteria.getMaxPrice() != null ||
            criteria.getCreatedAfter() != null ||
            criteria.getCreatedBefore() != null ||
            (criteria.getPage() != null && criteria.getPage() > 0); // ページネーション指定時も検索実行
 }

 /**
  * 高度検索のAjax処理
  * POST /parts/api/advanced-search
  */
 @PostMapping("/api/advanced-search")
 @ResponseBody
 public ResponseEntity<?> advancedSearchApi(@RequestBody AdvancedSearchCriteria criteria) {
     log.info("Ajax高度検索API呼び出し開始: {}", criteria);
     
     try {
         // 検索条件の妥当性チェック
         Map<String, String> validationErrors = automaticPartService.validateSearchCriteria(criteria);
         if (!validationErrors.isEmpty()) {
             log.warn("検索条件に不正な値があります: {}", validationErrors);
             return ResponseEntity.badRequest().body(createErrorResponse("検索条件が不正です", validationErrors));
         }
         
         // デフォルト値の設定
         criteria.setDefaultSort();
         criteria.setDefaultPagination();
         
         // 高度検索の実行
         Page<AutomativePart> parts = automaticPartService.searchByAdvancedCriteria(criteria);
         long totalCount = automaticPartService.countByAdvancedCriteria(criteria);
         
         // 検索統計情報の取得
         Map<String, Object> statistics = automaticPartService.getSearchStatistics(criteria);
         
         // レスポンスの構築
         Map<String, Object> response = new HashMap<>();
         response.put("success", true);
         response.put("parts", parts.getContent());
         response.put("totalCount", totalCount);
         response.put("currentPage", parts.getNumber());
         response.put("totalPages", parts.getTotalPages());
         response.put("pageSize", parts.getSize());
         response.put("hasNext", parts.hasNext());
         response.put("hasPrevious", parts.hasPrevious());
         response.put("statistics", statistics);
         
         log.info("Ajax高度検索API呼び出し完了: 結果{}件", totalCount);
         return ResponseEntity.ok(response);
         
     } catch (Exception e) {
         log.error("Ajax高度検索API呼び出しでエラーが発生しました", e);
         return ResponseEntity.badRequest().body(createErrorResponse("検索処理に失敗しました", e.getMessage()));
     }
 }

 /**
  * 検索条件のリセット処理
  * POST /parts/reset-search
  */
 @PostMapping("/reset-search")
 public String resetSearch(Model model) {
     log.info("検索条件リセット");
     
     // 空の検索条件で高度検索画面にリダイレクト
     return "redirect:/parts/advanced-search";
 }

 /**
  * 検索条件の保存処理（お気に入り検索）
  * POST /parts/save-search
  */
 @PostMapping("/save-search")
 @ResponseBody
 public ResponseEntity<?> saveSearchCriteria(@RequestBody Map<String, Object> request) {
     log.info("検索条件保存API呼び出し開始");
     
     try {
         String searchName = (String) request.get("searchName");
         @SuppressWarnings("unchecked")
         Map<String, Object> criteriaMap = (Map<String, Object>) request.get("criteria");
         
         if (searchName == null || searchName.trim().isEmpty()) {
             return ResponseEntity.badRequest().body(createErrorResponse("検索名は必須です", null));
         }
         
         // TODO: 実際の保存処理（データベースまたはセッション）
         // 現在は成功レスポンスのみ返す
         
         Map<String, Object> response = new HashMap<>();
         response.put("success", true);
         response.put("message", "検索条件 '" + searchName + "' を保存しました");
         
         log.info("検索条件保存完了: {}", searchName);
         return ResponseEntity.ok(response);
         
     } catch (Exception e) {
         log.error("検索条件保存でエラーが発生しました", e);
         return ResponseEntity.badRequest().body(createErrorResponse("検索条件の保存に失敗しました", e.getMessage()));
     }
 }

 /**
  * ソート選択肢をモデルに追加する補助メソッド
  */
 private void addSortOptionsToModel(Model model) {
     Map<String, String> sortOptions = new HashMap<>();
     sortOptions.put("updatedAt", "更新日時");
     sortOptions.put("createdAt", "登録日時");
     sortOptions.put("partName", "部品名");
     sortOptions.put("partNumber", "部品番号");
     sortOptions.put("manufacturer", "メーカー");
     sortOptions.put("price", "価格");
     
     model.addAttribute("sortOptions", sortOptions);
     
     Map<String, String> sortOrderOptions = new HashMap<>();
     sortOrderOptions.put("DESC", "降順");
     sortOrderOptions.put("ASC", "昇順");
     
     model.addAttribute("sortOrderOptions", sortOrderOptions);
 }

 /**
  * エラーレスポンス作成用の補助メソッド
  */
 private Map<String, Object> createErrorResponse(String message, Object details) {
     Map<String, Object> response = new HashMap<>();
     response.put("success", false);
     response.put("message", message);
     response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
     
     if (details != null) {
         response.put("details", details);
     }
     
     return response;
 }

 /**
  * 検索結果の統計情報表示
  * GET /parts/search-statistics
  */
 @GetMapping("/search-statistics")
 @ResponseBody
 public ResponseEntity<?> getSearchStatistics(AdvancedSearchCriteria criteria) {
     log.info("検索統計情報API呼び出し開始");
     
     try {
         Map<String, Object> statistics = automaticPartService.getSearchStatistics(criteria);
         
         Map<String, Object> response = new HashMap<>();
         response.put("success", true);
         response.put("statistics", statistics);
         
         return ResponseEntity.ok(response);
         
     } catch (Exception e) {
         log.error("検索統計情報取得でエラーが発生しました", e);
         return ResponseEntity.badRequest().body(createErrorResponse("統計情報の取得に失敗しました", e.getMessage()));
     }
 }
 /**
  * 高度検索のフォーム送信処理（デバッグ版）
  * POST /parts/advanced-search
  */
 @PostMapping("/advanced-search")
 public String processAdvancedSearch(AdvancedSearchCriteria criteria, Model model) {
     log.info("=== 高度検索フォーム送信処理開始 ===");
     log.info("受信したcriteria: {}", criteria);
     log.info("受信時点でのpage: {}, size: {}", criteria.getPage(), criteria.getSize());
     
     try {
         // デフォルト値設定前の状態をログ出力
         log.info("デフォルト値設定前: page={}, size={}", criteria.getPage(), criteria.getSize());
         
         // デフォルト値設定
         criteria.setDefaultSort();
         criteria.setDefaultPagination();
         
         // デフォルト値設定後の状態をログ出力
         log.info("デフォルト値設定後: page={}, size={}", criteria.getPage(), criteria.getSize());
         log.info("最終的なページネーション設定: page={}, size={}", criteria.getPage(), criteria.getSize());
         
         // 検索条件の妥当性チェック
         Map<String, String> validationErrors = automaticPartService.validateSearchCriteria(criteria);
         if (!validationErrors.isEmpty()) {
             log.warn("検索条件に不正な値があります: {}", validationErrors);
             model.addAttribute("errorMessage", "検索条件に不正な値があります");
             model.addAttribute("validationErrors", validationErrors);
             model.addAttribute("criteria", criteria);
             return "parts/advanced-search";
         }
         
         // 高度検索の実行
         log.info("検索実行前: criteria={}", criteria);
         Page<AutomativePart> parts = automaticPartService.searchByAdvancedCriteria(criteria);
         long totalCount = automaticPartService.countByAdvancedCriteria(criteria);
         
         log.info("検索実行後:");
         log.info("- parts.getContent().size(): {}", parts.getContent().size());
         log.info("- parts.getSize(): {}", parts.getSize());
         log.info("- parts.getNumber(): {}", parts.getNumber());
         log.info("- parts.getTotalPages(): {}", parts.getTotalPages());
         log.info("- totalCount: {}", totalCount);
         
         // 検索統計情報の取得
         Map<String, Object> statistics = automaticPartService.getSearchStatistics(criteria);
         
         // 検索結果をモデルに追加
         model.addAttribute("parts", parts.getContent());
         model.addAttribute("totalCount", totalCount);
         model.addAttribute("searchExecuted", true);
         model.addAttribute("statistics", statistics);
         
         // ページネーション情報をモデルに追加
         model.addAttribute("currentPage", criteria.getPage());
         model.addAttribute("totalPages", parts.getTotalPages());
         model.addAttribute("pageSize", criteria.getSize());
         model.addAttribute("hasNext", parts.hasNext());
         model.addAttribute("hasPrevious", parts.hasPrevious());
         
         log.info("モデルに設定した値:");
         log.info("- currentPage: {}", criteria.getPage());
         log.info("- totalPages: {}", parts.getTotalPages());
         log.info("- pageSize: {}", criteria.getSize());
         log.info("- parts.size(): {}", parts.getContent().size());
         
         log.info("検索完了: 結果{}件, ページ{}/{}, サイズ{}", 
                 totalCount, criteria.getPage() + 1, parts.getTotalPages(), criteria.getSize());
         
     } catch (Exception e) {
         log.error("高度検索処理でエラーが発生しました", e);
         model.addAttribute("errorMessage", "検索中にエラーが発生しました: " + e.getMessage());
         model.addAttribute("searchExecuted", false);
     }
     
     try {
         // カテゴリ一覧（検索条件用）
         List<Category> categories = categoryService.findActiveCategories();
         model.addAttribute("categories", categories);
         
         // 検索条件をモデルに追加
         model.addAttribute("criteria", criteria);
         log.info("最終的にモデルに設定したcriteria: {}", criteria);
         
         // ソート選択肢の設定
         addSortOptionsToModel(model);
         
     } catch (Exception e) {
         log.error("カテゴリ取得でエラー", e);
     }
     
     log.info("=== 高度検索フォーム送信処理完了 ===");
     return "parts/advanced-search";
 }
 /**
  * 検索条件付きCSVエクスポート処理
  * GET /parts/export/csv/search
  */
 @GetMapping("/export/csv/search")
 public ResponseEntity<byte[]> exportSearchResultsCSV(AdvancedSearchCriteria criteria) {
     log.info("検索条件付きCSVエクスポート処理開始: {}", criteria);
     
     try {
         // 検索条件の前処理
         criteria.setDefaultSort();
         criteria.setDefaultPagination();
         
         // 検索条件が空の場合は全データをエクスポート
         byte[] csvData;
         String baseFilename;
         
         if (criteria.isEmpty()) {
             log.info("検索条件が空のため、全データをエクスポートします");
             csvData = automaticPartCsvService.exportPartsToCSV();
             baseFilename = "parts_all_export";
         } else {
             log.info("検索条件に基づいてフィルタリングされたデータをエクスポートします");
             csvData = automaticPartCsvService.exportPartsToCSVBySearchCriteria(criteria);
             baseFilename = "parts_search_export";
         }
         
         // ファイル名に現在の日時を含める
         String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
         String filename = baseFilename + "_" + timestamp + ".csv";
         
         // レスポンスヘッダーの設定
         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
         headers.setContentDispositionFormData("attachment", filename);
         headers.setContentLength(csvData.length);
         
         log.info("検索条件付きCSVエクスポート処理完了: ファイル名={}, サイズ={}bytes", filename, csvData.length);
         
         return ResponseEntity.ok()
                 .headers(headers)
                 .body(csvData);
                 
     } catch (ServiceException e) {
         log.error("検索条件付きCSVエクスポート処理でビジネスエラーが発生: {}", e.getMessage());
         return ResponseEntity.badRequest()
                 .body(("エラー: " + e.getMessage()).getBytes());
     } catch (Exception e) {
         log.error("検索条件付きCSVエクスポート処理で予期せぬエラーが発生", e);
         return ResponseEntity.internalServerError()
                 .body("エクスポート処理に失敗しました".getBytes());
     }
 }
}