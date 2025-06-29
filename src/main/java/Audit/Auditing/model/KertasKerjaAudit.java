package Audit.Auditing.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

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

    @Column(columnDefinition = "TEXT")
    private String prosedur;

    @Column(columnDefinition = "TEXT")
    private String tahapan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dilakukan_oleh_id", nullable = false)
    private User dilakukanOleh;

    @Column(name = "dokumen_path")
    private String dokumenPath;

    @CreatedDate
    @Column(name = "tgl_dibuat", nullable = false, updatable = false)
    private LocalDateTime tanggalDibuat;
}