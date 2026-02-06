package org.uit.utimea.shared.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of Excel import operation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelImportResult {
    
    @Builder.Default
    private int successCount = 0;
    
    @Builder.Default
    private int failureCount = 0;
    
    @Builder.Default
    private int totalCount = 0;
    
    @Builder.Default
    private List<ExcelValidationError> errors = new ArrayList<>();
    
    public void addError(ExcelValidationError error) {
        this.errors.add(error);
        this.failureCount++;
    }
    
    public void incrementSuccess() {
        this.successCount++;
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
