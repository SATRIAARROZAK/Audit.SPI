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
import java.util.UUID; // Import UUID

@Service
public class KertasKerjaAuditServiceImpl implements KertasKerjaAuditService {

    @Autowired
    private KertasKerjaAuditRepository kertasKerjaAuditRepository;

    @Autowired
    private SuratTugasRepository suratTugasRepository;

    @Autowired
    private FileStorageService fileStorageService;

    // Metode save lama, bisa Anda hapus atau biarkan jika masih terpakai di tempat
    // lain
    @Override
    @Transactional
    public KertasKerjaAudit save(KertasKerjaAuditDto dto, User user) {
        // ... (Implementasi lama)
        return null; // Sesuaikan jika metode ini masih dipakai
    }

    @Override
    @Transactional
    public void saveDynamic(KertasKerjaAuditDto dto, User user) {
        SuratTugas suratTugas = suratTugasRepository.findById(dto.getSuratTugasId())
                .orElseThrow(() -> new EntityNotFoundException("Surat Tugas tidak ditemukan"));

        // Simpan file sekali saja
        String dokumenPath = null;
        if (dto.getDokumen() != null && !dto.getDokumen().isEmpty()) {
            dokumenPath = fileStorageService.storeFile(dto.getDokumen());
        }
        
        // PINDAHKAN PEMBUATAN UUID KE SINI
        UUID groupIdentifier = UUID.randomUUID(); 

        // Loop melalui setiap prosedur yang di-submit
        for (int i = 0; i < dto.getProsedur().size(); i++) {
            String prosedurText = dto.getProsedur().get(i);

            // Loop melalui setiap tahapan untuk menemukan yang sesuai
            boolean hasTahapan = false;
            for (int j = 0; j < dto.getTahapan().size(); j++) {
                // Cek apakah tahapan ini milik prosedur saat ini
                if (dto.getProsedurIndex().get(j) == i) {
                    hasTahapan = true;
                    String tahapanText = dto.getTahapan().get(j);

                    KertasKerjaAudit kka = new KertasKerjaAudit();
                    kka.setSuratTugas(suratTugas);
                    kka.setProsedurGroup(groupIdentifier); // Gunakan UUID yang sama
                    kka.setProsedur(prosedurText);
                    kka.setTahapan(tahapanText);
                    kka.setDilakukanOleh(user);
                    kka.setDokumenPath(dokumenPath);
                    kertasKerjaAuditRepository.save(kka);
                }
            }

            // Jika prosedur tidak punya tahapan sama sekali
            if (!hasTahapan) {
                KertasKerjaAudit kka = new KertasKerjaAudit();
                kka.setSuratTugas(suratTugas);
                kka.setProsedurGroup(groupIdentifier); // Gunakan UUID yang sama
                kka.setProsedur(prosedurText);
                kka.setTahapan(null); // Tahapan kosong
                kka.setDilakukanOleh(user);
                kka.setDokumenPath(dokumenPath);
                kertasKerjaAuditRepository.save(kka);
            }
        }
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