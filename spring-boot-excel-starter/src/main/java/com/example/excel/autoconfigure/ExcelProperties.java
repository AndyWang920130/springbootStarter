package com.example.excel.autoconfigure;

import java.nio.charset.StandardCharsets;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Excel starter.
 *
 * <p>All properties are bound under the {@code excel} prefix, e.g.:
 * <pre>
 * excel:
 *   enabled: true
 *   default-sheet-name: Sheet1
 *   batch-size: 1000
 * </pre>
 */
@ConfigurationProperties(prefix = ExcelProperties.PREFIX)
public class ExcelProperties {

    public static final String PREFIX = "excel";

    /**
     * Whether the Excel auto-configuration is enabled.
     */
    private boolean enabled = true;

    /**
     * Default sheet name used when writing if no explicit name is supplied.
     */
    private String defaultSheetName = "Sheet1";

    /**
     * Charset used for download headers and CSV handling.
     */
    private String charset = StandardCharsets.UTF_8.name();

    /**
     * Batch size used by the batch read listener when streaming large files.
     */
    private int batchSize = 1000;

    /**
     * Whether EasyExcel should automatically close the underlying stream after
     * a read/write operation completes.
     */
    private boolean autoCloseStream = true;

    /**
     * Whether the first row (headers) should be included / expected. This maps to
     * EasyExcel's {@code head} handling; the number of head rows to skip on read.
     */
    private int headRowNumber = 1;

    /**
     * Default file extension/type used for writing when not explicitly provided.
     */
    private ExcelType excelType = ExcelType.XLSX;

    public enum ExcelType {
        XLSX,
        XLS,
        CSV
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDefaultSheetName() {
        return defaultSheetName;
    }

    public void setDefaultSheetName(String defaultSheetName) {
        this.defaultSheetName = defaultSheetName;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public boolean isAutoCloseStream() {
        return autoCloseStream;
    }

    public void setAutoCloseStream(boolean autoCloseStream) {
        this.autoCloseStream = autoCloseStream;
    }

    public int getHeadRowNumber() {
        return headRowNumber;
    }

    public void setHeadRowNumber(int headRowNumber) {
        this.headRowNumber = headRowNumber;
    }

    public ExcelType getExcelType() {
        return excelType;
    }

    public void setExcelType(ExcelType excelType) {
        this.excelType = excelType;
    }
}
