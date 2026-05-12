package com.portfolio.linepay.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LinePayReturnCodeUtil {

    // ==========================================
    // Return Code 常數定義 (final String)
    // ==========================================
    
    /** 請求成功執行時傳遞的代碼。如果是查詢付款請求狀態的結果，則該狀態是顧客完成LINE Pay認證之前的狀態。 */
    public static final String CODE_0000 = "0000";
    
    /** 顧客已完成LINE Pay認證，可以進行付款授權。 */
    public static final String CODE_0110 = "0110";
    
    /** 顧客取消付款或超過LINE Pay認證等待時間。 */
    public static final String CODE_0121 = "0121";
    
    /** 付款失敗。 */
    public static final String CODE_0122 = "0122";
    
    /** 付款完成。 */
    public static final String CODE_0123 = "0123";
    
    /** 該用戶不是LINE Pay用戶。 */
    public static final String CODE_1101 = "1101";
    
    /** 該用戶目前無法使用LINE Pay交易。 */
    public static final String CODE_1102 = "1102";
    
    /** 您的商店尚未在合作商店中心註冊成為合作商店。請確認輸入的credentials是否正確。 */
    public static final String CODE_1104 = "1104";
    
    /** 該合作商店目前無法使用LINE Pay。 */
    public static final String CODE_1105 = "1105";
    
    /** 請求標頭訊息有錯誤。 */
    public static final String CODE_1106 = "1106";
    
    /** 該信用卡無法正常使用。 */
    public static final String CODE_1110 = "1110";
    
    /** 金額訊息有誤。 */
    public static final String CODE_1124 = "1124";
    
    /** 帳戶狀態有問題。如為EPI交易，商家有可能未開通EPI支付方式。如為Preapproved交易，有可能用戶已經刪除該支付方式，需重新取得Regkey。 */
    public static final String CODE_1141 = "1141";
    
    /** 餘額不足。 */
    public static final String CODE_1142 = "1142";
    
    /** 付款進行中。 */
    public static final String CODE_1145 = "1145";
    
    /** 無交易歷史。 */
    public static final String CODE_1150 = "1150";
    
    /** 有相同交易歷史。 */
    public static final String CODE_1152 = "1152";
    
    /** 付款請求金額和請款金額不同。 */
    public static final String CODE_1153 = "1153";
    
    /** 無法使用設定為預先授權付款的付款方式。 */
    public static final String CODE_1154 = "1154";
    
    /** 交易ID有誤。 */
    public static final String CODE_1155 = "1155";
    
    /** 無付款請求訊息。 */
    public static final String CODE_1159 = "1159";
    
    /** 無法退款。(超過可退款期限) */
    public static final String CODE_1163 = "1163";
    
    /** 超出可退款金額。 */
    public static final String CODE_1164 = "1164";
    
    /** 已退款的交易。 */
    public static final String CODE_1165 = "1165";
    
    /** 須在LINE Pay中選擇付款方式並驗證認證密碼。 */
    public static final String CODE_1169 = "1169";
    
    /** 會員帳戶餘額發生變化。 */
    public static final String CODE_1170 = "1170";
    
    /** 已存在相同訂單號碼的交易記錄。 */
    public static final String CODE_1172 = "1172";
    
    /** 超出可查看的最多交易數量(100筆)。 */
    public static final String CODE_1177 = "1177";
    
    /** 合作商店不支援該貨幣。 */
    public static final String CODE_1178 = "1178";
    
    /** 無法處理該狀態。 */
    public static final String CODE_1179 = "1179";
    
    /** 已超過付款期限。 */
    public static final String CODE_1180 = "1180";
    
    /** 付款金額必須大於設定的最低金額。 */
    public static final String CODE_1183 = "1183";
    
    /** 付款金額必須小於設定的最高金額。 */
    public static final String CODE_1184 = "1184";
    
    /** 無預先授權付款密鑰。 */
    public static final String CODE_1190 = "1190";
    
    /** 預先授權付款密鑰已逾期。 */
    public static final String CODE_1193 = "1193";
    
    /** 合作商店不支援預先授權付款。 */
    public static final String CODE_1194 = "1194";
    
    /** API呼叫請求重複。 */
    public static final String CODE_1198 = "1198";
    
    /** 內部請求發生錯誤。 */
    public static final String CODE_1199 = "1199";
    
    /** 信用卡付款時發生臨時錯誤。 */
    public static final String CODE_1280 = "1280";
    
    /** 信用卡付款時發生錯誤。 */
    public static final String CODE_1281 = "1281";
    
    /** 信用卡授權時發生錯誤。 */
    public static final String CODE_1282 = "1282";
    
    /** 有不當使用疑慮，付款被拒絕。 */
    public static final String CODE_1283 = "1283";
    
    /** 信用卡付款暫時暫停。 */
    public static final String CODE_1284 = "1284";
    
    /** 信用卡付款訊息缺失。 */
    public static final String CODE_1285 = "1285";
    
    /** 信用卡付款訊息中有錯誤訊息。 */
    public static final String CODE_1286 = "1286";
    
    /** 信用卡已過期。 */
    public static final String CODE_1287 = "1287";
    
    /** 信用卡帳戶餘額不足。 */
    public static final String CODE_1288 = "1288";
    
    /** 超出信用卡額度。 */
    public static final String CODE_1289 = "1289";
    
    /** 超出信用卡單筆付款額度。 */
    public static final String CODE_1290 = "1290";
    
    /** 該卡已被通報失竊。 */
    public static final String CODE_1291 = "1291";
    
    /** 該卡已停用。 */
    public static final String CODE_1292 = "1292";
    
    /** CVN輸入錯誤。 */
    public static final String CODE_1293 = "1293";
    
    /** 該卡已被列入黑名單。 */
    public static final String CODE_1294 = "1294";
    
    /** 信用卡號碼錯誤。 */
    public static final String CODE_1295 = "1295";
    
    /** 無法處理此金額。 */
    public static final String CODE_1296 = "1296";
    
    /** 該卡被拒絕。 */
    public static final String CODE_1298 = "1298";
    
    /** 發生臨時錯誤。請稍後再試一次。 */
    public static final String CODE_190X = "190X";
    
    /** 參數錯誤。 */
    public static final String CODE_2101 = "2101";
    
    /** JSON數據格式錯誤。 */
    public static final String CODE_2102 = "2102";
    
    /** 發生了內部錯誤。 */
    public static final String CODE_9000 = "9000";

    // ==========================================
    // 訊息 Map 與查詢方法
    // ==========================================

    private static final Map<String, String> MESSAGE_MAP;

    static {
        Map<String, String> map = new HashMap<>();
        map.put(CODE_0000, "請求成功執行時傳遞的代碼。如果是查詢付款請求狀態的結果，則該狀態是顧客完成LINE Pay認證之前的狀態。");
        map.put(CODE_0110, "顧客已完成LINE Pay認證，可以進行付款授權。");
        map.put(CODE_0121, "顧客取消付款或超過LINE Pay認證等待時間。");
        map.put(CODE_0122, "付款失敗。");
        map.put(CODE_0123, "付款完成。");
        map.put(CODE_1101, "該用戶不是LINE Pay用戶。");
        map.put(CODE_1102, "該用戶目前無法使用LINE Pay交易。");
        map.put(CODE_1104, "您的商店尚未在合作商店中心註冊成為合作商店。請確認輸入的credentials是否正確。");
        map.put(CODE_1105, "該合作商店目前無法使用LINE Pay。");
        map.put(CODE_1106, "請求標頭訊息有錯誤。");
        map.put(CODE_1110, "該信用卡無法正常使用。");
        map.put(CODE_1124, "金額訊息有誤。");
        map.put(CODE_1141, "帳戶狀態有問題。如為EPI交易，商家有可能未開通EPI支付方式。如為Preapproved交易，有可能用戶已經刪除該支付方式，需重新取得Regkey。");
        map.put(CODE_1142, "餘額不足。");
        map.put(CODE_1145, "付款進行中。");
        map.put(CODE_1150, "無交易歷史。");
        map.put(CODE_1152, "有相同交易歷史。");
        map.put(CODE_1153, "付款請求金額和請款金額不同。");
        map.put(CODE_1154, "無法使用設定為預先授權付款的付款方式。");
        map.put(CODE_1155, "交易ID有誤。");
        map.put(CODE_1159, "無付款請求訊息。");
        map.put(CODE_1163, "無法退款。(超過可退款期限)");
        map.put(CODE_1164, "超出可退款金額。");
        map.put(CODE_1165, "已退款的交易。");
        map.put(CODE_1169, "須在LINE Pay中選擇付款方式並驗證認證密碼。");
        map.put(CODE_1170, "會員帳戶餘額發生變化。");
        map.put(CODE_1172, "已存在相同訂單號碼的交易記錄。");
        map.put(CODE_1177, "超出可查看的最多交易數量(100筆)。");
        map.put(CODE_1178, "合作商店不支援該貨幣。");
        map.put(CODE_1179, "無法處理該狀態。");
        map.put(CODE_1180, "已超過付款期限。");
        map.put(CODE_1183, "付款金額必須大於設定的最低金額。");
        map.put(CODE_1184, "付款金額必須小於設定的最高金額。");
        map.put(CODE_1190, "無預先授權付款密鑰。");
        map.put(CODE_1193, "預先授權付款密鑰已逾期。");
        map.put(CODE_1194, "合作商店不支援預先授權付款。");
        map.put(CODE_1198, "API呼叫請求重複。");
        map.put(CODE_1199, "內部請求發生錯誤。");
        map.put(CODE_1280, "信用卡付款時發生臨時錯誤。");
        map.put(CODE_1281, "信用卡付款時發生錯誤。");
        map.put(CODE_1282, "信用卡授權時發生錯誤。");
        map.put(CODE_1283, "有不當使用疑慮，付款被拒絕。");
        map.put(CODE_1284, "信用卡付款暫時暫停。");
        map.put(CODE_1285, "信用卡付款訊息缺失。");
        map.put(CODE_1286, "信用卡付款訊息中有錯誤訊息。");
        map.put(CODE_1287, "信用卡已過期。");
        map.put(CODE_1288, "信用卡帳戶餘額不足。");
        map.put(CODE_1289, "超出信用卡額度。");
        map.put(CODE_1290, "超出信用卡單筆付款額度。");
        map.put(CODE_1291, "該卡已被通報失竊。");
        map.put(CODE_1292, "該卡已停用。");
        map.put(CODE_1293, "CVN輸入錯誤。");
        map.put(CODE_1294, "該卡已被列入黑名單。");
        map.put(CODE_1295, "信用卡號碼錯誤。");
        map.put(CODE_1296, "無法處理此金額。");
        map.put(CODE_1298, "該卡被拒絕。");
        map.put(CODE_190X, "發生臨時錯誤。請稍後再試一次。");
        map.put(CODE_2101, "參數錯誤。");
        map.put(CODE_2102, "JSON數據格式錯誤。");
        map.put(CODE_9000, "發生了內部錯誤。");
        
        // 將 Map 設為不可修改，確保執行緒安全 (Thread-Safe)
        MESSAGE_MAP = Collections.unmodifiableMap(map);
    }

    /**
     * 透過 Return Code 取得對應的錯誤訊息
     * 
     * @param code LINE Pay 回傳的代碼
     * @return 對應的中文說明，若找不到則回傳預設的未知錯誤訊息
     */
    public static String getMessage(String code) {
        // 特別處理 190X 的格式
        if (code != null && code.startsWith("190")) {
            return MESSAGE_MAP.get(CODE_190X);
        }
        return MESSAGE_MAP.getOrDefault(code, "未知的錯誤代碼 (" + code + ")");
    }

    /**
     * 檢查是否為成功代碼
     * 
     * @param code LINE Pay 回傳的代碼
     * @return true 代表請求成功
     */
    public static boolean isSuccess(String code) {
        return CODE_0000.equals(code);
    }
    
    // 私有建構子，防止被實例化
    private LinePayReturnCodeUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
