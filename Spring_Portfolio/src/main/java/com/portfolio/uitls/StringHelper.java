package com.portfolio.uitls;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import javax.sql.rowset.serial.SerialClob;

import org.springframework.util.StringUtils;

import com.portfolio.constants.SystemConstants;

/**
 * 處理 String 相關的方法
 */
public class StringHelper {

	/**
	 * 將 String 轉換為 Clob
	 *
	 * @param content 要轉換的字串
	 * @return 轉換後的 Clob 對象
	 * @throws SQLException 如果轉換過程中發生 SQL 異常
	 */
	public static Clob stringToClob(String content) throws SQLException {
		if (content == null) {
			return null;
		}
		return new SerialClob(content.toCharArray());
	}

	/**
	 * 將 Clob 轉換為 String
	 *
	 * @param clob 要轉換的 Clob 對象
	 * @return 轉換後的字串，若 Clob 為 null 則返回空字串
	 * @throws SQLException 如果讀取 Clob 過程中發生 SQL 異常
	 * @throws IOException  如果 IO 操作過程中發生異常
	 */
	public static String clobToString(Clob clob) throws SQLException, IOException {
		if (clob == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		try (Reader reader = clob.getCharacterStream(); BufferedReader br = new BufferedReader(reader)) {

			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}
		}

		// 移除最後一個換行符（如果有的話）
		if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
			sb.setLength(sb.length() - 1);
		}

		return sb.toString();
	}

	/**
	 * 安全地將 Clob 轉換為 String，不拋出異常
	 *
	 * @param clob 要轉換的 Clob 對象
	 * @return 轉換後的字串，若轉換失敗則返回空字串
	 */
	public static String clobToStringSafe(Clob clob) {
		try {
			return clobToString(clob);
		} catch (Exception e) {
			LogUtils.error("Clob 轉換為 String 失敗", e);
			return "";
		}
	}

	/**
	 * 檢查 Clob 是否為空
	 *
	 * @param clob 要檢查的 Clob 對象
	 * @return 如果 Clob 為 null 或內容為空則返回 true，否則返回 false
	 */
	public static boolean isEmpty(Clob clob) {
		if (clob == null) {
			return true;
		}
		try {
			return clob.length() == 0;
		} catch (SQLException e) {
			LogUtils.error("檢查 Clob 是否為空失敗", e);
			return true;
		}
	}

	/**
	 * 格式化PEM字串
	 */
	public static String formatPem(String base64Content, String type) {
		return "-----BEGIN " + type + "-----\n" + base64Content + "\n-----END " + type + "-----";
	}

	/**
	 * 檢查檔名是否符合允許的擴展名
	 */
	public static boolean validExtensionsCheck(String fileName, String validExtensionsString) {
		String lowerFileName = fileName.toLowerCase();
		String[] extensions = validExtensionsString.split(",");
		for (String ext : extensions) {
			if (lowerFileName.endsWith(ext.trim())) {
				return true;
			}
		}
		return false;
	}

	/*
	 * 判斷傳入字串不全部為空
	 */
	public static boolean isAnyNotEmpty(String... strings) {
		return Arrays.stream(strings).anyMatch(StringUtils::hasText);
	}

	/**
	 * 檢查給定的字符串是否是有效的 Base64 編碼
	 *
	 * @param str 要檢查的字符串
	 * @return true 如果是有效的 Base64 編碼，否則 false
	 */
	public static boolean isValidBase64(String str) {
		try {
			Base64.getDecoder().decode(str);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	/**
	 * 將逗號分隔的字串轉換為字串列表
	 *
	 * @param commaSeparatedString 逗號分隔的字串，可為null或空字串
	 * @return 字串列表，若輸入為null或空字串則返回空列表
	 */
	public static List<String> splitByComma(String commaSeparatedString) {
		return Optional.ofNullable(commaSeparatedString).filter(str -> !str.isEmpty())
				.map(str -> Arrays.asList(str.split(","))).orElse(List.of());
	}

	/**
	 * 將指定分隔符分隔的字串轉換為字串列表
	 *
	 * @param str       要分割的字串，可為null或空字串
	 * @param delimiter 分隔符
	 * @return 字串列表，若輸入為null或空字串則返回空列表
	 */
	public static List<String> splitByDelimiter(String str, String delimiter) {
		return Optional.ofNullable(str).filter(s -> !s.isEmpty()).map(s -> Arrays.asList(s.split(delimiter)))
				.orElse(List.of());
	}

	/**
	 * 按字節截斷字串（加省略號）
	 */
	public static String truncateBytesWithEllipsis(String text, int maxByteLength) {
		return truncateByBytes(text, maxByteLength, true);
	}

	/**
	 * 按字節截斷字串（不加省略號）
	 */
	public static String truncateBytesWithoutEllipsis(String text, int maxByteLength) {
		return truncateByBytes(text, maxByteLength, false);
	}

	/**
	 * 內部實現
	 */
	private static String truncateByBytes(String text, int maxByteLength, boolean addEllipsis) {
		if (text == null || text.getBytes(StandardCharsets.UTF_8).length <= maxByteLength) {
			return text;
		}

		int targetLength = addEllipsis ? maxByteLength - 3 : maxByteLength;
		String result = text;

		while (result.getBytes(StandardCharsets.UTF_8).length > targetLength) {
			result = result.substring(0, result.length() - 1);
		}

		return addEllipsis ? result + SystemConstants.DOT_STRING : result;
	}

}
