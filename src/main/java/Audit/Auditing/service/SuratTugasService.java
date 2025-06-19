package Audit.Auditing.service;

import Audit.Auditing.dto.SuratTugasDTO;
import Audit.Auditing.model.StatusSuratTugas;
import Audit.Auditing.model.SuratTugas;
import Audit.Auditing.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SuratTugasService {
    void createSuratTugas(SuratTugasDTO suratTugasDTO);

    List<SuratTugas> getAllSuratTugas();

    Optional<SuratTugas> getSuratTugasById(Long id);

    void updateSuratTugas(Long id, SuratTugasDTO suratTugasDTO);

    void deleteSuratTugas(Long id);

    List<SuratTugas> getSuratByStatus(StatusSuratTugas status);

    // Simplified review method (dates are now set by admin)
    void reviewSuratTugas(Long suratId);

    void approveSuratTugas(Long suratId, User approver);

    void rejectSuratTugas(Long suratId, String catatan, User approver);

    List<SuratTugas> getTugasUntukPegawai(User user);

    // New method for secretary to return surat for revision
    void returnSuratTugasForRevision(Long suratId, String catatan, User secretary);
}