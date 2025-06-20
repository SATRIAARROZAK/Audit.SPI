package Audit.Auditing.service;

import Audit.Auditing.dto.SuratTugasDTO;
import Audit.Auditing.model.StatusSuratTugas;
import Audit.Auditing.model.SuratTugas;
import Audit.Auditing.model.User;
import org.springframework.data.domain.Page; // Import Page
import org.springframework.data.domain.Pageable; // Import Pageable

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SuratTugasService {
    void createSuratTugas(SuratTugasDTO suratTugasDTO);

    List<SuratTugas> getAllSuratTugas(); // Keep for backward compatibility if needed, or replace

    Page<SuratTugas> getAllSuratTugas(Pageable pageable); // New: for paginated list

    Page<SuratTugas> searchSuratTugas(String keyword, Pageable pageable); // New: for search with pagination

    Optional<SuratTugas> getSuratTugasById(Long id);

    void updateSuratTugas(Long id, SuratTugasDTO suratTugasDTO);

    void deleteSuratTugas(Long id);

    List<SuratTugas> getSuratByStatus(StatusSuratTugas status);

    void reviewSuratTugas(Long suratId);

    void approveSuratTugas(Long suratId, User approver);

    void rejectSuratTugas(Long suratId, String catatan, User approver);

    List<SuratTugas> getTugasUntukPegawai(User user);

    Page<SuratTugas> getTugasUntukPegawai(User user, Pageable pageable); // New: for paginated list for pegawai

    void returnSuratTugasForRevision(Long suratId, String catatan, User secretary);

    // Di dalam interface SuratTugasService
    Page<SuratTugas> getTugasUntukKetuaTim(User ketuaTim, Pageable pageable, String keyword);
}