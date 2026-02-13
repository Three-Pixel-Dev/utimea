package org.uit.utimea.features.student.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFName;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.uit.utimea.features.student.dto.excel.StudentExcelDTO;
import org.uit.utimea.features.student.dto.request.StudentFilter;
import org.uit.utimea.features.student.dto.request.StudentRequest;
import org.uit.utimea.features.student.dto.response.StudentResponse;
import org.uit.utimea.features.student.mapper.StudentMapper;
import org.uit.utimea.shared.config.ExcelConfig;
import org.uit.utimea.shared.dto.request.PageAndFilterDTO;
import org.uit.utimea.shared.entity.Code;
import org.uit.utimea.shared.entity.CodeValue;
import org.uit.utimea.shared.entity.MajorSection;
import org.uit.utimea.shared.excel.AbstractExcelService;
import org.uit.utimea.shared.excel.ExcelValidationError;
import org.uit.utimea.shared.repository.CodeRepository;
import org.uit.utimea.shared.repository.CodeValueRepository;
import org.uit.utimea.shared.repository.MajorSectionRepository;
import org.uit.utimea.shared.repository.UserRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * Excel service implementation for Students.
 * Handles template generation, export, and import with multithreading support.
 */
@Slf4j
@Service
public class StudentExcelService extends AbstractExcelService<StudentRequest, StudentResponse, StudentExcelDTO> {

    private final StudentService studentService;
    private final CodeValueRepository codeValueRepository;
    private final CodeRepository codeRepository;
    private final MajorSectionRepository majorSectionRepository;
    private final StudentMapper studentMapper;
    private final UserRepository userRepository;

    public StudentExcelService(
            StudentService studentService,
            CodeValueRepository codeValueRepository,
            CodeRepository codeRepository,
            MajorSectionRepository majorSectionRepository,
            StudentMapper studentMapper,
            UserRepository userRepository,
            @org.springframework.beans.factory.annotation.Qualifier("excelThreadPool") ExecutorService excelThreadPool,
            ExcelConfig excelConfig) {
        super(excelThreadPool, excelConfig.excelBatchSize());
        this.studentService = studentService;
        this.codeValueRepository = codeValueRepository;
        this.codeRepository = codeRepository;
        this.majorSectionRepository = majorSectionRepository;
        this.studentMapper = studentMapper;
        this.userRepository = userRepository;
    }

    @Override
    protected List<String> getColumnHeaders() {
        return List.of("Name", "Phone Number", "Email", "Batch Name", "Major Section Name");
    }

    @Override
    protected List<Integer> getColumnWidths() {
        return List.of(30, 20, 30, 20, 30);
    }

    @Override
    protected String getSheetName() {
        return "Students";
    }

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

            addDropdownValidations((XSSFSheet) sheet);

