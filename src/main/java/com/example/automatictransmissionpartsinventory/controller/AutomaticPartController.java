package com.example.automatictransmissionpartsinventory.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.automatictransmissionpartsinventory.entity.AutomativePart;
import com.example.automatictransmissionpartsinventory.exception.ServiceException;
import com.example.automatictransmissionpartsinventory.service.AutomaticPartService;
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
                        Model model) {
        
        log.info("検索処理開始: 部品名={}, メーカー={}, 最低価格={}, 最高価格={}", 
                partName, manufacturer, minPrice, maxPrice);
        
        try {
            List<AutomativePart> searchResults;
            
            // パラメータがすべて空の場合は全件取得
            boolean hasSearchCondition = (partName != null && !partName.trim().isEmpty()) ||
                                        (manufacturer != null && !manufacturer.trim().isEmpty()) ||
                                        minPrice != null || maxPrice != null;
            
            if (hasSearchCondition) {
                searchResults = automaticPartService.findByConditions(partName, manufacturer, minPrice, maxPrice);
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
}