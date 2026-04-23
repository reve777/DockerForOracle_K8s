package com.portfolio.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.portfolio.eunms.PermissionEnum;

/**
 * 檢查是否有權限的註解
 * PermissionAnnotation 就像是給每個權限欄位貼上了說明書，讓系統知道：
 * 這個欄位對應 PermissionEnum.QUERY（位元索引 3），
 * 這個欄位對應 PermissionEnum.ADD（位元索引 4），依此類推。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD) // 作用域:欄位
public @interface PermissionAnnotation {
	PermissionEnum permission();
}