            sheet.createFreezePane(0, 1);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
            
        } catch (IOException e) {
            log.error("Error generating Excel template", e);
            throw new RuntimeException("Failed to generate Excel template", e);
        }
    }

    private void addDropdownValidations(XSSFSheet sheet) {
        addBatchDropdownValidation(sheet, 1, 10000);
        addMajorSectionDropdownValidation(sheet, 1, 10000);
    }

    private void addBatchDropdownValidation(XSSFSheet sheet, int firstRow, int lastRow) {
        try {
            Code batchCode = codeRepository.findByConstantValue("BATCH")
                    .orElseThrow(() -> new RuntimeException("BATCH code not found"));
            
            List<CodeValue> batches = codeValueRepository.findByCode(batchCode);
            List<String> batchNames = batches.stream()
                    .sorted((b1, b2) -> Long.compare(b1.getId(), b2.getId()))
                    .map(CodeValue::getName)
                    .collect(Collectors.toList());
            
            if (batchNames.isEmpty()) {
                log.warn("No batch code values found for dropdown validation");
                return;
            }
            
            XSSFDataValidationHelper validationHelper = new XSSFDataValidationHelper(sheet);
            DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(
                    batchNames.toArray(new String[0])
            );
            
            CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, 3, 3);
            DataValidation validation = validationHelper.createValidation(constraint, addressList);
            
            validation.setShowErrorBox(true);
            validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            validation.createErrorBox("Invalid Batch", "Please select a valid batch from the dropdown list.");
            validation.setShowPromptBox(true);
            validation.createPromptBox("Batch", "Please select a batch from the dropdown list.");
            
            sheet.addValidationData(validation);
            log.info("Added batch dropdown validation with {} options for rows {}-{}", 
                    batchNames.size(), firstRow + 1, lastRow + 1);
        } catch (Exception e) {
            log.error("Error adding batch dropdown validation", e);
        }
    }

    private void addMajorSectionDropdownValidation(XSSFSheet sheet, int firstRow, int lastRow) {
        try {
            List<MajorSection> majorSections = majorSectionRepository.findAll();
            List<String> majorSectionNames = majorSections.stream()
                    .sorted((ms1, ms2) -> Long.compare(ms1.getId(), ms2.getId()))
                    .map(MajorSection::getName)
                    .toList();
            
            if (majorSectionNames.isEmpty()) {
                log.warn("No major sections found for dropdown validation");
                return;
            }
            
            // Calculate total character length including separators (comma + space = 2 chars per item)
            // Excel has a 255 character limit for explicit list constraints
            int totalLength = majorSectionNames.stream()
                    .mapToInt(name -> name.length() + 2) // +2 for ", " separator
                    .sum() - 2; // -2 because last item doesn't have separator
            
            // If the list is too long, use a formula-based approach with a hidden sheet
            if (totalLength > 250) { // Use 250 as safety margin
                log.warn("Major section list too long ({} chars), using formula-based validation", totalLength);
                addMajorSectionDropdownValidationWithFormula(sheet, firstRow, lastRow, majorSectionNames);
            } else {
                XSSFDataValidationHelper validationHelper = new XSSFDataValidationHelper(sheet);
                DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(
                        majorSectionNames.toArray(new String[0])
                );
                
                CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, 4, 4);
                DataValidation validation = validationHelper.createValidation(constraint, addressList);
                
                validation.setShowErrorBox(true);
                validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
                validation.createErrorBox("Invalid Major Section", "Please select a valid major section from the dropdown list.");
                validation.setShowPromptBox(true);
                validation.createPromptBox("Major Section", "Please select a major section from the dropdown list.");
                
                sheet.addValidationData(validation);
                log.info("Added major section dropdown validation with {} options for rows {}-{}", 
                        majorSectionNames.size(), firstRow + 1, lastRow + 1);
            }
        } catch (Exception e) {
            log.error("Error adding major section dropdown validation", e);
        }
    }

    private void addMajorSectionDropdownValidationWithFormula(XSSFSheet sheet, int firstRow, int lastRow, List<String> majorSectionNames) {
        try {
            Workbook workbook = sheet.getWorkbook();
            
            // Create or get a hidden sheet for the major section list
            Sheet hiddenSheet = workbook.getSheet("_MajorSections");
            if (hiddenSheet == null) {
                hiddenSheet = workbook.createSheet("_MajorSections");
                workbook.setSheetHidden(workbook.getSheetIndex(hiddenSheet), true);
            }
            
            // Clear existing data
            for (int i = 0; i <= hiddenSheet.getLastRowNum(); i++) {
                Row row = hiddenSheet.getRow(i);
                if (row != null) {
                    hiddenSheet.removeRow(row);
                }
            }
            
            // Write major section names to hidden sheet
            for (int i = 0; i < majorSectionNames.size(); i++) {
                Row row = hiddenSheet.createRow(i);
                Cell cell = row.createCell(0);
                cell.setCellValue(majorSectionNames.get(i));
            }
            
            // Create named range for the major sections
            String namedRangeName = "MajorSectionsList";
            
            // Create formula reference to the hidden sheet
            String formula = String.format("'_MajorSections'!$A$1:$A$%d", majorSectionNames.size());
            
            // Try to find and update existing named range, or create new one
            org.apache.poi.ss.usermodel.Name namedRange = null;
            if (workbook instanceof XSSFWorkbook) {
                XSSFWorkbook xssfWorkbook = (XSSFWorkbook) workbook;
                try {
                    // Try to find existing named range using getAllNames()
                    Iterator<XSSFName> nameIterator = xssfWorkbook.getAllNames().iterator();
                    while (nameIterator.hasNext()) {
                        org.apache.poi.ss.usermodel.Name existingName = nameIterator.next();
                        if (existingName != null && namedRangeName.equals(existingName.getNameName())) {
                            namedRange = existingName;
                            break;
                        }
                    }
                } catch (Exception e) {
                    log.debug("Could not find existing named range: {}", e.getMessage());
                }
            }
            
            // Update existing or create new named range
            if (namedRange != null) {
                namedRange.setRefersToFormula(formula);
            } else {
                namedRange = workbook.createName();
                namedRange.setNameName(namedRangeName);
                namedRange.setRefersToFormula(formula);
            }
            
            // Create validation using the named range directly
            XSSFDataValidationHelper validationHelper = new XSSFDataValidationHelper(sheet);
            DataValidationConstraint constraint = validationHelper.createFormulaListConstraint(
                    namedRangeName
            );
            
            CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, 4, 4);
            DataValidation validation = validationHelper.createValidation(constraint, addressList);
            
            validation.setShowErrorBox(true);
            validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            validation.createErrorBox("Invalid Major Section", "Please select a valid major section from the dropdown list.");
            validation.setShowPromptBox(true);
            validation.createPromptBox("Major Section", "Please select a major section from the dropdown list.");
            
            sheet.addValidationData(validation);
            log.info("Added major section dropdown validation with formula ({} options) for rows {}-{}", 
                    majorSectionNames.size(), firstRow + 1, lastRow + 1);
        } catch (Exception e) {
            log.error("Error adding major section dropdown validation with formula", e);
            // Fallback: skip validation but log warning
            log.warn("Skipping major section dropdown validation due to error");
        }
    }

    @Override
    protected List<StudentExcelDTO> readExcelData(Workbook workbook) throws IOException {
        List<StudentExcelDTO> students = new ArrayList<>();
        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            StudentExcelDTO student = new StudentExcelDTO();

            Cell nameCell = row.getCell(0);
            if (nameCell != null) {
                student.setName(getCellValueAsString(nameCell));
            }

            Cell phoneCell = row.getCell(1);
            if (phoneCell != null) {
                student.setPhoneNumber(getCellValueAsString(phoneCell));
            }

            Cell emailCell = row.getCell(2);
            if (emailCell != null) {
                student.setEmail(getCellValueAsString(emailCell));
            }

            Cell batchCell = row.getCell(3);
            if (batchCell != null) {
                student.setBatchName(getCellValueAsString(batchCell));
            }

            Cell majorSectionCell = row.getCell(4);
            if (majorSectionCell != null) {
                student.setMajorSectionName(getCellValueAsString(majorSectionCell));
            }

            if (student.getName() != null && !student.getName().trim().isEmpty()) {
                students.add(student);
            }
        }
        
        return students;
    }

    @Override
    protected void writeExcelData(Workbook workbook, List<StudentExcelDTO> data) {
        Sheet sheet = workbook.getSheet(getSheetName());
        CellStyle dataStyle = createDataStyle(workbook);
        
        int rowNum = 1;
        for (StudentExcelDTO student : data) {
            Row row = sheet.createRow(rowNum++);
            
            createCell(row, 0, student.getName(), dataStyle);
            createCell(row, 1, student.getPhoneNumber(), dataStyle);
            createCell(row, 2, student.getEmail(), dataStyle);
            createCell(row, 3, student.getBatchName(), dataStyle);
            createCell(row, 4, student.getMajorSectionName(), dataStyle);
        }
        
        if (sheet instanceof XSSFSheet) {
            int lastDataRow = rowNum > 1 ? rowNum - 1 : 1;
            addBatchDropdownValidation((XSSFSheet) sheet, 1, Math.max(lastDataRow, 1000));
            // For export, we need to ensure the hidden sheet exists if using formula-based validation
            addMajorSectionDropdownValidation((XSSFSheet) sheet, 1, Math.max(lastDataRow, 1000));
        }
    }

    @Override
    protected List<StudentResponse> fetchAllData() {
        PageAndFilterDTO<StudentFilter> pageAndFilter = new PageAndFilterDTO<>();
        pageAndFilter.setPage(0);
        pageAndFilter.setSize(10000);
        var pagination = studentService.getAll(pageAndFilter);
        return pagination.content();
    }

    @Override
    protected void createEntity(StudentRequest request) {
        studentService.create(request);
    }

    @Override
    public List<ExcelValidationError> validateExcelData(List<StudentExcelDTO> excelData) {
        List<ExcelValidationError> errors = new ArrayList<>();
        
        // Track emails within the Excel file to detect duplicates
        java.util.Set<String> emailsInFile = new java.util.HashSet<>();
        
        for (int i = 0; i < excelData.size(); i++) {
            StudentExcelDTO student = excelData.get(i);
            int rowNumber = i + 2;

            if (student.getName() == null || student.getName().trim().isEmpty()) {
                errors.add(ExcelValidationError.builder()
                        .rowNumber(rowNumber)
                        .column("Name")
                        .message("Name is required")
                        .build());
            }

            if (student.getEmail() == null || student.getEmail().trim().isEmpty()) {
                errors.add(ExcelValidationError.builder()
                        .rowNumber(rowNumber)
                        .column("Email")
                        .message("Email is required")
                        .build());
            } else {
                String email = student.getEmail().trim();
                
                // Validate email format
                if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    errors.add(ExcelValidationError.builder()
                            .rowNumber(rowNumber)
                            .column("Email")
                            .message("Invalid email format")
                            .invalidValue(email)
                            .build());
                } else {
                    // Check for duplicate email within the same Excel file
                    if (emailsInFile.contains(email.toLowerCase())) {
                        errors.add(ExcelValidationError.builder()
                                .rowNumber(rowNumber)
                                .column("Email")
                                .message("Email '" + email + "' is duplicated in this file")
                                .invalidValue(email)
                                .build());
                    } else {
                        emailsInFile.add(email.toLowerCase());
                        
                        // Check if email already exists in the database
                        if (userRepository.findByEmail(email).isPresent()) {
                            errors.add(ExcelValidationError.builder()
                                    .rowNumber(rowNumber)
                                    .column("Email")
                                    .message("Email '" + email + "' already exists in the system")
                                    .invalidValue(email)
                                    .build());
                        }
                    }
                }
            }

            if (student.getBatchName() != null && !student.getBatchName().trim().isEmpty()) {
                Code batchCode = codeRepository.findByConstantValue("BATCH")
                        .orElse(null);
                if (batchCode == null) {
                    errors.add(ExcelValidationError.builder()
                            .rowNumber(rowNumber)
                            .column("Batch Name")
                            .message("BATCH code not found in system")
                            .invalidValue(student.getBatchName())
                            .build());
                } else {
                    CodeValue batch = codeValueRepository.findByCodeAndName(batchCode, student.getBatchName())
                            .orElse(null);
                    if (batch == null) {
                        errors.add(ExcelValidationError.builder()
                                .rowNumber(rowNumber)
                                .column("Batch Name")
                                .message("Batch '" + student.getBatchName() + "' not found. Please select from the dropdown list.")
                                .invalidValue(student.getBatchName())
                                .build());
                    }
                }
            }

            if (student.getMajorSectionName() != null && !student.getMajorSectionName().trim().isEmpty()) {
                MajorSection majorSection = majorSectionRepository.findByName(student.getMajorSectionName())
                        .orElse(null);
                if (majorSection == null) {
                    errors.add(ExcelValidationError.builder()
                            .rowNumber(rowNumber)
                            .column("Major Section Name")
                            .message("Major Section '" + student.getMajorSectionName() + "' not found. Please select from the dropdown list.")
                            .invalidValue(student.getMajorSectionName())
                            .build());
                }
            }
        }
        
        return errors;
    }

    @Override
    public StudentRequest convertExcelDtoToRequest(StudentExcelDTO excelDto) {
        Long batchId = null;
        
        if (excelDto.getBatchName() != null && !excelDto.getBatchName().trim().isEmpty()) {
            Code batchCode = codeRepository.findByConstantValue("BATCH")
                    .orElse(null);
            if (batchCode != null) {
                batchId = codeValueRepository.findByCodeAndName(batchCode, excelDto.getBatchName())
                        .map(CodeValue::getId)
                        .orElse(null);
            }
        }

        Long majorSectionId = null;
        if (excelDto.getMajorSectionName() != null && !excelDto.getMajorSectionName().trim().isEmpty()) {
            majorSectionId = majorSectionRepository.findByName(excelDto.getMajorSectionName())
                    .map(MajorSection::getId)
                    .orElse(null);
        }
        
        return new StudentRequest(
                excelDto.getName(),
                excelDto.getPhoneNumber(),
                excelDto.getEmail(),
                batchId,
                majorSectionId
        );
    }

    @Override
    public StudentExcelDTO convertResponseToExcelDto(StudentResponse response) {
        return StudentExcelDTO.builder()
                .name(response.name())
                .phoneNumber(response.phoneNumber())
                .email(response.email())
                .batchName(response.batch() != null ? response.batch().name() : null)
                .majorSectionName(response.majorSection() != null ? response.majorSection().name() : null)
                .build();
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                } else {
                    yield String.valueOf((long) cell.getNumericCellValue());
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private void createCell(Row row, int columnIndex, String value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        if (value != null) {
            cell.setCellValue(value);
        }
        cell.setCellStyle(style);
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
}
