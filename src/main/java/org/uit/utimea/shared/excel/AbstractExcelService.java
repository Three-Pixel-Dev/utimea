package org.uit.utimea.shared.excel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Abstract base implementation of ExcelService with common functionality.
 * Provides template generation, batch processing, and multithreading support.
 * 
 * Subclasses need to implement entity-specific conversion and validation logic.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractExcelService<REQUEST, RESPONSE, EXCEL_DTO> implements ExcelService<REQUEST, RESPONSE, EXCEL_DTO> {

    protected final ExecutorService threadPool;
    protected final int batchSize;

    /**
     * Gets the column headers for the Excel template
     */
    protected abstract List<String> getColumnHeaders();

    /**
     * Gets the column width for each column (optional, defaults to 15)
     */
    protected List<Integer> getColumnWidths() {
        List<String> headers = getColumnHeaders();
        return headers.stream().map(h -> 15).toList();
    }

    /**
     * Gets the sheet name for the Excel file
     */
    protected abstract String getSheetName();

    /**
     * Reads Excel data from file and converts to Excel DTOs
     */
    protected abstract List<EXCEL_DTO> readExcelData(Workbook workbook) throws IOException;

    /**
     * Writes Excel data to workbook
     */
    protected abstract void writeExcelData(Workbook workbook, List<EXCEL_DTO> data);

    @Override
    public InputStream generateTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(getSheetName());

            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(workbook);
            List<String> headers = getColumnHeaders();
            List<Integer> widths = getColumnWidths();
            
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, widths.get(i) * 256);
            }

            sheet.createFreezePane(0, 1);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
            
        } catch (IOException e) {
            log.error("Error generating Excel template", e);
            throw new RuntimeException("Failed to generate Excel template", e);
        }
    }

    @Override
    public CompletableFuture<InputStream> exportToExcel() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<RESPONSE> allData = fetchAllData();
                List<EXCEL_DTO> excelData = allData.stream()
                        .map(this::convertResponseToExcelDto)
                        .toList();
                
                return createExcelWorkbook(excelData);
            } catch (Exception e) {
                log.error("Error exporting to Excel", e);
                throw new RuntimeException("Failed to export to Excel", e);
            }
        }, threadPool);
    }

    @Override
    public CompletableFuture<ExcelImportResult> importFromExcel(MultipartFile file) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Workbook workbook = new XSSFWorkbook(file.getInputStream());
                List<EXCEL_DTO> excelData = readExcelData(workbook);
                workbook.close();

                List<ExcelValidationError> validationErrors = validateExcelData(excelData);
                
                ExcelImportResult result = ExcelImportResult.builder()
                        .totalCount(excelData.size())
                        .errors(validationErrors)
                        .failureCount(validationErrors.size())
                        .build();
                
                if (!validationErrors.isEmpty()) {
                    log.warn("Excel import validation failed with {} errors", validationErrors.size());
                    return result;
                }

                int totalBatches = (int) Math.ceil((double) excelData.size() / batchSize);
                log.info("Starting Excel import: {} rows, {} batches, batch size: {}", 
                        excelData.size(), totalBatches, batchSize);
                
                List<CompletableFuture<BatchProcessResult>> batchFutures = new ArrayList<>();
                
                for (int i = 0; i < excelData.size(); i += batchSize) {
                    int start = i;
                    int end = Math.min(i + batchSize, excelData.size());
                    List<EXCEL_DTO> batch = excelData.subList(start, end);
                    int batchNumber = (i / batchSize) + 1;
                    
                    CompletableFuture<BatchProcessResult> batchFuture = CompletableFuture.supplyAsync(() -> {
                        return processBatch(batch, start + 1);
                    }, threadPool);
                    
                    batchFutures.add(batchFuture);
                }

                CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture[0])).join();
                
                for (CompletableFuture<BatchProcessResult> future : batchFutures) {
                    BatchProcessResult batchResult = future.join();
                    result.setSuccessCount(result.getSuccessCount() + batchResult.successCount);
                    result.setFailureCount(result.getFailureCount() + batchResult.failureCount);
                    result.getErrors().addAll(batchResult.errors);
                }
                
                log.info("Excel import completed: {} success, {} failures out of {} total", 
                        result.getSuccessCount(), result.getFailureCount(), result.getTotalCount());
                
                return result;
                
            } catch (IOException e) {
                log.error("Error reading Excel file", e);
                ExcelImportResult errorResult = ExcelImportResult.builder()
                        .totalCount(0)
                        .failureCount(1)
                        .build();
                errorResult.addError(ExcelValidationError.builder()
                        .rowNumber(0)
                        .column("File")
                        .message("Failed to read Excel file: " + e.getMessage())
                        .build());
                return errorResult;
            }
        }, threadPool);
    }

    /**
     * Fetches all data for export (to be implemented by subclasses)
     */
    protected abstract List<RESPONSE> fetchAllData();

    /**
     * Processes a batch of Excel DTOs and returns the result
     */
    protected BatchProcessResult processBatch(List<EXCEL_DTO> batch, int startRowNumber) {
        String threadName = Thread.currentThread().getName();
        log.info("Processing batch starting at row {} on thread: {}", startRowNumber, threadName);
        
        int successCount = 0;
        int failureCount = 0;
        List<ExcelValidationError> errors = new ArrayList<>();
        
        for (int i = 0; i < batch.size(); i++) {
            EXCEL_DTO excelDto = batch.get(i);
            int rowNumber = startRowNumber + i;
            
            try {
                REQUEST request = convertExcelDtoToRequest(excelDto);
                createEntity(request);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                errors.add(ExcelValidationError.builder()
                        .rowNumber(rowNumber)
                        .message("Failed to create entity: " + e.getMessage())
                        .build());
                log.error("Error processing row {} on thread {}: {}", rowNumber, threadName, e.getMessage());
            }
        }
        
        log.info("Batch completed on thread {}: {} success, {} failures (rows {}-{})", 
                threadName, successCount, failureCount, startRowNumber, startRowNumber + batch.size() - 1);
        
        return new BatchProcessResult(successCount, failureCount, errors);
    }

    /**
     * Creates an entity from request (to be implemented by subclasses)
     */
    protected abstract void createEntity(REQUEST request);

    /**
     * Creates Excel workbook from data
     */
    protected InputStream createExcelWorkbook(List<EXCEL_DTO> data) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(getSheetName());

            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(workbook);
            List<String> headers = getColumnHeaders();
            List<Integer> widths = getColumnWidths();
            
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, widths.get(i) * 256);
            }

            writeExcelData(workbook, data);

            sheet.createFreezePane(0, 1);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        }
    }

    /**
     * Creates header cell style
     */
    protected CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * Inner class for batch processing results
     */
    protected static class BatchProcessResult {
        final int successCount;
        final int failureCount;
        final List<ExcelValidationError> errors;

        BatchProcessResult(int successCount, int failureCount, List<ExcelValidationError> errors) {
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.errors = errors;
        }
    }
}
