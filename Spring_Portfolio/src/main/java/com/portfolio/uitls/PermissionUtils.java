package com.portfolio.uitls;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.portfolio.bean.PermissionBean;
import com.portfolio.constants.ModuleConstants;

import org.apache.commons.lang3.StringUtils;

/**
 * 權限檢查工具類 - 統一權限邏輯，避免重複程式碼
 */
public final class PermissionUtils {

	private static final int CONFIRM_MOVE_IDX_NUM = 1; // 覆核功能位移的向量
	private static final String BINARY_CONVERT_FORMAT = "%7s"; // LDAP權限轉換的二進制格式
	private static final String DIGIT_ONLY_REGEX = "[^0-9]"; // 只保留數字的正規表達式
	private static final String PERMISSION_FORMAT_REGEX = "[0-9,]+"; // 權限格式驗證
	private static final String DEFAULT_PERMISSION_MASK = "0000000"; // 無權限的預設遮罩

	private PermissionUtils() {
		throw new AssertionError("工具類不應被實例化");
	}

	/**
	 * 檢查指定操作的權限
	 * 
	 * @param permissionValues 權限字串，例如："8,8,15,32,15,32,..."
	 * @param function         功能名稱，例如："bank"
	 * @param action           操作名稱，例如："confirm"
	 * @return 是否有權限
	 */
	public static boolean hasPermission(String permissionValues, String function, String action) {
		try {
			if (StringUtils.isBlank(permissionValues)) {
				return false;
			}

			String[] permissions = permissionValues.split(",");
			int permissionIndex = findFunctionIndex(function, action);

			if (permissionIndex < 0 || permissionIndex >= permissions.length) {
				return false;
			}

			String bitmask = convertDecimalToBinaryReversed(permissions[permissionIndex]);
			int bitIndex = getBitIndex(action);

			return checkStringPermission(bitmask, bitIndex);

		} catch (Exception e) {
			LogUtils.error("權限檢查時發生錯誤", e);
			return false;
		}
	}

	/**
	 * 從權限字串建立 PermissionBean 物件
	 * 
	 * @param permissionValues 權限字串
	 * @param function         功能名稱
	 * @param action           操作名稱
	 * @return PermissionBean 包含各種權限的狀態
	 */
	public static PermissionBean buildPermissionBean(String permissionValues, String function, String action) {
		try {
			if (StringUtils.isBlank(permissionValues)) {
				return buildDefaultPermissionBean();
			}

			String[] permissions = permissionValues.split(",");
			int permissionIndex = findFunctionIndex(function, action);

			if (permissionIndex < 0 || permissionIndex >= permissions.length) {
				return buildDefaultPermissionBean();
			}

			String bitmask = convertDecimalToBinaryReversed(permissions[permissionIndex]);
			return buildPermissionBeanFromBitmask(bitmask);

		} catch (Exception e) {
			LogUtils.error("建立 PermissionBean 時發生錯誤", e);
			return buildDefaultPermissionBean();
		}
	}

	/**
	 * 建立首頁(待辦事項)的權限物件
	 * 
	 * @param permissionValues 權限字串
	 * @return PermissionBean 首頁權限物件
	 */
	public static PermissionBean buildIndexPermissionBean(String permissionValues) {
		try {
			if (StringUtils.isBlank(permissionValues)) {
				return buildDefaultPermissionBean();
			}

			String[] permissions = permissionValues.split(",");
			if (permissions.length > 0) {
				String bitmask = convertDecimalToBinaryReversed(permissions[0]);
				return buildPermissionBeanFromBitmask(bitmask);
			} else {
				return buildDefaultPermissionBean();
			}
		} catch (Exception e) {
			LogUtils.error("建立首頁 PermissionBean 時發生錯誤", e);
			return buildDefaultPermissionBean();
		}
	}

