package com.portfolio.entity.data;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class UserData {
    // 根據 Excel 欄位名稱映射
    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("年齡")
    private Integer age;

    @ExcelProperty("電子郵件")
    private String email;
}