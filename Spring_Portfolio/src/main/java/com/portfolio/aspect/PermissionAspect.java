package com.portfolio.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.portfolio.bean.PermissionBean;
import com.portfolio.constants.MessageConstants;
import com.portfolio.constants.SystemConstants;
import com.portfolio.eunms.PermissionEnum;
import com.portfolio.exceptions.UnauthorizedException;

import jakarta.servlet.http.HttpServletRequest;

/*
 * 負責「實際執行」權限檢查，決定是否允許方法繼續執行，並對結果進行處理。
 */
@Aspect
@Component
@Order(1)
public class PermissionAspect {

    /**
     * 使用 @Around 環繞通知，攔截所有帶有 @RequiresPermission 註解的方法
     * 此處回傳值需跟Controller一致
     * @param joinPoint 被攔截的方法連接點
     * @param requiresPermission 該方法上的權限要求註解，可取得所需的權限枚舉
     * @return 方法執行結果，或在權限不足時回傳 error 頁面
     * @throws Throwable 如果被攔截的方法拋出例外，這裡也會繼續拋出
     */
    @Around("@annotation(requiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequiresPermission requiresPermission) throws Throwable {
        // 從 Request 取得 permissionBean
    	RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
    	if (requestAttributes == null) {
    	    throw new Exception("No request attributes available.");
    	}
    	HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
    	// 從 Request 中取得之前放入的 permissionBean ( GlobalModelAdviceHandler 已經放入該 bean)
    	PermissionBean permissionBean = (PermissionBean) request.getAttribute(SystemConstants.PERMISSION_BEAN);

    	// 檢查是否取得了 permissionBean，或是權限不足
    	System.out.println(hasPermission(permissionBean, requiresPermission.value()));
    	if (permissionBean == null || !hasPermission(permissionBean, requiresPermission.value())) {
    	    throw new UnauthorizedException(MessageConstants.NO_PERMISSION_ERROR_MSG);
    	}

        return joinPoint.proceed();
    }

    /**
     * 根據 permissionBean 與所需的權限枚舉，判斷使用者是否具備該權限
     * @param permissionBean 使用者的權限 bean
     * @param requiredPermission 方法要求的權限枚舉
     * @return true 表示有權限，false 則無權限
     */
    private boolean hasPermission(PermissionBean permissionBean, PermissionEnum requiredPermission) {
        switch (requiredPermission) {
            case QUERY:
                return permissionBean.isCanQuery();
            case ADD:
                return permissionBean.isCanAdd();
            case DELETE:
                return permissionBean.isCanDelete();
            case UPDATE:
                return permissionBean.isCanUpdate();
            case CONFIRM:
                return permissionBean.isCanConfirm();
            case CONFIRM_RECORD_QUERY:
                return permissionBean.isCanConfirmRecordQuery();
            default:
                return false;
        }
    }

}
