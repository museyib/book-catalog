package llc.devlab.app;

import java.time.Instant;

public record AuditEventDto(Long id, Instant logTime, String actor, String action, String target) {
}
