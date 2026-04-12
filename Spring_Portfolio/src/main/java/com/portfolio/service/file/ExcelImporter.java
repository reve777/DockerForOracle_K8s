package com.portfolio.service.file;

import org.apache.poi.ss.usermodel.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ExcelImporter {

	public static void main(String[] args) {
		String filePath = "data.xlsx"; // 你的 Excel 檔案路徑
		importExcel(filePath);
	}

	public static void importExcel(String filePath) {
		try (FileInputStream fis = new FileInputStream(new File(filePath));
				Workbook workbook = WorkbookFactory.create(fis)) {

			// 1. 取得第一個工作表 (索引為 0)
			Sheet sheet = workbook.getSheetAt(0);

			// 2. 迭代每一行 (Row)
			for (Row row : sheet) {
				// 3. 迭代每個單元格 (Cell)
				for (Cell cell : row) {
					printCellValue(cell);
					System.out.print(" | ");
				}
				System.out.println(); // 換行
			}

		} catch (IOException e) {
			System.err.println("讀取檔案失敗: " + e.getMessage());
		}
	}

	// 輔助方法：根據單元格類型讀取值
	private static void printCellValue(Cell cell) {
		switch (cell.getCellType()) {
		case STRING:
			System.out.print(cell.getStringCellValue());
			break;
		case NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				System.out.print(cell.getDateCellValue());
			} else {
				System.out.print(cell.getNumericCellValue());
			}
			break;
		case BOOLEAN:
			System.out.print(cell.getBooleanCellValue());
			break;
		case FORMULA:
			System.out.print(cell.getCellFormula());
			break;
		default:
			System.out.print("");
		}
	}
}