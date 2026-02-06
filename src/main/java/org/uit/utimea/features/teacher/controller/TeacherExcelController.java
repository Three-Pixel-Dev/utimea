package org.uit.utimea.features.teacher.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.uit.utimea.shared.dto.response.ApiResponse;
import org.uit.utimea.shared.excel.ExcelImportResult;
import org.uit.utimea.shared.util.ApiResponseUtil;
import org.uit.utimea.features.teacher.service.TeacherExcelService;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for Teacher Excel import/export operations
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teachers/excel")
public class TeacherExcelController {

    private final TeacherExcelService teacherExcelService;

    /**
     * Downloads Excel template for teachers
     */
    @GetMapping("/template")
    public ResponseEntity<InputStreamResource> downloadTemplate(HttpServletRequest httpServletRequest) {
        InputStream templateStream = teacherExcelService.generateTemplate();
        InputStreamResource resource = new InputStreamResource(templateStream);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=teachers_template.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /**
     * Exports all teachers to Excel
     */
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportToExcel(HttpServletRequest httpServletRequest) {
        CompletableFuture<InputStream> exportFuture = teacherExcelService.exportToExcel();
        
        try {
            InputStream exportStream = exportFuture.get(); // Wait for completion
            InputStreamResource resource = new InputStreamResource(exportStream);
            
            String filename = "teachers_export_" + java.time.LocalDate.now() + ".xlsx";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Imports teachers from Excel file
     */
    @PostMapping("/import")
    public ResponseEntity<ApiResponse> importFromExcel(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest httpServletRequest) {
        
        if (file.isEmpty()) {
            ApiResponse errorResponse = ApiResponseUtil.error(
                    "File is empty",
                    HttpStatus.BAD_REQUEST,
                    httpServletRequest
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        CompletableFuture<ExcelImportResult> importFuture = teacherExcelService.importFromExcel(file);
        
        try {
            ExcelImportResult result = importFuture.get();

            if (result.getSuccessCount() == 0 && result.getFailureCount() > 0) {
                ApiResponse errorResponse = ApiResponseUtil.error(
                        String.format("Import failed: All %d row(s) had errors", result.getFailureCount()),
                        HttpStatus.PARTIAL_CONTENT,
                        result,
                        httpServletRequest
                );
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(errorResponse);
            }

            if (result.getSuccessCount() > 0 && result.getFailureCount() > 0) {
                ApiResponse partialResponse = ApiResponseUtil.success(
                        result,
                        String.format("Partially imported: %d succeeded, %d failed", 
                                result.getSuccessCount(), result.getFailureCount()),
                        httpServletRequest
                );
                return ResponseEntity.ok(partialResponse);
            }

            ApiResponse successResponse = ApiResponseUtil.success(
                    result,
                    String.format("Successfully imported %d teacher(s)", result.getSuccessCount()),
                    httpServletRequest
            );
            return ResponseEntity.ok(successResponse);
            
        } catch (Exception e) {
            ApiResponse errorResponse = ApiResponseUtil.error(
                    "Failed to import teachers: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    httpServletRequest
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
