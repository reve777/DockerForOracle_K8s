package com.portfolio.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.portfolio.eunms.PermissionEnum;
/**
 * 判斷方法是否有權限存取
 * 負責「標記」哪些方法需要權限控制，並告知需要什麼權限。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequiresPermission {
    PermissionEnum value();
}

