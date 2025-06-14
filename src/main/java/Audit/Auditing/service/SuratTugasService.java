package Audit.Auditing.service;

import Audit.Auditing.dto.SuratTugasDTO;
import Audit.Auditing.model.StatusSuratTugas;
import Audit.Auditing.model.SuratTugas; // Import ini
import Audit.Auditing.model.User;

import java.time.LocalDate;
import java.util.List; // Import ini
import java.util.Optional; // Import ini

public interface SuratTugasService {
    void createSuratTugas(SuratTugasDTO suratTugasDTO);

    // --- TAMBAHKAN METODE-METODE DI BAWAH INI ---
    List<SuratTugas> getAllSuratTugas();

    Optional<SuratTugas> getSuratTugasById(Long id);

    void updateSuratTugas(Long id, SuratTugasDTO suratTugasDTO);

    void deleteSuratTugas(Long id);

    List<SuratTugas> getSuratByStatus(StatusSuratTugas status);

    void reviewAndSetDates(Long suratId, LocalDate tanggalMulai, LocalDate tanggalSelesai);

    void approveSuratTugas(Long suratId, User approver);
    void rejectSuratTugas(Long suratId, String catatan, User approver);
}