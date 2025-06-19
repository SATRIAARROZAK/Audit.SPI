package Audit.Auditing.model;

public enum JenisAudit {
    RENCANA_AUDIT("Rencana Audit"),
    RENCANA_REVIEW_AUDIT("Rencana Review Audit"),
    LAYANAN("Layanan");

    private final String displayName;

    JenisAudit(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}