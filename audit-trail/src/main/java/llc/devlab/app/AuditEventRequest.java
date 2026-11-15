package llc.devlab.app;

public record AuditEventRequest(String actorId, AuditAction action, RiskLevel riskLevel, String targetType, String targetId, String ipAddress) {
}
