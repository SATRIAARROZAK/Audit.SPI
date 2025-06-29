package Audit.Auditing.service;

import Audit.Auditing.dto.KertasKerjaAuditDto;
import Audit.Auditing.model.KertasKerjaAudit;
import Audit.Auditing.model.SuratTugas;
import Audit.Auditing.model.User;
import Audit.Auditing.repository.KertasKerjaAuditRepository;
import Audit.Auditing.repository.SuratTugasRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class KertasKerjaAuditServiceImpl implements KertasKerjaAuditService {

    @Autowired
    private KertasKerjaAuditRepository kertasKerjaAuditRepository;

    @Autowired
    private SuratTugasRepository suratTugasRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Override
    @Transactional
    public KertasKerjaAudit save(KertasKerjaAuditDto dto, User user) {
        SuratTugas suratTugas = suratTugasRepository.findById(dto.getSuratTugasId())
                .orElseThrow(() -> new EntityNotFoundException("Surat Tugas tidak ditemukan"));

        KertasKerjaAudit kertasKerja = new KertasKerjaAudit();
        kertasKerja.setSuratTugas(suratTugas);
        kertasKerja.setProsedur(dto.getProsedur());
        kertasKerja.setTahapan(dto.getTahapan());
        kertasKerja.setDilakukanOleh(user);

        if (dto.getDokumen() != null && !dto.getDokumen().isEmpty()) {
            // Sebaiknya buat subdirektori baru untuk dokumen audit
            String fileName = fileStorageService.storeFile(dto.getDokumen());
            kertasKerja.setDokumenPath(fileName);
        }

        return kertasKerjaAuditRepository.save(kertasKerja);
    }

    @Override
    public List<KertasKerjaAudit> getBySuratTugasId(Long suratTugasId) {
        return kertasKerjaAuditRepository.findBySuratTugasId(suratTugasId);
    }
    
    @Override
    public List<KertasKerjaAudit> getAll() {
        return kertasKerjaAuditRepository.findAll();
    }
}