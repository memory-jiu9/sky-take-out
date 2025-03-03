package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面类，用来实现公共字段的自动填充
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    /**
     * 设置切入点，在com.sky.mapper下的所有类的所有方法 && 该方法添加了@AutoFill注解都会被增强
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {
    }

    /**
     * 前置插入，在mapper类的方法执行之前进行插入
     *
     * @param joinPoint
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("现在进行切面编程");
        // 获取目标方法上的注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill annotation = signature.getMethod().getAnnotation(AutoFill.class);

        // 获取注解传入的参数
        OperationType value = annotation.value();
        // 获取目标方法的参数，第一个参数就是数据对象
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            log.info("autoFill:没有参数传入");
            return;
        }
        Object entity = args[0];

        // 准备要使用的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        // 判断该注解是插入操作还是更新操作
        if (value == OperationType.INSERT) {
            try {
                // 获取设置时间的方法
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                // 获取设置用户的方法
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);


                setCreateTime.invoke(entity, now);
                setUpdateTime.invoke(entity, now);

                setCreateUser.invoke(entity,currentId);
                setUpdateUser.invoke(entity,currentId);

            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else if (value == OperationType.UPDATE) {
            try {
                // 获取设置时间的方法
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                // 获取设置用户的方法
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setUpdateTime.invoke(entity, now);

                setUpdateUser.invoke(entity,currentId);

            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
