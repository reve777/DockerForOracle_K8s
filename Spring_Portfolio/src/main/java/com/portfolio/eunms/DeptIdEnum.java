package com.portfolio.eunms;

import java.util.Arrays;

/**
 * 部門ID枚舉，定義系統支援的部門代碼與名稱對應
 */
public enum DeptIdEnum {
	MKT("市場行銷部"),
    HR("人力資源部"),
    FIN("財務管理部"),
    RD("研發設計部"),
    PR("公共關係室"),
    BD("業務開發部"),
    QA("品質保證部"),
    SCM("供應鏈管理部"),
    ESG("永續發展室"),
    SEC("董事會秘書處"),
    LS("物流資材部");

    private final String deptName;

    DeptIdEnum(String deptName) {
        this.deptName = deptName;
    }

    public String getDeptName() {
        return deptName;
    }

    /**
     * 根據部門代碼或名稱查找對應的枚舉
     * @param value 部門代碼(如ABDD)或部門名稱(如通路推展部)
     * @return 對應的DeptID枚舉，找不到則返回null
     */
    public static DeptIdEnum getByValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return Arrays.stream(values())
            .filter(dept -> dept.name().equals(value) || dept.deptName.equals(value))
            .findFirst()
            .orElse(null);
    }

    /**
     * 轉換部門資訊為標準部門代碼
     * @param deptId 部門代碼
     * @param deptName 部門名稱
     * @return 標準化的部門代碼，找不到則返回原deptId
     */
    public static String convertDeptId(String deptId, String deptName) {
        DeptIdEnum dept = getByValue(deptId);
        if (dept != null) {
            return dept.name();
        }

        dept = getByValue(deptName);
        return dept != null ? dept.name() : deptId;
    }

}
