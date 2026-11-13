package llc.devlab.app;

public record AuditEventRequest(String actorId, AuditAction action, String targetType, String targetId, String ipAddress) {
}
