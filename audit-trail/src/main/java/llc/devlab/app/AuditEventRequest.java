package llc.devlab.app;

public record AuditEventRequest(String actor, String action, String target) {
}
