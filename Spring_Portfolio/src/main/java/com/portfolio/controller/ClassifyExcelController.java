package com.portfolio.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.portfolio.entity.data.Classify;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

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
}