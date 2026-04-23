package com.portfolio.uitls;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import com.portfolio.aspect.IvanSqlImage;
import com.portfolio.constants.MessageConstants;
import com.portfolio.entity.AuditedEntity;


/**
 * 審計數據格式化工具類
 * 用於處理審計日誌中的各種數據格式化，提高可讀性
 */
public class AuditFormatter {

    private AuditFormatter() {
        throw new AssertionError(MessageConstants.TOOLS_ERROR_STRING);
    }

    /**
     * 將 AuditedEntity 實例格式化為欄位名稱-值對的字符串
     * 格式: 欄位名稱='值', 欄位名稱='值', ...
     *
     * @param entity AuditedEntity 實例
     * @return 格式化的字符串
     */
    public static String formatEntity(AuditedEntity entity) {
        if (entity == null) {
            return "";
        }

        try {
            StringBuilder sb = new StringBuilder();

            // 獲取所有帶有 IvanSqlImage 註解的欄位及其值
            Map<Integer, Object[]> sortedFields = getSortedFieldsAndValues(entity);

            boolean first = true;
            for (Map.Entry<Integer, Object[]> entry : sortedFields.entrySet()) {
                if (!first) {
                    sb.append(", ");
                }

                // 獲取欄位名稱和值
                Field field = (Field) entry.getValue()[0];
                Object value = entry.getValue()[1];
                String fieldName = field.getName();

                // 安全地轉換欄位值為字串
                String strValue = convertValueToString(value, fieldName);

                sb.append(fieldName).append("='").append(strValue).append("'");
                first = false;
            }

            LogUtils.debug("Formatted entity: {}", sb);
            return sb.toString();

        } catch (Exception e) {
        	LogUtils.warn("格式化實體時發生錯誤: {}", e.getMessage());
            // 如果發生錯誤，嘗試使用原始的 toSqlImage() 方法
            try {
                return entity.toSqlImage();
            } catch (IllegalAccessException ex) {
            	LogUtils.warn("轉換為 SQL Image 時發生錯誤: {}", ex.getMessage());
                return "";
            }
        }
    }

    /**
     * 將原始 SQL Image 字符串格式化為欄位名稱-值對的形式
     * 使用與 AuditedEntity 相同的欄位順序
     *
     * @param sqlImage 原始 SQL Image 字符串
     * @param templateEntity 模板實體，用於獲取欄位信息
     * @return 格式化的字符串
     */
    public static String formatSqlImage(String sqlImage, AuditedEntity templateEntity) {
        if (StringUtils.isBlank(sqlImage) || templateEntity == null) {
            return sqlImage;
        }

        try {
            // 分割原始字符串
            String[] values = sqlImage.split(",");

            // 獲取排序後的欄位
            Map<Integer, Field> sortedFields = getAnnotatedFieldsWithOrder(templateEntity);

            StringBuilder sb = new StringBuilder();
            int index = 0;
            boolean first = true;

            for (Map.Entry<Integer, Field> entry : sortedFields.entrySet()) {
                if (index >= values.length) {
                    break;
                }

                if (!first) {
                    sb.append(", ");
                }

                String fieldName = entry.getValue().getName();
                String value = values[index++];
                if ("null".equals(value)) {
                    value = "";
                }

                sb.append(fieldName).append("='").append(value).append("'");
                first = false;
            }

            return sb.toString();

        } catch (Exception e) {
        	LogUtils.warn("格式化 SQL Image 時發生錯誤: {}", e.getMessage());
            return sqlImage;
        }
    }

    /**
     * 將查詢字符串轉換為格式化的鍵值對形式
     *
     * @param queryString 原始查詢字符串，例如 "bankId=1234&bankName=Test&status=A"
     * @return 格式化的字符串，例如 "bankId='1234', bankName='Test', status='A'"
     */
    public static String formatQueryParam(String queryString) {
        if (StringUtils.isBlank(queryString)) {
            return "";
        }

        // 解析查詢字符串中的參數
        Map<String, String> params = parseQueryString(queryString);

        // 構建格式化的輸出
        StringBuilder formattedParams = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (formattedParams.length() > 0) {
                formattedParams.append(", ");
            }

            // 格式化為 key='value'
            String value = entry.getValue() != null ? entry.getValue() : "";
            formattedParams.append(entry.getKey()).append("='").append(value).append("'");
        }

