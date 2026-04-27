package com.portfolio.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.portfolio.exceptions.ValidationException;
import com.portfolio.uitls.LogUtils;



/**
 * Log記錄切面方法
 */
@Aspect
@Component
@Order(2)
public class ControllerLogAspect {

    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public Object logControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
    	long startTime = System.currentTimeMillis();
        // 獲取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getName();
        String className = signature.getDeclaringType().getSimpleName();
        String fullMethodName = className + "." + methodName;

        // 記錄參數（過濾 Model 和 BindingResult）
        Object[] args = joinPoint.getArgs();
        LogUtils.parameters(fullMethodName, args);

        try {
            // 執行原方法
        	Object result = joinPoint.proceed();

            // 計算執行時間
            long executionTime = System.currentTimeMillis() - startTime;

            // 記錄成功日誌
            LogUtils.operate(String.format("成功執行 %s (耗時: %d ms)", fullMethodName, executionTime));

            return result;
        } catch (ValidationException ve) {
            // 對於驗證例外，僅記錄警告信息，不記錄堆疊
            long executionTime = System.currentTimeMillis() - startTime;
            LogUtils.bizWarn(String.format("%s 驗證失敗: %s (耗時: %d ms)", fullMethodName, ve.getMessage(), executionTime));
            throw ve;
        } catch (Exception e) {
            // 計算執行時間
            long executionTime = System.currentTimeMillis() - startTime;
            // 如果異常是鎖衝突，這可能是重試中的一次，可以記錄為 WARN 而非 ERROR
            if (e instanceof org.springframework.dao.PessimisticLockingFailureException) {
                LogUtils.bizWarn(String.format("%s 偵測到鎖競爭，準備重試 (耗時: %d ms)", fullMethodName, executionTime));
            } else {
                // 真正的業務失敗或系統崩潰
                LogUtils.error(String.format("%s 執行失敗，原因: %s (耗時: %d ms)", 
                              fullMethodName, e.getMessage(), executionTime), e);
            }
            throw e;
        }
    }

}
