package com.portfolio.uitls;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import com.portfolio.constants.MessageConstants;
import com.portfolio.constants.SystemConstants;
import com.portfolio.eunms.StatusEnum.InstStatusEnum;


public final class Utils {

    private Utils() {
        throw new AssertionError(MessageConstants.TOOLS_ERROR_STRING);
    }

    /**
     * 處理Controller 型別轉換/資料驗證 錯誤
     * @param bindingResult
     * @return
     */
    public static String handleColumnFormatError(BindingResult bindingResult) {
	    List<String> errorMessages = new ArrayList<>();
	    for (ObjectError oe : bindingResult.getAllErrors()) {
	        if (oe instanceof FieldError fe) {
	            if (MessageConstants.TYPE_MISMATCH.equals(fe.getCode())) { // 型別轉換錯誤
	                errorMessages.add(fe.getField()+MessageConstants.COLUMN_FORMAT_ERROR);
	            } else { // 資料驗證錯誤
	                errorMessages.add(fe.getDefaultMessage());
	            }
	        } else {
	            errorMessages.add(oe.getDefaultMessage());
	        }
	    }
	    return String.join(SystemConstants.STRING_NEW_LINE, errorMessages);
    }

    /**
     * 執行隨機延遲，用於重試機制中減少衝突
     * @param minDelayMs 最小延遲時間(毫秒)
     * @param maxDelayMs 最大延遲時間(毫秒)
     * @param retryNum 當前重試次數
     */
    public static void randomDelay(int minDelayMs, int maxDelayMs, int retryNum) {
        try {
            // 指數退避算法：隨著重試次數增加，延遲時間指數增長
            int factor = Math.min(10, retryNum); // 限制因子大小
            int delayRange = maxDelayMs - minDelayMs;
            int delayMs = minDelayMs + (int)(Math.random() * delayRange * factor / 2);

            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /*
     * 日期解析 string -> LocalDate (不含時分秒的date)
     * */
    public static LocalDate parseDate(String hireDate) {
        if (!StringUtils.hasText(hireDate)) {
            return null;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return LocalDate.parse(hireDate, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("日期格式錯誤: " + hireDate, e);
        }
    }

    /**
     * 處理查詢參數並進行脫敏
     */
    public static String processQueryParams(String[] params) {
        if (params == null || params.length == 0) {
            return "";
        }

        return Stream.of(params)
            .filter(StringUtils::hasText)
            .map(Utils::processSingleParam)
            .collect(Collectors.joining(","));
    }

    // 處理單個參數，檢查是否需要脫敏
    public static String processSingleParam(String param) {
        if (!StringUtils.hasText(param)) {
            return param;
        }

        // 嘗試分割參數，格式應為 "key=value"
        String[] parts = param.split("=", 2);
        if (parts.length != 2) {
            return param; // 不符合 key=value 格式，返回原始參數
        }

        String paramName = parts[0].trim();
        String paramValue = parts[1];

        // 檢查參數名是否在敏感列表中
        if (SystemConstants.SENSITIVE_PARAM_NAMES.contains(paramName.toLowerCase())) {
            return paramName + "=" + maskSensitiveData(paramValue);
        }

        return param;
    }

    // 對敏感參數進行脫敏處理
    public static String maskSensitiveData(String param) {
        if (!StringUtils.hasText(param)) {
            return param;
        }

        int length = param.length();
        if (length <= 4) {
            return "****";
        } else {
            // 保留前兩位和後兩位，中間用星號替代
            return param.substring(0, 2) +
                   "*".repeat(length - 4) +
                   param.substring(length - 2);
        }
    }

    // 截斷過長的查詢參數，使用內部定義的常數
    public static String truncateQueryParams(String params) {
        if (params == null) {
            return null;
        }

        if (params.length() <= SystemConstants.MAX_QUERY_PARAM_LENGTH) {
            return params;
        }

        // 截斷並添加指示符
        return params.substring(0, SystemConstants.MAX_QUERY_PARAM_LENGTH - 3) + "...";
    }

    /**
     * 生成唯一識別符
     * @return
     */
    public static String generateUniqueIdentifier(String prefix) {
        return prefix + UUID.randomUUID().toString();
    }

    /**
     * 將 LocalDateTime 格式化為易讀字串
     * @param dateTime 要格式化的 LocalDateTime
     * @param pattern 格式化模式，為空時使用預設模式 (yyyy-MM-dd HH:mm:ss)
     * @return 格式化後的日期時間字串，若輸入為 null 則返回 null
     */
    public static String formatDateTime(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
            StringUtils.hasText(pattern) ? pattern : "yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }

    /**
     * 將 LocalDateTime 格式化為易讀字串，使用預設格式 yyyy-MM-dd HH:mm:ss
     * @param dateTime 要格式化的 LocalDateTime
     * @return 格式化後的日期時間字串，若輸入為 null 則返回 null
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return formatDateTime(dateTime, null);
    }
    /**
     * 物件屬性複製或轉換核心方法
     *
     * @param <S> 來源物件的類型
     * @param <T> 目標物件的類型
     * @param source 來源物件
     * @param targetClass 目標物件的類別
     * @param target 目標物件 (可為 null，此時會創建 targetClass 的新實例)
     * @return 屬性複製後的目標物件，如果複製失敗則返回 null
     */
    private static <S, T> T copyBeanProperties(S source, Class<T> targetClass, T target) {
        if (source == null) return null;

        try {
            // 如果目標對象為 null，創建新實例
            if (target == null) {
                target = targetClass.getDeclaredConstructor().newInstance();
            }

            // 複製屬性
            BeanUtils.copyProperties(source, target);

            return target;
        } catch (Exception e) {
            LogUtils.warn("copyBeanProperties() 複製物件屬性失敗", e);
            return null;
        }
    }

    /**
     * 建立並返回指定物件的複製品 (淺複製)
     *
     * @param original 要複製的原始物件
     * @param clazz 物件的類別
     * @return 原始物件的複製品，如果複製失敗則返回null
     */
    public static <T> T cloneBean(T original, Class<T> clazz) {
        return copyBeanProperties(original, clazz, null);
    }

    /**
     * 將一個類型的物件屬性複製到另一個類型的物件 (淺複製)
     * 此方法支援不同類型間的屬性複製，但要求屬性名稱相同
     *
     * @param <S> 來源物件的類型
     * @param <T> 目標物件的類型
     * @param original 來源物件
     * @param targetClass 目標物件的類別
     * @return 複製屬性後的目標物件，如果複製失敗則返回null
     */
    public static <S, T> T convertBean(S original, Class<T> targetClass) {
        return copyBeanProperties(original, targetClass, null);
    }

    /**
     * 將來源物件的屬性複製到目標物件 (淺複製)
     * 此方法支援不同類型間的屬性複製，但要求屬性名稱相同
     *
     * @param <S> 來源物件的類型
     * @param <T> 目標物件的類型
     * @param source 來源物件
     * @param target 目標物件
     * @return 複製成功返回 true，失敗返回 false
     */
    public static <S, T> boolean copyProperties(S source, T target) {
        if (source == null || target == null) return false;

        try {
            // 直接使用 BeanUtils 進行複製，避免類型轉換問題
            BeanUtils.copyProperties(source, target);
            return true;
        } catch (Exception e) {
            LogUtils.warn("copyProperties() 複製物件屬性失敗", e);
            return false;
        }
    }

    /**
     * 解碼HTTP Header值，處理URL編碼的中文字符
     * @param value Header原始值
     * @return 解碼後的字串，null時返回空字串
     * @throws UnsupportedEncodingException 當編碼格式不支援時
     */
    public static String decodeHeader(String value) throws UnsupportedEncodingException {
        return value == null ? "" :
        	URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    /**
     * 根據起訖日期判斷狀態
     * 使用物件: 機構、收單機構
     */
    public static String determineStatusByDate(Object entity) {
        try {
            String status = (String) entity.getClass().getMethod("getStatus").invoke(entity);
            LocalDate startDate = (LocalDate) entity.getClass().getMethod("getStartDate").invoke(entity);
            LocalDate endDate = (LocalDate) entity.getClass().getMethod("getEndDate").invoke(entity);
            return determineStatusByDate(status, startDate, endDate);
        } catch (Exception e) {
            LogUtils.warn("determineStatusByDate() 反射調用失敗", e);
            return InstStatusEnum.N.getDescription();
        }
    }

    /**
     * 根據起訖日期判斷狀態
     * @param currentStatus 當前狀態
     * @param startDate 起始日期
     * @param endDate 結束日期
     * @return 基於日期判斷的狀態描述
     */
    private static String determineStatusByDate(String currentStatus, LocalDate startDate, LocalDate endDate) {
        // 待覆核狀態直接返回
        if (InstStatusEnum.W.getCode().equals(currentStatus)) {
            return InstStatusEnum.W.getDescription();
        }

        LocalDate currentDate = LocalDate.now();

        // 無起訖日期時使用待啟用
        if (startDate == null || endDate == null) {
            return InstStatusEnum.N.getDescription();
        }

        // 當前日期在起訖日期區間內
        if ((currentDate.isEqual(startDate) || currentDate.isAfter(startDate)) &&
            (currentDate.isEqual(endDate) || currentDate.isBefore(endDate))) {
            return InstStatusEnum.Y.getDescription(); // 啟用
        } else {
            return InstStatusEnum.N.getDescription(); // 待啟用
        }
    }

}
