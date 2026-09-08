package com.example.excel.exception;

/**
 * Runtime exception thrown when an Excel import/export operation fails.
 */
public class ExcelException extends RuntimeException {

    public ExcelException(String message) {
        super(message);
    }

    public ExcelException(String message, Throwable cause) {
        super(message, cause);
    }
}
