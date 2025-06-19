package Audit.Auditing.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "surat_tugas")
@Data
@EntityListeners(AuditingEntityListener.class) // Untuk auto-populate createdDate
public class SuratTugas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, name = "nomor_surat") // 1. Nomer surat
    private String nomorSurat;

    @Column(columnDefinition = "TEXT", name = "deskripsi_surat") // 2. Deskripsi surat (opsional, remove nullable=false)
    private String deskripsiSurat;

    @Enumerated(EnumType.STRING) // 4. Tujuan (form select)
    @Column(nullable = false, name = "jenis_audit")
    private JenisAudit jenisAudit; // Use the new enum

    @Column(nullable = false, name = "file_path")
    private String filePath; // Path ke file PDF/DOCX yang di-upload

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSuratTugas status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ketua_tim_id", nullable = false)
    private User ketuaTim;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "surat_tugas_anggota", joinColumns = @JoinColumn(name = "surat_tugas_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> anggotaTim;

    @Column(name = "tanggal_mulai_audit", nullable = false) // 3. set tgl mulai audit (now mandatory from admin)
    private LocalDate tanggalMulaiAudit;

    @Column(name = "tanggal_selesai_audit", nullable = false) // 3. set tgl selesai audit (now mandatory from admin)
    private LocalDate tanggalSelesaiAudit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private User approver; // User yang menyetujui/menolak (Kepala SPI atau Sekretaris)

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(columnDefinition = "TEXT")
    private String catatanPersetujuan;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "suratTugas", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("timestamp ASC")
    private List<SuratTugasHistory> history;
}