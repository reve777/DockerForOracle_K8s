package com.portfolio.entity.data;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class Classify {
    // 匯入時通常不帶 ID，讓資料庫自增
    private Integer id;

    @ExcelProperty("分類名稱")
    private String name;

    @ExcelProperty("支援交易")
    private Boolean tx;
}