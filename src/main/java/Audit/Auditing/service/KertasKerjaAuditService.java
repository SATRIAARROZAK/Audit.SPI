package Audit.Auditing.service;

import Audit.Auditing.dto.KertasKerjaAuditDto;
import Audit.Auditing.model.KertasKerjaAudit;
import Audit.Auditing.model.User;

import java.util.List;

public interface KertasKerjaAuditService {
    // Metode save yang sudah ada sebelumnya
    KertasKerjaAudit save(KertasKerjaAuditDto dto, User user);

    // Metode baru untuk menyimpan data dari form dinamis
    void saveDynamic(KertasKerjaAuditDto dto, User user);
    
    List<KertasKerjaAudit> getBySuratTugasId(Long suratTugasId);
    
    List<KertasKerjaAudit> getAll();
}