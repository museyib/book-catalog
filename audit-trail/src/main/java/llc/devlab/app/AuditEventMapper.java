package llc.devlab.app;

import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AuditEventMapper {

    public AuditEventDto toDto(AuditEvent auditEvent) {
        return new AuditEventDto(
                auditEvent.getId(),
                auditEvent.getLogTime(),
                auditEvent.getActor(),
                auditEvent.getAction(),
                auditEvent.getTarget());
    }

    public AuditEvent toEntity(AuditEventRequest request) {
        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setLogTime(Instant.now());
        auditEvent.setActor(request.actor());
        auditEvent.setAction(request.action());
        auditEvent.setTarget(request.target());
        return auditEvent;
    }
}
