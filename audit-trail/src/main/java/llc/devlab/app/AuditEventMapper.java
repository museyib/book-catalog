package llc.devlab.app;

import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;

@Component
public class AuditEventMapper {

    public AuditEventDto toDto(AuditEvent auditEvent) {
        return new AuditEventDto(
                auditEvent.getId(),
                auditEvent.getOccurredAt(),
                auditEvent.getActorId(),
                auditEvent.getAction(),
                auditEvent.getTargetType(),
                auditEvent.getTargetId(),
                auditEvent.getIpAddress());
    }

    public AuditEvent toEntity(AuditEventRequest request) {
        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setOccurredAt(Instant.now());
        auditEvent.setActorId(request.actorId());
        auditEvent.setAction(request.action());
        auditEvent.setTargetType(request.targetType());
        auditEvent.setTargetId(request.targetId());
        try {
            auditEvent.setIpAddress(InetAddress.getByName(request.ipAddress()));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        return auditEvent;
    }
}
