package org.uit.utimea.shared.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a validation error in Excel import
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelValidationError {
    
    /**
     * Row number in Excel (1-based)
     */
    private int rowNumber;
    
    /**
     * Column name or identifier
     */
    private String column;
    
    /**
     * Error message
     */
    private String message;
    
    /**
     * The value that caused the error (optional)
     */
    private String invalidValue;
}
