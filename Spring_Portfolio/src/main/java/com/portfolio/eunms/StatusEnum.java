package com.portfolio.eunms;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

public final class StatusEnum {

    // 防止外部實例化，避免破壞列舉的單例特性，也避免出現不合法的狀態
    private StatusEnum() {
    }

    // 通用狀態代碼常數
    public static final String STATUS_YES = "Y";
    public static final String STATUS_NO = "N";
    public static final String STATUS_WAIT = "W";
    public static final String STATUS_ALL = "ALL";

    // 狀態描述常數
    public static final String DESC_NORMAL = "正常";
    public static final String DESC_ENABLED = "啟用";
    public static final String DESC_WAIT_ENABLED = "待啟用";
    public static final String DESC_WAIT_REVIEW = "待覆核";
    public static final String DESC_ALL = "全選";

    // 操作類型常數
    public static final String TYPE_ADD = "A";
    public static final String TYPE_DELETE = "D";
    public static final String TYPE_UPDATE = "U";

    public static final String TYPE_DESC_ADD = "新增";
    public static final String TYPE_DESC_DELETE = "刪除";
    public static final String TYPE_DESC_UPDATE = "修改";

    // 覆核相關標題常數
    public static final String TITLE_BEFORE = "異動前";
    public static final String TITLE_AFTER = "異動後";

    /**
     * (暫存檔) type異動類型 通用列舉
     */
    public enum CommonTempTypeEnum {
        A(TYPE_ADD, TYPE_DESC_ADD),
        D(TYPE_DELETE, TYPE_DESC_DELETE),
        U(TYPE_UPDATE, TYPE_DESC_UPDATE);

        @Getter
        private final String code;
        @Getter
        private final String description;

        CommonTempTypeEnum(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public static CommonTempTypeEnum fromCode(String code) {
            return Arrays.stream(values())
                         .filter(e -> e.getCode().equals(code))
                         .findFirst()
                         .orElse(null);
        }

        public Map<String, String> getTitleAttributes() {
            Map<String, String> titles = new HashMap<>();
            if (this == U) {
                titles.put("title1", TITLE_BEFORE);
                titles.put("title2", TITLE_AFTER);
            } else {
                titles.put("title3", this.getDescription());
            }
            return titles;
        }
    }

    /**
     * (主檔) status狀態 通用列舉
     */
    public enum CommonStatusEnum {
        Y(STATUS_YES, DESC_NORMAL),
        W(STATUS_WAIT, DESC_WAIT_REVIEW);

        @Getter
        private final String code;
        @Getter
        private final String description;

        CommonStatusEnum(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public static CommonStatusEnum fromCode(String code) {
            return Arrays.stream(values())
                    .filter(e -> e.getCode().equals(code))
                    .findFirst()
                    .orElse(null);
		}
    }

    /**
     * 機構/收單機構狀態列舉
     * BANK_MST/ACQUIRER_MST.status
     * Inst. = Institutions 縮寫
     */
    public enum InstStatusEnum {
        ALL(STATUS_ALL, DESC_ALL), // 畫面選單用
        Y(STATUS_YES, DESC_ENABLED),
        N(STATUS_NO, DESC_WAIT_ENABLED),
        W(STATUS_WAIT, DESC_WAIT_REVIEW);

        @Getter
        private final String code;
        @Getter
        private final String description;

        InstStatusEnum(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public static InstStatusEnum fromCode(String code) {
            if (code == null) {
                return null;
            }
            return Arrays.stream(values())
                    .filter(e -> e.getCode().equals(code))
                    .findFirst()
                    .orElse(null);
		}
	}

}
