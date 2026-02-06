package org.uit.utimea.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Configuration for Excel processing with thread pool and batch size optimization
 * based on available CPU cores.
 */
@Configuration
@EnableAsync
public class ExcelConfig {

    @Value("${excel.thread-pool.core-size:0}")
    private int corePoolSize;

    @Value("${excel.thread-pool.max-size:0}")
    private int maxPoolSize;

    @Value("${excel.batch-size:0}")
    private int batchSize;

    /**
     * Creates a thread pool executor optimized for CPU-bound Excel processing.
     * Pool size is calculated based on available CPU cores if not specified.
     * 
     * Formula: optimal pool size = CPU cores + 1 (for I/O wait)
     */
    @Bean(name = "excelThreadPool")
    public ExecutorService excelThreadPool() {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        
        // Use configured values or calculate based on CPU cores
        int coreSize = corePoolSize > 0 ? corePoolSize : availableProcessors;
        int maxSize = maxPoolSize > 0 ? maxPoolSize : (availableProcessors * 2);
        
        return Executors.newFixedThreadPool(coreSize, r -> {
            Thread thread = new Thread(r, "excel-processor-" + System.currentTimeMillis());
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Gets the optimal batch size for processing Excel rows.
     * Default is calculated as: (CPU cores * 100) for optimal throughput
     */
    @Bean(name = "excelBatchSize")
    public int excelBatchSize() {
        if (batchSize > 0) {
            return batchSize;
        }
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        return Math.max(100, availableProcessors * 100);
    }

    /**
     * Gets the optimal thread pool size for the current system
     */
    public int getOptimalPoolSize() {
        if (corePoolSize > 0) {
            return corePoolSize;
        }
        return Runtime.getRuntime().availableProcessors();
    }
}
