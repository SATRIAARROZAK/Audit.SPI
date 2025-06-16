package Audit.Auditing.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "surat_tugas_history")
@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SuratTugasHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "surat_tugas_id", nullable = false)
    private SuratTugas suratTugas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSuratTugas status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id") // User yang melakukan aksi
    private User actor;

    @Column(columnDefinition = "TEXT")
    private String notes; // Catatan, misal: alasan penolakan

    @CreatedDate
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public SuratTugasHistory(SuratTugas suratTugas, StatusSuratTugas status, User actor, String notes) {
        this.suratTugas = suratTugas;
        this.status = status;
        this.actor = actor;
        this.notes = notes;
    }
}