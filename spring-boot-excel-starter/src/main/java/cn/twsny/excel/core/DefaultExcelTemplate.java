package cn.twsny.excel.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.function.Consumer;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;

import cn.twsny.excel.autoconfigure.ExcelProperties;
import cn.twsny.excel.exception.ExcelException;
import cn.twsny.excel.listener.BatchReadListener;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Default {@link ExcelTemplate} implementation delegating to Alibaba EasyExcel and
 * applying defaults from {@link ExcelProperties}.
 */
public class DefaultExcelTemplate implements ExcelTemplate {

    private static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ExcelProperties properties;

    public DefaultExcelTemplate(ExcelProperties properties) {
        this.properties = properties;
    }

    // ------------------------------------------------------------------
    // Reading
    // ------------------------------------------------------------------

    @Override
    public <T> List<T> read(InputStream inputStream, Class<T> clazz) {
        return read(inputStream, clazz, 0);
    }

    @Override
    public <T> List<T> read(InputStream inputStream, Class<T> clazz, Integer sheetNo) {
        try {
            return EasyExcel.read(inputStream, clazz, null)
                    .autoCloseStream(properties.isAutoCloseStream())
                    .sheet(sheetNo)
                    .headRowNumber(properties.getHeadRowNumber())
                    .doReadSync();
        } catch (Exception ex) {
            throw new ExcelException("Failed to read Excel data into " + clazz.getName(), ex);
        }
    }

    @Override
    public <T> void readInBatch(InputStream inputStream, Class<T> clazz, Consumer<List<T>> batchConsumer) {
        try {
            BatchReadListener<T> listener = new BatchReadListener<>(properties.getBatchSize(), batchConsumer);
            EasyExcel.read(inputStream, clazz, listener)
                    .autoCloseStream(properties.isAutoCloseStream())
                    .sheet()
                    .headRowNumber(properties.getHeadRowNumber())
                    .doRead();
        } catch (Exception ex) {
            throw new ExcelException("Failed to batch-read Excel data into " + clazz.getName(), ex);
        }
    }

    // ------------------------------------------------------------------
    // Writing
    // ------------------------------------------------------------------

    @Override
    public <T> void write(OutputStream outputStream, List<T> data, Class<T> clazz) {
        write(outputStream, data, clazz, properties.getDefaultSheetName());
    }

    @Override
    public <T> void write(OutputStream outputStream, List<T> data, Class<T> clazz, String sheetName) {
        try {
            writerBuilder(outputStream, clazz)
                    .sheet(sheetName)
                    .doWrite(data);
        } catch (Exception ex) {
            throw new ExcelException("Failed to write Excel data from " + clazz.getName(), ex);
        }
    }

    @Override
    public <T> void writeToResponse(HttpServletResponse response, String fileName, List<T> data, Class<T> clazz) {
        writeToResponse(response, fileName, properties.getDefaultSheetName(), data, clazz);
    }

    @Override
    public <T> void writeToResponse(HttpServletResponse response, String fileName, String sheetName,
                                    List<T> data, Class<T> clazz) {
        try {
            Charset charset = Charset.forName(properties.getCharset());
            response.setContentType(XLSX_CONTENT_TYPE);
            response.setCharacterEncoding(charset.name());

            String encoded = URLEncoder.encode(fileName, charset).replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition",
                    "attachment;filename*=" + charset.name().toLowerCase() + "''" + encoded + ".xlsx");

            // Do not auto-close the servlet output stream; the container owns it.
            EasyExcel.write(response.getOutputStream(), clazz)
                    .excelType(ExcelTypeEnum.XLSX)
                    .autoCloseStream(false)
                    .sheet(sheetName)
                    .doWrite(data);
        } catch (Exception ex) {
            throw new ExcelException("Failed to write Excel data to HTTP response", ex);
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private <T> ExcelWriterBuilder writerBuilder(OutputStream outputStream, Class<T> clazz) {
        return EasyExcel.write(outputStream, clazz)
                .excelType(toExcelTypeEnum(properties.getExcelType()))
                .autoCloseStream(properties.isAutoCloseStream());
    }

    private ExcelTypeEnum toExcelTypeEnum(ExcelProperties.ExcelType type) {
        return switch (type) {
            case XLS -> ExcelTypeEnum.XLS;
            case CSV -> ExcelTypeEnum.CSV;
            default -> ExcelTypeEnum.XLSX;
        };
    }
}
