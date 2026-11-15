package llc.devlab.app;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditEventService;
    private final ClientIpProvider clientIpProvider;
    private final CurrentActorProvider currentActorProvider;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();
    private final Map<String, Expression> expressionCache = new ConcurrentHashMap<>();

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void audit(JoinPoint joinPoint, Auditable auditable, Object result) {
        String targetId = resolveTargetId(joinPoint, auditable.targetId(), result);
        AuditEventRequest request = new AuditEventRequest(
                currentActorProvider.getCurrentActorId(),
                auditable.action(),
                auditable.riskLevel(),
                auditable.targetType(),
                targetId,
                clientIpProvider.getClientIp());
        auditEventService.logEvent(request);
    }

    private String resolveTargetId(JoinPoint joinPoint, String spelExpression, Object result) {
        if (spelExpression == null || spelExpression.isBlank()) {
            return null;
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // Binds all method parameters automatically (e.g., #id, #bookDto)
        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(
                joinPoint.getTarget(), method, joinPoint.getArgs(), nameDiscoverer);

        // Bind the return value to #result (just like Spring's @Cacheable)
        context.setVariable("result", result);

        Expression expression = expressionCache.computeIfAbsent(spelExpression, parser::parseExpression);
        Object value = expression.getValue(context);
        return value != null ? value.toString() : null;
    }
}
