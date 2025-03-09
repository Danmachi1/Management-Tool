package com.crfmanagement.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelReader {

    public static List<Object[]> readExcel(String filePath) {
        List<Object[]> data = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            String lastPSL = null;
            String lastCRF = null;

            for (Row row : sheet) {
                List<Object> rowData = new ArrayList<>();
                String currentPSL = null;
                String currentCRF = null;

                for (int i = 0; i < row.getLastCellNum(); i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    switch (cell.getCellType()) {
                        case STRING:
                            rowData.add(cell.getStringCellValue());
                            break;
                        case NUMERIC:
                            rowData.add(cell.getNumericCellValue());
                            break;
                        default:
                            rowData.add("");
                            break;
                    }

                    // Track PSL and CRF for filling down
                    if (i == 1) { // Assuming column index 1 is PSL
                        if (cell.getCellType() == CellType.STRING) {
                            currentPSL = cell.getStringCellValue();
                        } else if (cell.getCellType() == CellType.NUMERIC) {
                            currentPSL = String.valueOf((int) cell.getNumericCellValue());
                        }
                        if (currentPSL != null && !currentPSL.isEmpty()) {
                            lastPSL = currentPSL;
                        } else {
                            rowData.set(i, lastPSL);
                        }
                    } else if (i == 2) { // Assuming column index 2 is CRF
                        if (cell.getCellType() == CellType.STRING) {
                            currentCRF = cell.getStringCellValue();
                        } else if (cell.getCellType() == CellType.NUMERIC) {
                            currentCRF = String.valueOf((int) cell.getNumericCellValue());
                        }
                        if (currentCRF != null && !currentCRF.isEmpty()) {
                            lastCRF = currentCRF;
                        } else {
                            rowData.set(i, lastCRF);
                        }
                    }
                }
                data.add(rowData.toArray());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }
}
