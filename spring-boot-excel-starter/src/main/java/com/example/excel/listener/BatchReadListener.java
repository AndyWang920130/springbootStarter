package com.example.excel.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;

/**
 * A generic EasyExcel {@link ReadListener} that buffers parsed rows and flushes
 * them to a caller-supplied {@link Consumer} in batches. This keeps memory usage
 * bounded when importing large files.
 *
 * <p>The listener is stateful and therefore must not be shared across concurrent
 * read operations; create a new instance per read.
 *
 * @param <T> the model type each row is parsed into
 */
public class BatchReadListener<T> implements ReadListener<T> {

    private final int batchSize;
    private final Consumer<List<T>> batchConsumer;

    private List<T> buffer;

    public BatchReadListener(int batchSize, Consumer<List<T>> batchConsumer) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be positive, got " + batchSize);
        }
        if (batchConsumer == null) {
            throw new IllegalArgumentException("batchConsumer must not be null");
        }
        this.batchSize = batchSize;
        this.batchConsumer = batchConsumer;
        this.buffer = ListUtils.newArrayListWithExpectedSize(batchSize);
    }

    @Override
    public void invoke(T data, AnalysisContext context) {
        buffer.add(data);
        if (buffer.size() >= batchSize) {
            flush();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (!buffer.isEmpty()) {
            flush();
        }
    }

    private void flush() {
        // Hand off a defensive copy so the consumer owns its batch, then reset.
        batchConsumer.accept(new ArrayList<>(buffer));
        buffer = ListUtils.newArrayListWithExpectedSize(batchSize);
    }
}
