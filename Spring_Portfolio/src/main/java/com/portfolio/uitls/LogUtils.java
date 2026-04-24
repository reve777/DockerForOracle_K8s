package com.portfolio.uitls;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import lombok.extern.log4j.Log4j2;

/**
 * 日誌輔助工具類，提供統一的日誌格式
 */
@Log4j2
public final class LogUtils {

	// 日誌前綴
	private static final String STR_OP = "[使用] ";
	private static final String STR_SYS = "[SYSTEM] ";
	private static final String STR_BIZ_WARN = "[業務邏輯] ";
	private static final String STR_SYS_ERR = "[錯誤] ";

	// 防止實例化
	private LogUtils() {
		throw new AssertionError("工具類不應該被實例化");
	}

	/**
	 * 清除上下文
	 */
	public static void clear() {
		ThreadContext.clearAll();
	}

	/**
	 * 記錄操作相關日誌
	 */
	public static void operate(final String msg) {
		if (log.isInfoEnabled()) {
			log.info(formatMessage(STR_OP, msg));
		}
	}

	/**
	 * 記錄操作相關日誌 (支持格式化字符串)
	 */
	public static void operate(final String format, final Object... args) {
		if (log.isInfoEnabled()) {
			String msg = String.format(format.replace("{}", "%s"), args);
			log.info(formatMessage(STR_OP, msg));
		}
	}

	/**
	 * 記錄系統日誌
	 */
	public static void system(final String msg) {
		if (log.isInfoEnabled()) {
			log.info(formatMessage(STR_SYS, msg));
		}
	}

	/**
	 * 記錄系統日誌 (支持格式化字符串)
	 */
	public static void system(final String format, final Object... args) {
		if (log.isInfoEnabled()) {
			String msg = String.format(format.replace("{}", "%s"), args);
			log.info(formatMessage(STR_SYS, msg));
		}
	}

	/**
	 * 記錄業務錯誤
	 */
	public static void bizWarn(final String msg) {
		if (log.isWarnEnabled()) {
			log.warn(formatMessage(STR_BIZ_WARN, msg));
		}
	}

	/**
	 * 記錄業務錯誤 (支持格式化字符串)
	 */
	public static void bizWarn(final String format, final Object... args) {
		if (log.isWarnEnabled()) {
			String msg = String.format(format.replace("{}", "%s"), args);
			log.warn(formatMessage(STR_BIZ_WARN, msg));
		}
	}

	/**
	 * 記錄系統警告
	 */
	public static void warn(final String msg) {
		if (log.isWarnEnabled()) {
			log.warn(formatMessage(STR_SYS, msg));
		}
	}

	/**
	 * 記錄系統日誌 (支持格式化字符串)
	 */
	public static void warn(final String format, final Object... args) {
		if (log.isWarnEnabled()) {
			String msg = String.format(format.replace("{}", "%s"), args);
			log.warn(formatMessage(STR_SYS, msg));
		}
	}

	/**
	 * 記錄系統錯誤
	 */
	public static void error(final String msg, final Throwable t) {
		if (log.isErrorEnabled()) {
			log.error(formatMessage(STR_SYS_ERR, msg), t);
		}
	}

	/**
	 * 除錯日誌
	 */
	public static void debug(final String msg) {
		if (log.isDebugEnabled()) {
			log.debug(formatMessage(STR_OP, msg));
		}
	}

	/**
	 * 除錯日誌 (支持格式化字符串)
	 */
	public static void debug(final String format, final Object... args) {
		if (log.isDebugEnabled()) {
			String msg = String.format(format.replace("{}", "%s"), args);
			log.debug(formatMessage(STR_OP, msg));
		}
	}

	/**
	 * 格式化日誌消息
	 */
	private static String formatMessage(final String prefix, final String msg) {
		StringBuilder formattedMsg = new StringBuilder(prefix);

//        // 添加請求ID (如果存在)
//        String requestId = ThreadContext.get(SystemConstants.REQUEST_ID_KEY);
//        if (StringUtils.isNotBlank(requestId)) {
//            formattedMsg.append("[reqId:").append(requestId).append("] ");
//        }
//
//        // 添加UserId (如果存在)
//        String userId = ThreadContext.get(SystemConstants.USER_ID_KEY);
//        if (StringUtils.isNotBlank(userId)) {
//            formattedMsg.append("[userId:").append(userId).append("] ");
//        }

		// 添加原始消息
		formattedMsg.append(msg);

		// 移除換行符號，壓縮多餘空格
		return formattedMsg.toString().replaceAll("\\r?\\n", " ").replaceAll("\\s+", " ");
	}

	/**
	 * 簡化的參數日誌方法
	 */
	public static void parameters(final String methodName, final Object... args) {
		if (log.isInfoEnabled()) {
			StringBuilder params = new StringBuilder();
			if (args != null) {
				for (Object arg : args) {
					// 排除 Model(通常包含大量視圖渲染相關的數據), BindingResult 交給專門handler處理
					if (arg != null &&
							!(arg instanceof Model) && !(arg instanceof BindingResult)) {
						params.append(arg).append(", ");
					}
				}
				// 移除最後的逗號和空格（如果有的話）
				if (params.length() > 2) {
					params.setLength(params.length() - 2);
				}
			}
			log.info(formatMessage(STR_OP, "方法 " + methodName + " 參數: [" + params + "]"));
		}
	}

}
