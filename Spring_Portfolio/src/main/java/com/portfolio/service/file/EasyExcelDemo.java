package com.portfolio.service.file;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.portfolio.entity.data.UserData;

public class EasyExcelDemo {
	public static void main(String[] args) {
		String fileName = "users.xlsx";

		// 讀取 Excel
		EasyExcel.read(fileName, UserData.class, new PageReadListener<UserData>(dataList -> {
			for (UserData user : dataList) {
				// 在這裡處理每一行讀取到的資料（例如存入資料庫）
				System.out.println("讀取到： " + user.getName() + "，年齡：" + user.getAge());
			}
		})).sheet().doRead();
	}
}
