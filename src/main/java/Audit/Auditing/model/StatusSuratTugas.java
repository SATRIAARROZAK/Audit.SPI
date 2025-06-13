package Audit.Auditing.model;

public enum StatusSuratTugas {
    BARU("Baru Dibuat Admin"),
    REVIEW_SEKRETARIS("Review Sekretaris"),
    PERSETUJUAN_KEPALASPI("Persetujuan Kepala SPI"),
    DISETUJUI("Disetujui"),
    DITOLAK("Ditolak"),
    SELESAI("Selesai");

    private final String displayName;

    StatusSuratTugas(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}