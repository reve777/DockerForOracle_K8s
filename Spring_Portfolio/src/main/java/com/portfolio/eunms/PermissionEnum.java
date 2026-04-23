package com.portfolio.eunms;

/**
 * 權限列舉，參考 Portal權限表
 */
public enum PermissionEnum {
    UPDATE(0),           // 修改: bit 0 (值=1)
    DELETE(1),           // 刪除: bit 1 (值=2)
    ADD(2),              // 新增: bit 2 (值=4)
    QUERY(3),            // 查詢: bit 3 (值=8)
    CONFIRM(5),          // 覆核(包含覆核查詢+覆核動作): bit 5 (值=32)
    CONFIRM_RECORD_QUERY(6);    // 覆核紀錄查詢: bit 6 (值=64)

    private final int bitIndex;

    PermissionEnum(int bitIndex) {
        this.bitIndex = bitIndex;
    }

    public int getBitIndex() {
        return bitIndex;
    }

}
