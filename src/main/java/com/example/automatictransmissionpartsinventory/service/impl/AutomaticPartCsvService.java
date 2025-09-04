package com.example.automatictransmissionpartsinventory.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.automatictransmissionpartsinventory.entity.AutomativePart;
import com.example.automatictransmissionpartsinventory.exception.ServiceException;
import com.example.automatictransmissionpartsinventory.service.AutomaticPartService;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AT部品データのCSV処理サービス
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutomaticPartCsvService {

    private final AutomaticPartService automaticPartService;
    
    // CSV関連の定数
    private static final String[] CSV_HEADERS = {
        "部品番号", "部品名", "価格", "説明", "メーカー名"
    };
    
    private static final String CSV_CHARSET = "UTF-8";
    private static final char CSV_SEPARATOR = ',';
    
    /**
     * 部品データをCSV形式でエクスポートする
     * @return CSVファイルのバイト配列
     * @throws ServiceException CSV生成に失敗した場合
     */
    public byte[] exportPartsToCSV() throws ServiceException {
        log.info("CSV エクスポート処理開始");
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(baos, CSV_CHARSET);
             CSVWriter csvWriter = new CSVWriter(osw, 
                 CSV_SEPARATOR,     // separator
                 CSVWriter.DEFAULT_QUOTE_CHARACTER,  // quote character
                 CSVWriter.DEFAULT_ESCAPE_CHARACTER, // escape character
                 CSVWriter.DEFAULT_LINE_END)) {     // line end
            
            // ヘッダー行の書き込み
            csvWriter.writeNext(CSV_HEADERS);
            
            // 全部品データの取得と書き込み
            List<AutomativePart> parts = automaticPartService.findAllParts();
            
            for (AutomativePart part : parts) {
                String[] record = {
                    part.getPartNumber(),
                    part.getPartName(),
                    part.getPrice() != null ? part.getPrice().toString() : "",
                    part.getDescription() != null ? part.getDescription() : "",
                    part.getManufacturer() != null ? part.getManufacturer() : ""
                };
                csvWriter.writeNext(record);
            }
            
            csvWriter.flush();
            
            log.info("CSV エクスポート処理完了: {}件のデータをエクスポート", parts.size());
            return baos.toByteArray();
            
        } catch (IOException e) {
            log.error("CSV エクスポート処理でエラーが発生しました", e);
            throw new ServiceException("CSVエクスポートに失敗しました: " + e.getMessage());
        }
    }
    
    /**
     * CSVファイルから部品データをインポートする
     * @param file アップロードされたCSVファイル
     * @return インポート結果
     * @throws ServiceException CSVインポートに失敗した場合
     */
    public CsvImportResult importPartsFromCSV(MultipartFile file) throws ServiceException {
        log.info("CSV インポート処理開始: ファイル名={}", file.getOriginalFilename());
        
        // ファイルの基本チェック
        validateCsvFile(file);
        
        CsvImportResult result = new CsvImportResult();
        
        try (InputStreamReader isr = new InputStreamReader(file.getInputStream(), CSV_CHARSET);
             CSVReader csvReader = new CSVReader(isr)) {
            
            List<String[]> records = csvReader.readAll();
            
            if (records.isEmpty()) {
                throw new ServiceException("CSVファイルが空です");
            }
            
            // ヘッダー行の検証
            validateCsvHeaders(records.get(0));
            
            // データ行の処理（ヘッダー行をスキップ）
            for (int i = 1; i < records.size(); i++) {
                try {
                    String[] record = records.get(i);
                    AutomativePart part = parseCSVRecord(record, i + 1);
                    
                    // 重複チェック
                    if (automaticPartService.findByPartNumber(part.getPartNumber()).isPresent()) {
                        result.addSkippedRow(i + 1, "部品番号が既に存在します: " + part.getPartNumber());
                        continue;
                    }
                    
                    // 部品の登録
                    automaticPartService.registerPart(part);
                    result.incrementSuccessCount();
                    
                } catch (Exception e) {
                    log.warn("CSV行{}の処理でエラーが発生: {}", i + 1, e.getMessage());
                    result.addErrorRow(i + 1, e.getMessage());
                }
            }
            
            log.info("CSV インポート処理完了: 成功={}, エラー={}, スキップ={}",
                    result.getSuccessCount(), result.getErrorCount(), result.getSkipCount());
            
            return result;
            
        } catch (IOException | CsvException e) {
            log.error("CSV インポート処理でエラーが発生しました", e);
            throw new ServiceException("CSVインポートに失敗しました: " + e.getMessage());
        }
    }
    
    /**
     * アップロードされたファイルの基本バリデーション
     */
    private void validateCsvFile(MultipartFile file) throws ServiceException {
        if (file.isEmpty()) {
            throw new ServiceException("ファイルが選択されていません");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".csv")) {
            throw new ServiceException("CSVファイルを選択してください");
        }
        
        // ファイルサイズチェック（5MBまで）
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new ServiceException("ファイルサイズが大きすぎます（5MB以下にしてください）");
        }
    }
    
    /**
     * CSVヘッダー行の検証
     */
    private void validateCsvHeaders(String[] headers) throws ServiceException {
        if (headers.length < CSV_HEADERS.length) {
            throw new ServiceException("CSVファイルの形式が正しくありません。必要な列数: " + CSV_HEADERS.length);
        }
        
        // 必須列の存在チェック（部分的な一致も許可）
        for (int i = 0; i < Math.min(3, headers.length); i++) { // 最初の3列（部品番号、部品名、価格）は必須
            if (headers[i] == null || headers[i].trim().isEmpty()) {
                throw new ServiceException("必須の列（" + CSV_HEADERS[i] + "）が不足しています");
            }
        }
    }
    
    /**
     * CSV行データをAutomaticPartオブジェクトに変換
     */
    private AutomativePart parseCSVRecord(String[] record, int rowNumber) throws ServiceException {
        if (record.length < 3) {
            throw new ServiceException("必要な列数が不足しています");
        }
        
        try {
            AutomativePart part = new AutomativePart();
            
            // 必須項目
            part.setPartNumber(validateAndTrim(record[0], "部品番号"));
            part.setPartName(validateAndTrim(record[1], "部品名"));
            
            // 価格の解析
            String priceStr = validateAndTrim(record[2], "価格");
            try {
                part.setPrice(new BigDecimal(priceStr));
            } catch (NumberFormatException e) {
                throw new ServiceException("価格の形式が正しくありません: " + priceStr);
            }
            
            // オプション項目
            if (record.length > 3 && record[3] != null) {
                part.setDescription(record[3].trim().isEmpty() ? null : record[3].trim());
            }
            
            if (record.length > 4 && record[4] != null) {
                part.setManufacturer(record[4].trim().isEmpty() ? null : record[4].trim());
            }
            
            // 作成・更新日時の設定
            LocalDateTime now = LocalDateTime.now();
            part.setCreatedAt(now);
            part.setUpdatedAt(now);
            
            return part;
            
        } catch (ServiceException e) {
            throw e; // ServiceExceptionはそのまま再スロー
        } catch (Exception e) {
            throw new ServiceException("行" + rowNumber + "の解析に失敗しました: " + e.getMessage());
        }
    }
    
    /**
     * 文字列フィールドのバリデーションとトリム
     */
    private String validateAndTrim(String value, String fieldName) throws ServiceException {
        if (value == null || value.trim().isEmpty()) {
            throw new ServiceException(fieldName + "は必須です");
        }
        return value.trim();
    }
    
    /**
     * CSVインポート結果を格納するクラス
     */
    public static class CsvImportResult {
        private int successCount = 0;
        private int errorCount = 0;
        private int skipCount = 0;
        private List<String> errorMessages = new ArrayList<>();
        private List<String> skippedMessages = new ArrayList<>();
        
        public void incrementSuccessCount() {
            this.successCount++;
        }
        
        public void addErrorRow(int rowNumber, String message) {
            this.errorCount++;
            this.errorMessages.add("行" + rowNumber + ": " + message);
        }
        
        public void addSkippedRow(int rowNumber, String message) {
            this.skipCount++;
            this.skippedMessages.add("行" + rowNumber + ": " + message);
        }
        
        public int getSuccessCount() { return successCount; }
        public int getErrorCount() { return errorCount; }
        public int getSkipCount() { return skipCount; }
        public int getTotalCount() { return successCount + errorCount + skipCount; }
        public List<String> getErrorMessages() { return errorMessages; }
        public List<String> getSkippedMessages() { return skippedMessages; }
        
        public boolean hasErrors() { return errorCount > 0; }
        public boolean hasSkipped() { return skipCount > 0; }
        public boolean isSuccess() { return errorCount == 0; }
    }
}