	/**
	 * 在功能清單中查找指定功能的索引位置 覆核功能會自動查找對應的覆核版本索引
	 * 
	 * @param function 功能名稱
	 * @param action   操作名稱
	 * @return 功能索引，未找到返回 -1
	 */
	public static int findFunctionIndex(String function, String action) {
		boolean isConfirmAction = ModuleConstants.classify.equals(action);

		for (int i = 0; i < ModuleConstants.FUNCTION_LIST.size(); i++) {
			String moduleFunction = ModuleConstants.FUNCTION_LIST.get(i);

			if (moduleFunction.equalsIgnoreCase(function)) {
				if (isConfirmAction) {
					// 覆核功能：查找下一個索引（覆核版本）
					if (i + CONFIRM_MOVE_IDX_NUM < ModuleConstants.FUNCTION_LIST.size()
							&& ModuleConstants.FUNCTION_LIST.get(i + CONFIRM_MOVE_IDX_NUM).equalsIgnoreCase(function)) {
						return i + CONFIRM_MOVE_IDX_NUM; // 返回覆核版本的索引
					}
					return i;
				} else {
					// 非覆核功能：返回第一個找到的索引
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * 根據操作名稱取得對應的位元索引位置 映射關係：update=0, delete=1, add=2, query=3, confirm=5,
	 * confirmRecordQuery=6
	 * 
	 * @param action 操作名稱
	 * @return 位元索引，無效操作返回 -1
	 */
	public static int getBitIndex(String action) {
		if (action.startsWith(ModuleConstants.PREFIX_UPDATE)) {
			return 0;
		} else if (action.startsWith(ModuleConstants.PREFIX_DELETE)) {
			return 1;
		} else if (action.startsWith(ModuleConstants.PREFIX_ADD)) {
			return 2;
		} else if (action.startsWith(ModuleConstants.PREFIX_MAIN) || action.startsWith(ModuleConstants.PREFIX_GET)
				|| action.startsWith(ModuleConstants.PREFIX_DOWNLOAD)
				|| action.startsWith(ModuleConstants.PREFIX_UPLOAD)) {
			return 3;
		} else if (action.startsWith(ModuleConstants.PREFIX_CONFIRM)) {
			return 5;
		} else if (action.startsWith(ModuleConstants.PREFIX_CONFIRM_RECORD_QUERY)) {
			return 6;
		}
		return -1;
	}

	/**
	 * 檢查位元遮罩中指定位置是否為 '1'
	 * 
	 * @param bitmask     位元遮罩字串
	 * @param stringIndex 要檢查的位置
	 * @return 該位置是否有權限
	 */
	public static boolean checkStringPermission(String bitmask, int stringIndex) {
		if (stringIndex < 0 || stringIndex >= bitmask.length()) {
			return false;
		}
		return bitmask.charAt(stringIndex) == '1';
	}

	/**
	 * 從位元遮罩字串建立完整的 PermissionBean 物件
	 * 
	 * @param bitmask 7位元的權限遮罩字串
	 * @return PermissionBean 包含所有權限狀態
	 */
	public static PermissionBean buildPermissionBeanFromBitmask(String bitmask) {
		LogUtils.debug("buildPermissionBeanFromBitmask: bitmask={}", bitmask);

		PermissionBean permissionBean = new PermissionBean();

		try {
			// 使用字串索引檢查，對應：0=修改, 1=刪除, 2=新增, 3=查詢, 5=覆核, 6=覆核紀錄查詢
			boolean updatePermission = checkStringPermission(bitmask, 0);
			boolean deletePermission = checkStringPermission(bitmask, 1);
			boolean addPermission = checkStringPermission(bitmask, 2);
			boolean queryPermission = checkStringPermission(bitmask, 3);
			boolean confirmPermission = checkStringPermission(bitmask, 5);
			boolean confirmRecordQueryPermission = checkStringPermission(bitmask, 6);

			LogUtils.debug("權限解析結果: update={}, delete={}, add={}, query={}, confirm={}, confirmRecordQuery={}",
					updatePermission, deletePermission, addPermission, queryPermission, confirmPermission,
					confirmRecordQueryPermission);

			// 設定到 PermissionBean
			permissionBean.setCanUpdate(updatePermission);
			permissionBean.setCanDelete(deletePermission);
			permissionBean.setCanAdd(addPermission);
			permissionBean.setCanQuery(queryPermission);
			permissionBean.setCanConfirm(confirmPermission);
			permissionBean.setCanConfirmRecordQuery(confirmRecordQueryPermission);

		} catch (Exception e) {
			LogUtils.error("建立 PermissionBean 時發生錯誤", e);
			return buildDefaultPermissionBean();
		}

		return permissionBean;
	}

	/**
	 * 建立預設權限物件(所有權限皆為 false)
	 * 
	 * @return 預設的 PermissionBean
	 */
	public static PermissionBean buildDefaultPermissionBean() {
		return new PermissionBean(); // 所有權限預設為 false
	}

	/**
	 * 將十進制字串轉換為反轉的7位元二進制字串 處理亂碼和非數字字元，確保系統穩定性 例如：32 → "0100000" → "0000010"
	 * 
	 * @param text 十進制數字字串
	 * @return 反轉的7位元二進制字串，異常時返回 "0000000"
	 */
	public static String convertDecimalToBinaryReversed(String text) {
		// 輸入驗證：空值處理
		if (StringUtils.isBlank(text)) {
			LogUtils.warn("權限值為空，返回預設值");
			return DEFAULT_PERMISSION_MASK;
		}

		// 清理亂碼：只保留數字字元
		String cleanText = text.replaceAll(DIGIT_ONLY_REGEX, "");

		// 清理後驗證：無有效數字時使用預設值
		if (cleanText.isEmpty()) {
			LogUtils.warn("權限值包含亂碼無法解析: {}", text);
			return DEFAULT_PERMISSION_MASK;
		}

		try {
			// 轉換邏輯：十進制 → 二進制 → 反轉
			int number = Integer.parseInt(cleanText);
			String binaryPart = String.format(BINARY_CONVERT_FORMAT, Integer.toBinaryString(number)).replace(' ', '0');
			return new StringBuilder(binaryPart).reverse().toString();
		} catch (NumberFormatException e) {
			LogUtils.error("數字轉換失敗: " + cleanText, e);
			return DEFAULT_PERMISSION_MASK;
		}
	}

	/**
	 * 從 iv-creds 中解析 AP039 權限
	 */
	public static String extractPermissionFromIvCreds(String ivCreds) {
		if (ivCreds == null)
			return null;

		try {
			LogUtils.debug("原始 iv-creds 長度: {}", ivCreds.length());
			LogUtils.debug("開頭100字元: {}", ivCreds.length() > 100 ? ivCreds.substring(0, 100) : ivCreds);

			// 1. 檢查是否直接包含 AP039
			if (ivCreds.contains("AP039:")) {
				LogUtils.debug("直接找到 AP039");
				return extractFromPlainText(ivCreds);
			}

			// 2. 去掉 Version=1, 後解碼
			if (ivCreds.startsWith("Version=1,")) {
				String base64Part = ivCreds.substring(10).trim(); // 去掉 "Version=1, "
				LogUtils.debug("嘗試解碼部分: {}", base64Part.substring(0, Math.min(50, base64Part.length())));

				try {
					byte[] decoded = Base64.getDecoder().decode(base64Part);
					String decodedContent = new String(decoded, StandardCharsets.UTF_8);
					LogUtils.debug("解碼成功，查找 AP039...");

					if (decodedContent.contains("AP039:")) {
						return extractFromPlainText(decodedContent);
					}
				} catch (Exception e) {
					LogUtils.debug("Base64 解碼失敗: {}", e.getMessage());
				}
			}

			LogUtils.warn("無法找到 AP039 權限");
			return null;

		} catch (Exception e) {
			LogUtils.error("解析失敗: " + e.getMessage(), e);
			return null;
		}
	}

	/**
	 * 從明文中提取 AP039 權限值 驗證提取結果的格式正確性
	 * 
	 * @param text 包含權限的明文字串
	 * @return 權限字串，格式異常時返回 null
	 */
	private static String extractFromPlainText(String text) {
		int start = text.indexOf("AP039:") + 6;
		int end = text.indexOf(".", start);
		if (end == -1)
			end = text.length();

		String extracted = text.substring(start, end);

		// 格式驗證：只允許數字和逗號
		if (!extracted.matches(PERMISSION_FORMAT_REGEX)) {
			LogUtils.warn("權限格式異常: {}", extracted);
			return null;
		}

		return extracted;
	}

}
