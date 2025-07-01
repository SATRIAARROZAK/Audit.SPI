package Audit.Auditing.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID; // Pastikan UUID di-import

@Entity
@Table(name = "kertas_kerja_audit")
@Data
@EntityListeners(AuditingEntityListener.class)
public class KertasKerjaAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "surat_tugas_id", nullable = false)
    private SuratTugas suratTugas;

    // Kolom ini mengelompokkan beberapa baris tahapan ke dalam satu prosedur
    @Column(name = "prosedur_grup", nullable = false)
    private UUID prosedurGroup;

    @Column(columnDefinition = "TEXT")
    private String prosedur; // Ini akan menyimpan nama prosedur

    @Column(columnDefinition = "TEXT")
    private String tahapan; // Ini akan menyimpan satu tahapan

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dilakukan_oleh_id", nullable = false)
    private User dilakukanOleh;

    @Column(name = "dokumen_path")
    private String dokumenPath;

    @CreatedDate
    @Column(name = "tgl_dibuat", nullable = false, updatable = false)
    private LocalDateTime tanggalDibuat;
}