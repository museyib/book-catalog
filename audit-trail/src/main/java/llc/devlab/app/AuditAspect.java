package llc.devlab.app;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditEventService;

    @AfterReturning(pointcut = "@annotation(auditable)")
    public void audit(Auditable auditable) {
        AuditEventRequest request = new AuditEventRequest(auditable.actor(), auditable.action(), auditable.target());
        auditEventService.logEvent(request);
    }
}
