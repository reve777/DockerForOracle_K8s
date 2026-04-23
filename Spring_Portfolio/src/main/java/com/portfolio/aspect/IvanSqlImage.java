package com.portfolio.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 自訂的註解（Annotation），主要用於標記實體類別（如 AuditedEntity）中的欄位，
 以便在後續的處理過程中，透過反射機制讀取這些欄位的資訊。
 */
@Retention(RetentionPolicy.RUNTIME) // 表示此註解在運行時期可被讀取，這對於反射操作是必要的。
@Target(ElementType.FIELD) // 表示此註解只能用於欄位（Field）。
public @interface IvanSqlImage {
    int order() default 0;

    String value() default "";
}