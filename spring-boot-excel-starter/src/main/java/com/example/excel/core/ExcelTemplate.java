package com.example.excel.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Consumer;

import jakarta.servlet.http.HttpServletResponse;

/**
 * High-level facade for reading and writing Excel files, backed by Alibaba EasyExcel.
 *
 * <p>A singleton bean of this type is auto-configured and can be injected wherever
 * import/export is needed.
 */
public interface ExcelTemplate {

    // ------------------------------------------------------------------
    // Reading (import)
    // ------------------------------------------------------------------

    /**
     * Read all rows from the given input stream into a list, using the first sheet.
     *
     * @param inputStream source stream (not closed unless {@code auto-close-stream} is set)
     * @param clazz       model type for each row
     * @param <T>         model type
     * @return all parsed rows
     */
    <T> List<T> read(InputStream inputStream, Class<T> clazz);

    /**
     * Read all rows from a specific sheet into a list.
     *
     * @param inputStream source stream
     * @param clazz       model type for each row
     * @param sheetNo     zero-based sheet index
     */
    <T> List<T> read(InputStream inputStream, Class<T> clazz, Integer sheetNo);

    /**
     * Stream rows from the first sheet, invoking {@code batchConsumer} once per batch.
     * Suitable for very large files as it never materialises the whole file in memory.
     *
     * @param inputStream   source stream
     * @param clazz         model type for each row
     * @param batchConsumer callback receiving each batch of rows
     */
    <T> void readInBatch(InputStream inputStream, Class<T> clazz, Consumer<List<T>> batchConsumer);

    // ------------------------------------------------------------------
    // Writing (export)
    // ------------------------------------------------------------------

    /**
     * Write data to an output stream using the default sheet name.
     *
     * @param outputStream target stream
     * @param data         rows to write
     * @param clazz        model type describing the columns
     */
    <T> void write(OutputStream outputStream, List<T> data, Class<T> clazz);

    /**
     * Write data to an output stream using an explicit sheet name.
     */
    <T> void write(OutputStream outputStream, List<T> data, Class<T> clazz, String sheetName);

    /**
     * Write data directly to an HTTP response as a downloadable {@code .xlsx} file.
     * Sets the appropriate content type and {@code Content-Disposition} headers.
     *
     * @param response the servlet response
     * @param fileName file name without extension (extension is added automatically)
     * @param data     rows to write
     * @param clazz    model type describing the columns
     */
    <T> void writeToResponse(HttpServletResponse response, String fileName, List<T> data, Class<T> clazz);

    /**
     * Write data directly to an HTTP response, specifying the sheet name.
     */
    <T> void writeToResponse(HttpServletResponse response, String fileName, String sheetName,
                             List<T> data, Class<T> clazz);
}
