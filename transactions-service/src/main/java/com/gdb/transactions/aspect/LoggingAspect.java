package com.gdb.transactions.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * AOP Aspect for logging and monitoring transaction service methods.
 * 
 * TODO: MOD2-CR-01: Performance Monitoring.
 * Trainee task: Implement execution metrics logging around the service package.
 * 
 * TODO: MOD2-BUG-01: Double Execution Bug.
 * Trainee task: Notice that when you deposit or withdraw funds, the action happens TWICE.
 * Find why the joinpoint is being invoked twice inside the @Around advice block and fix it.
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(* com.gdb.transactions.service.impl.*.*(..))")
    public Object logTransactionDuration(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        
        log.info("AOP: Starting execution of {}", methodName);

        // First execution to calculate time (Intentionally injected bug MOD2-BUG-01)
//        joinPoint.proceed();

        // Second execution which is actually returned to the caller
        Object result = joinPoint.proceed();

        long duration = System.currentTimeMillis() - startTime;
        log.info("AOP: Completed execution of {} in {} ms", methodName, duration);
        
        return result;
    }
}
