package Audit.Auditing.model;

public enum StatusSuratTugas {
    BARU("pending"),
    REVIEW_SEKRETARIS("review"),
    PERSETUJUAN_KEPALASPI("pending approval"),
    DISETUJUI("disetujui"),
    DITOLAK("ditolak"),
    DIKEMBALIKAN_KE_ADMIN("revisi"), // New status
    SELESAI("Selesai");

    private final String displayName;

    StatusSuratTugas(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}