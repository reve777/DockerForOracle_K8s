package com.portfolio.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.portfolio.entity.data.Classify;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/excel/classify")
public class ClassifyExcelController {

	// 模擬資料庫
	private static List<Classify> db = new ArrayList<>();
	private static int currentId = 1;

	// 1. 取得清單
	@GetMapping("/")
	public List<Classify> list() {
		return db;
	}

	// 2. 取得單筆
	@GetMapping("/{id}")
	public Classify get(@PathVariable Integer id) {
		return db.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
	}

	// 3. 新增
	@PostMapping("/")
	public void add(@RequestBody Classify classify) {
		classify.setId(currentId++);
		db.add(classify);
	}

	// 4. Excel 匯入 (重點)
	@PostMapping("/import")
	public ResponseEntity<?> importExcel(@RequestParam("file") MultipartFile file) {
		try {
			List<Classify> tempItems = new ArrayList<>();
			EasyExcel.read(file.getInputStream(), Classify.class, new PageReadListener<Classify>(dataList -> {
				tempItems.addAll(dataList);
			})).sheet().doRead();

			// 補上 ID 並存入
			for (Classify c : tempItems) {
				c.setId(currentId++);
				db.add(c);
			}
			return ResponseEntity.ok(tempItems.size());
		} catch (Exception e) {
			e.printStackTrace(); // 這行會在後端控制台印出具體錯誤原因
			return ResponseEntity.status(500).body("錯誤原因：" + e.getMessage());
		}
	}

	@GetMapping("/export")
	public void exportExcel(HttpServletResponse response) throws IOException {
		// 設定回應內容類型為 Excel 檔案
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setCharacterEncoding("utf-8");

		// 防止中文檔名亂碼
		String fileName = URLEncoder.encode("商品分類列表", "UTF-8").replaceAll("\\+", "%20");
		response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

		// 使用 EasyExcel 寫出資料 (db 是你原有的 List)
		EasyExcel.write(response.getOutputStream(), Classify.class).sheet("分類清單").doWrite(db);
	}
//	@GetMapping("/template")
//	public void downloadTemplate(HttpServletResponse response) throws IOException {
//	    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//	    response.setHeader("Content-Disposition", "attachment; filename=classify_template.xlsx");
//	    
//	    // 這裡可以使用 POI 產生一個只有 "name", "tx" 兩欄標頭的 Excel
//	    // 或者簡單地將 src/main/resources 下的預設檔串流出去
//	}
	@GetMapping("/template")
	public void downloadTemplate(HttpServletResponse response) throws IOException {
	    // 1. 強制設定 Content-Type，確保瀏覽器識別為 Excel
	    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
	    response.setHeader("Content-Disposition", "attachment; filename=classify_template.xlsx");

	    // 2. 建立 Excel 內容
	    try (Workbook workbook = new XSSFWorkbook(); 
	         ServletOutputStream out = response.getOutputStream()) {
	        
	        Sheet sheet = workbook.createSheet("商品分類範本");
	        
	        // 設定欄寬 (讓內容好看一點)
	        sheet.setColumnWidth(0, 256 * 10);
	        sheet.setColumnWidth(1, 256 * 20);
	        sheet.setColumnWidth(2, 256 * 15);

	        // 3. 建立表頭 (對應你的圖片)
	        Row header = sheet.createRow(0);
	        header.createCell(0).setCellValue("id");
	        header.createCell(1).setCellValue("分類名稱");
	        header.createCell(2).setCellValue("支援交易");

	        // 4. 加入一筆範例資料 (讓使用者知道怎麼填)
	        Row exampleRow = sheet.createRow(1);
	        exampleRow.createCell(0).setCellValue(1);
	        exampleRow.createCell(1).setCellValue("台股普通");
	        exampleRow.createCell(2).setCellValue("TRUE");

	        // 5. 寫入輸出流
	        workbook.write(out);
	        out.flush();
	    } catch (Exception e) {
	        log.error("下載範本失敗", e);
	    }
	}
}