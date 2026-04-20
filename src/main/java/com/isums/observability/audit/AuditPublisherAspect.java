package com.isums.observability.audit;

import com.isums.observability.context.AuditContext;
import com.isums.observability.masking.SensitiveDataMasker;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Aspect
public class AuditPublisherAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditPublisherAspect.class);

    private final AuditPublisher auditPublisher;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public AuditPublisherAspect(AuditPublisher auditPublisher) {
        this.auditPublisher = auditPublisher;
    }

    @Around("@annotation(auditAction)")
    public Object around(ProceedingJoinPoint joinPoint, AuditAction auditAction) throws Throwable {
        try {
            Object result = joinPoint.proceed();
            AuditEvent event = createEvent(joinPoint, auditAction, result, AuditStatus.SUCCESS, null);
            publishSafely(event);
            return result;
        } catch (Throwable ex) {
            if (auditAction.includeFailure()) {
                AuditEvent event = createEvent(joinPoint, auditAction, null, AuditStatus.FAILURE, ex);
                publishSafely(event);
            }
            throw ex;
        }
    }

    private void publishSafely(AuditEvent event) {
        markRequestAudited();
        try {
            auditPublisher.publish(event);
        } catch (Exception ex) {
            log.warn("Failed to publish annotated audit event action={}", event.getAction(), ex);
        }
    }

    private void markRequestAudited() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            attributes.setAttribute(AuditRequestAttributes.PUBLISHED, Boolean.TRUE, RequestAttributes.SCOPE_REQUEST);
        }
    }

    private AuditEvent createEvent(
            ProceedingJoinPoint joinPoint,
            AuditAction auditAction,
            Object result,
            AuditStatus status,
            Throwable throwable) {
        AuditEvent event = AuditEvent.fromContext(AuditContext.currentOrMdc());
        event.setAction(auditAction.action());
        event.setResourceType(auditAction.resourceType());
        event.setResourceId(resolveResourceId(joinPoint, auditAction, result));
        event.setStatus(status);
        if (throwable != null) {
            event.setErrorCode(throwable.getClass().getSimpleName());
            event.setErrorMessage(SensitiveDataMasker.maskString(throwable.getMessage()));
        }
        return event;
    }

    private String resolveResourceId(ProceedingJoinPoint joinPoint, AuditAction auditAction, Object result) {
        if (!StringUtils.hasText(auditAction.resourceIdExpression())) {
            return null;
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        StandardEvaluationContext context = new StandardEvaluationContext();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(signature.getMethod());
        Object[] args = joinPoint.getArgs();
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }
        context.setVariable("args", args);
        context.setVariable("result", result);
        Object value = parser.parseExpression(auditAction.resourceIdExpression()).getValue(context);
        return value == null ? null : String.valueOf(value);
    }
}
