# Excel Import/Export Design Pattern

This document describes the reusable design pattern for implementing Excel import/export functionality across different features.

## Architecture Overview

The pattern follows a **Strategy + Template Method** design pattern with multithreading support:

1. **ExcelConfig** - Configuration for thread pool and batch size
2. **ExcelService Interface** - Generic interface for Excel operations
3. **AbstractExcelService** - Base implementation with common functionality
4. **Feature-specific Service** - Implements entity-specific logic
5. **Excel Controller** - REST endpoints for Excel operations

## Components

### 1. ExcelConfig (`shared/config/ExcelConfig.java`)

Configures thread pool and batch size based on CPU cores:
- **Thread Pool**: Auto-calculates based on available CPU cores (default: CPU cores)
- **Batch Size**: Auto-calculates based on CPU cores (default: CPU cores × 100)
- Configurable via `application.yml`:
  ```yaml
  excel:
    thread-pool:
      core-size: 0  # 0 = auto-calculate
      max-size: 0   # 0 = auto-calculate
    batch-size: 0   # 0 = auto-calculate
  ```

### 2. ExcelService Interface (`shared/excel/ExcelService.java`)

Generic interface defining Excel operations:
- `generateTemplate()` - Creates Excel template
- `exportToExcel()` - Exports all entities (async)
- `importFromExcel()` - Imports from file (async with batch processing)
- `validateExcelData()` - Validates Excel data
- Conversion methods between Excel DTO and Request/Response DTOs

### 3. AbstractExcelService (`shared/excel/AbstractExcelService.java`)

Base implementation providing:
- Template generation with headers
- Multithreaded batch processing
- Excel file reading/writing
- Error handling and validation framework

**Key Features:**
- Uses thread pool for parallel processing
- Processes data in batches for optimal performance
- Handles large datasets efficiently
- Provides validation framework

### 4. Feature-Specific Implementation

Example: `TeacherExcelService`

**Required Methods:**
- `getColumnHeaders()` - Returns column names for Excel
- `getColumnWidths()` - Returns column widths (optional)
- `getSheetName()` - Returns sheet name
- `readExcelData()` - Reads data from Excel file
- `writeExcelData()` - Writes data to Excel file
- `fetchAllData()` - Fetches all entities for export
- `createEntity()` - Creates entity from request
- `validateExcelData()` - Validates Excel data
- `convertExcelDtoToRequest()` - Converts Excel DTO to Request
- `convertResponseToExcelDto()` - Converts Response to Excel DTO

### 5. Excel Controller

Example: `TeacherExcelController`

**Endpoints:**
- `GET /api/{feature}/excel/template` - Download template
- `GET /api/{feature}/excel/export` - Export all data
- `POST /api/{feature}/excel/import` - Import from file

## How to Add Excel Support to a New Feature

### Step 1: Create Excel DTO

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YourFeatureExcelDTO {
    private String field1;
    private String field2;
    // ... other fields
}
```

### Step 2: Create Excel Service

```java
@Service
public class YourFeatureExcelService extends AbstractExcelService<YourRequest, YourResponse, YourFeatureExcelDTO> {
    
    private final YourFeatureService yourFeatureService;
    // ... other dependencies
    
    public YourFeatureExcelService(
            YourFeatureService yourFeatureService,
            // ... other dependencies
            @Qualifier("excelThreadPool") ExecutorService excelThreadPool,
            ExcelConfig excelConfig) {
        super(excelThreadPool, excelConfig.excelBatchSize());
        this.yourFeatureService = yourFeatureService;
        // ... initialize other fields
    }
    
    @Override
    protected List<String> getColumnHeaders() {
        return List.of("Field1", "Field2", ...);
    }
    
    @Override
    protected String getSheetName() {
        return "YourFeature";
    }
    
    // Implement other required methods...
}
```

### Step 3: Create Excel Controller

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/your-feature/excel")
public class YourFeatureExcelController {
    
    private final YourFeatureExcelService excelService;
    
    @GetMapping("/template")
    public ResponseEntity<InputStreamResource> downloadTemplate() {
        // Implementation similar to TeacherExcelController
    }
    
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportToExcel() {
        // Implementation similar to TeacherExcelController
    }
    
    @PostMapping("/import")
    public ResponseEntity<ApiResponse> importFromExcel(@RequestParam("file") MultipartFile file) {
        // Implementation similar to TeacherExcelController
    }
}
```

### Step 4: Update Frontend Service

Add methods to your feature service:

```typescript
downloadTemplate: async (): Promise<void> => {
  const response = await apiClient.get('/api/your-feature/excel/template', {
    responseType: 'blob',
  })
  // Download logic...
}

exportToExcel: async (): Promise<void> => {
  const response = await apiClient.get('/api/your-feature/excel/export', {
    responseType: 'blob',
  })
  // Export logic...
}

importFromExcel: async (file: File): Promise<void> => {
  const formData = new FormData()
  formData.append('file', file)
  await apiClient.post('/api/your-feature/excel/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
```

### Step 5: Add UI Buttons

Update your feature index page to include Excel buttons (similar to Teachers/Students pages).

## Performance Optimization

### Thread Pool Configuration

The thread pool size is automatically calculated based on CPU cores:
- **Optimal Formula**: `CPU cores` (for CPU-bound tasks)
- **For I/O-bound tasks**: Consider `CPU cores × 2`

### Batch Size Configuration

Batch size balances memory usage and parallelism:
- **Default**: `CPU cores × 100`
- **For large datasets**: Increase batch size
- **For memory-constrained environments**: Decrease batch size

### Multithreading Benefits

1. **Parallel Processing**: Multiple batches processed simultaneously
2. **Better CPU Utilization**: Uses all available cores
3. **Scalability**: Handles large datasets efficiently
4. **Non-blocking**: Uses CompletableFuture for async operations

## Error Handling

The pattern provides comprehensive error handling:

1. **Validation Errors**: Row-level validation with detailed error messages
2. **Import Results**: Returns success/failure counts and error details
3. **Partial Success**: Handles cases where some rows succeed and others fail
4. **Error Reporting**: Detailed error information including row numbers and column names

## Testing Considerations

1. **Unit Tests**: Test Excel DTO conversions and validations
2. **Integration Tests**: Test full import/export flow
3. **Performance Tests**: Test with large datasets (1000+ rows)
4. **Error Scenarios**: Test invalid data, missing files, etc.

## Dependencies

Required Maven dependency (already added to `pom.xml`):
```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
```

## Example: Teacher Feature

See the complete implementation in:
- `TeacherExcelDTO.java`
- `TeacherExcelService.java`
- `TeacherExcelController.java`

This serves as a reference implementation for other features.
