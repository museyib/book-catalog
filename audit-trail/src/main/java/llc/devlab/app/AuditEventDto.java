package llc.devlab.app;

import java.net.InetAddress;
import java.time.Instant;

public record AuditEventDto(Long id,
                            Instant occurredAt,
                            String actorId,
                            AuditAction action,
                            String targetType,
                            String targetId,
                            InetAddress ipAddress) {
}