        return formattedParams.toString();
    }

    /**
     * 格式化 SQL 參數字符串為更可讀的形式
     *
     * @param parameters SQL 參數字符串
     * @return 格式化後的字符串
     */
    public static String formatSqlParameters(String parameters) {
        if (StringUtils.isBlank(parameters)) {
            return "";
        }

        // 簡單替換處理，可以根據實際參數格式進行調整
        try {
            // 如果參數是鍵值對格式，嘗試解析為鍵值對
            if (parameters.contains("=")) {
                return formatQueryParam(parameters);
            }

            // 如果是逗號分隔的參數列表，添加適當的格式
            String[] params = parameters.split(",");
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < params.length; i++) {
                if (result.length() > 0) {
                    result.append(", ");
                }
                result.append("param").append(i + 1).append("='").append(params[i].trim()).append("'");
            }
            return result.toString();
        } catch (Exception e) {
        	LogUtils.warn("格式化 SQL 參數時發生錯誤: {}", e.getMessage());
            return parameters;
        }
    }

    /**
     * 解析查詢字符串為 Map
     *
     * @param queryString 查詢字符串
     * @return 參數名稱與值的映射
     */
    private static Map<String, String> parseQueryString(String queryString) {
        Map<String, String> params = new HashMap<>();

        if (StringUtils.isBlank(queryString)) {
            return params;
        }

        // 分割查詢字符串
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = pair.substring(0, idx);
                String value = idx < pair.length() - 1 ? pair.substring(idx + 1) : "";

                // 處理 URL 編碼
                try {
                    value = java.net.URLDecoder.decode(value, StandardCharsets.UTF_8);
                } catch (Exception e) {
                    // 忽略解碼錯誤，使用原始值
                	LogUtils.warn("parseQueryString() 忽略解碼錯誤，使用原始值 : {}", e.getMessage());
                }

                params.put(key, value);
            }
        }

        return params;
    }

    /**
     * 安全地將欄位值轉換為字串
     * 處理各種特殊類型，避免序列化錯誤
     *
     * @param value 欄位值
     * @param fieldName 欄位名稱（用於日誌記錄）
     * @return 轉換後的字串
     */
    private static String convertValueToString(Object value, String fieldName) {
        if (value == null) {
            return "";
        }

        try {
            // 處理 Clob 類型
            if (value instanceof Clob) {
                return StringHelper.clobToStringSafe((Clob) value);
            }

            // 處理 Blob 類型（如果有的話，轉為 Base64 或標記）
            if (value instanceof java.sql.Blob) {
                return "[BLOB_DATA]";
            }

            // 處理 Hibernate Proxy 物件
            if (value.getClass().getName().contains("HibernateProxy") ||
                value.getClass().getName().contains("javassist")) {
                LogUtils.warn("欄位 {} 是 Hibernate Proxy，跳過序列化", fieldName);
                return "[PROXY_OBJECT]";
            }

            // 處理集合類型（如果有 @OneToMany 等關聯）
            if (value instanceof java.util.Collection) {
                LogUtils.warn("欄位 {} 是集合類型，跳過序列化", fieldName);
                return "[COLLECTION]";
            }

            // 一般類型直接轉字串
            return value.toString();

        } catch (Exception e) {
            // 任何序列化錯誤都不應該中斷審計流程
            LogUtils.warn("轉換欄位 {} 值時發生錯誤: {}", fieldName, e.getMessage());
            return "[ERROR_SERIALIZING]";
        }
    }

    /**
     * 獲取所有帶有 IvanSqlImage 註解的欄位及其值，按 order 排序
     *
     * @param entity AuditedEntity 實例
     * @return 按順序排列的欄位和值的映射
     * @throws IllegalAccessException 如果無法訪問欄位
     */
    private static Map<Integer, Object[]> getSortedFieldsAndValues(AuditedEntity entity)
            throws IllegalAccessException {
        Map<Integer, Object[]> result = new TreeMap<>();

        // 反射獲取所有欄位，包括父類欄位
        Class<?> clazz = entity.getClass();
        while (clazz != null && !Object.class.equals(clazz)) {
            for (Field field : clazz.getDeclaredFields()) {
                IvanSqlImage annotation = field.getAnnotation(IvanSqlImage.class);
                if (annotation != null) {
                    boolean accessible = field.canAccess(entity);
                    try {
                        field.setAccessible(true);
                        Object value = field.get(entity);
                        result.put(annotation.order(), new Object[] { field, value });
                    } catch (Exception e) {
                        // 欄位讀取失敗不應中斷整個審計流程
                        LogUtils.warn("讀取欄位 {} 時發生錯誤: {}", field.getName(), e.getMessage());
                        result.put(annotation.order(), new Object[] { field, null });
                    } finally {
                        field.setAccessible(accessible);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }

        return result;
    }

    /**
     * 獲取所有帶有 IvanSqlImage 註解的欄位，按 order 排序
     *
     * @param entity AuditedEntity 實例
     * @return 按順序排列的欄位映射
     */
    private static Map<Integer, Field> getAnnotatedFieldsWithOrder(AuditedEntity entity) {
        Map<Integer, Field> result = new TreeMap<>();

        // 反射獲取所有欄位，包括父類欄位
        Class<?> clazz = entity.getClass();
        while (clazz != null && !Object.class.equals(clazz)) {
            for (Field field : clazz.getDeclaredFields()) {
            	IvanSqlImage annotation = field.getAnnotation(IvanSqlImage.class);
                if (annotation != null) {
                    result.put(annotation.order(), field);
                }
            }
            clazz = clazz.getSuperclass();
        }

        return result;
    }
}
