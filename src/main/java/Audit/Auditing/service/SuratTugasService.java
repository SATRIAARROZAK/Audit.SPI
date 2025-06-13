package Audit.Auditing.service;

import Audit.Auditing.dto.SuratTugasDTO;
import Audit.Auditing.model.SuratTugas; // Import ini

import java.util.List; // Import ini
import java.util.Optional; // Import ini

public interface SuratTugasService {
    void createSuratTugas(SuratTugasDTO suratTugasDTO);

    // --- TAMBAHKAN METODE-METODE DI BAWAH INI ---
    List<SuratTugas> getAllSuratTugas();

    Optional<SuratTugas> getSuratTugasById(Long id);

    void updateSuratTugas(Long id, SuratTugasDTO suratTugasDTO);

    void deleteSuratTugas(Long id);
}