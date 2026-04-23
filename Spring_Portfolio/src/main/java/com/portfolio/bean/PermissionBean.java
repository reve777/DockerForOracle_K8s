package com.portfolio.bean;



import com.portfolio.aspect.PermissionAnnotation;
import com.portfolio.eunms.PermissionEnum;

import lombok.Data;

@Data
public class PermissionBean {
    @PermissionAnnotation(permission = PermissionEnum.QUERY)
    private boolean canQuery;

    @PermissionAnnotation(permission = PermissionEnum.ADD)
    private boolean canAdd;

    @PermissionAnnotation(permission = PermissionEnum.DELETE)
    private boolean canDelete;

    @PermissionAnnotation(permission = PermissionEnum.UPDATE)
    private boolean canUpdate;

    @PermissionAnnotation(permission = PermissionEnum.CONFIRM)
    private boolean canConfirm; // 包含覆核查詢+覆核動作

    @PermissionAnnotation(permission = PermissionEnum.CONFIRM_RECORD_QUERY)
    private boolean canConfirmRecordQuery;
}

