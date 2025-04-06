package ordertracker.apllication.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Before("execution(* ordertracker.api.controllers.*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        if (logger.isInfoEnabled()) {
            logger.info("Method called: {} with args: {}",
                    joinPoint.getSignature().toShortString(),
                    joinPoint.getArgs());
        }
    }

    @AfterReturning(pointcut = "execution(* ordertracker.api.controllers.*.*(..))",
            returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        if (logger.isInfoEnabled()) {
            logger.info("Method completed: {} with result: {}",
                    joinPoint.getSignature().toShortString(),
                    result);
        }
    }

    @AfterThrowing(pointcut = "within(@org.springframework.web.bind.annotation.RestController *)",
            throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        if (logger.isErrorEnabled()) {
            logger.error("Exception in method: {} with message: {}",
                    joinPoint.getSignature().toShortString(),
                    ex.getMessage(), ex); // Добавлен сам exception для stack trace
        }
    }
}