package llc.devlab.app;

import java.time.Instant;

public record AuditFilterRequest(String actorId, String action, String targetType, String targetId, Instant dateFrom, Instant dateTo) {
}
