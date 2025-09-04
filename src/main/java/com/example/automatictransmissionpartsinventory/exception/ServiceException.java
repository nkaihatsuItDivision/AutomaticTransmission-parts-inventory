package com.example.automatictransmissionpartsinventory.exception;

/**
 * サービス層専用の例外クラス
 * ビジネスロジック処理でのエラーをハンドリング
 */
public class ServiceException extends Exception {

    private static final long serialVersionUID = 1L;
    
    // エラーコード（エラーの種類を分類）
    private final String errorCode;

    /**
     * デフォルトコンストラクタ
     */
    public ServiceException() {
        super();
        this.errorCode = "GENERAL_ERROR";
    }

    /**
     * メッセージ付きコンストラクタ
     * @param message エラーメッセージ
     */
    public ServiceException(String message) {
        super(message);
        this.errorCode = "GENERAL_ERROR";
    }

    /**
     * メッセージとエラーコード付きコンストラクタ
     * @param message エラーメッセージ
     * @param errorCode エラーコード
     */
    public ServiceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * メッセージと原因例外付きコンストラクタ
     * @param message エラーメッセージ
     * @param cause 原因となった例外
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "GENERAL_ERROR";
    }

    /**
     * メッセージ、エラーコード、原因例外付きコンストラクタ
     * @param message エラーメッセージ
     * @param errorCode エラーコード
     * @param cause 原因となった例外
     */
    public ServiceException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 原因例外付きコンストラクタ
     * @param cause 原因となった例外
     */
    public ServiceException(Throwable cause) {
        super(cause);
        this.errorCode = "GENERAL_ERROR";
    }

    /**
     * エラーコードを取得
     * @return エラーコード
     */
    public String getErrorCode() {
        return errorCode;
    }

    // よく使用されるエラーコード定数
    public static final String PART_NOT_FOUND = "PART_NOT_FOUND";
    public static final String DUPLICATE_PART_NUMBER = "DUPLICATE_PART_NUMBER";
    public static final String INVALID_PART_DATA = "INVALID_PART_DATA";
    public static final String DATABASE_ERROR = "DATABASE_ERROR";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
}