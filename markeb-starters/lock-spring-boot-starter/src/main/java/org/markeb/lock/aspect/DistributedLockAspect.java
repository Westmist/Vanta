package org.markeb.lock.aspect;

import org.markeb.lock.DistributedLock;
import org.markeb.lock.annotation.DistributedLocked;
import org.markeb.lock.exception.LockAcquisitionException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

/**
 * 分布式锁切面
 */
@Aspect
public class DistributedLockAspect {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockAspect.class);

    private final DistributedLock distributedLock;
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    public DistributedLockAspect(DistributedLock distributedLock) {
        this.distributedLock = distributedLock;
    }

    @Around("@annotation(distributedLocked)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLocked distributedLocked) throws Throwable {
        String lockKey = parseKey(distributedLocked.key(), joinPoint);

        boolean locked = false;
        try {
            if (distributedLocked.leaseTime() > 0) {
                locked = distributedLock.tryLock(
                        lockKey,
                        distributedLocked.waitTime(),
                        distributedLocked.leaseTime(),
                        distributedLocked.timeUnit()
                );
            } else {
                locked = distributedLock.tryLock(
                        lockKey,
                        distributedLocked.waitTime(),
                        distributedLocked.timeUnit()
                );
            }

            if (!locked) {
                log.warn("Failed to acquire lock: {}", lockKey);
                throw new LockAcquisitionException(distributedLocked.failMessage());
            }

            return joinPoint.proceed();
        } finally {
            if (locked) {
                distributedLock.unlock(lockKey);
            }
        }
    }

    private String parseKey(String keyExpression, ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        String[] paramNames = nameDiscoverer.getParameterNames(method);
        if (paramNames == null || paramNames.length == 0) {
            return keyExpression;
        }

        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        try {
            Expression expression = parser.parseExpression(keyExpression);
            Object value = expression.getValue(context);
            return value != null ? value.toString() : keyExpression;
        } catch (Exception e) {
            return keyExpression;
        }
    }
}

