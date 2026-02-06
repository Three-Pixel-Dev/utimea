package org.uit.utimea.shared.excel;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Generic interface for Excel import/export operations.
 * This interface can be implemented for any entity type (Teacher, Student, etc.)
 * 
 * @param <REQUEST> The request DTO type for creating entities
 * @param <RESPONSE> The response DTO type
 * @param <EXCEL_DTO> The DTO type used for Excel operations
 */
public interface ExcelService<REQUEST, RESPONSE, EXCEL_DTO> {

    /**
     * Generates an Excel template file for the entity.
     * 
     * @return InputStream of the Excel template file
     */
    InputStream generateTemplate();

    /**
     * Exports all entities to Excel format.
     * Uses multithreading for better performance with large datasets.
     * 
     * @return InputStream of the Excel export file
     */
    CompletableFuture<InputStream> exportToExcel();

    /**
     * Imports entities from an Excel file.
     * Uses batch processing with thread pool for optimal performance.
     * 
     * @param file The Excel file to import
     * @return ExcelImportResult containing success count, failure count, and errors
     */
    CompletableFuture<ExcelImportResult> importFromExcel(MultipartFile file);

    /**
     * Validates Excel data before import.
     * 
     * @param excelData List of Excel DTOs to validate
     * @return List of validation errors
     */
    List<ExcelValidationError> validateExcelData(List<EXCEL_DTO> excelData);

    /**
     * Converts Excel DTO to Request DTO for entity creation.
     * 
     * @param excelDto The Excel DTO
     * @return The Request DTO
     */
    REQUEST convertExcelDtoToRequest(EXCEL_DTO excelDto);

    /**
     * Converts entity Response DTO to Excel DTO for export.
     * 
     * @param response The Response DTO
     * @return The Excel DTO
     */
    EXCEL_DTO convertResponseToExcelDto(RESPONSE response);
}
