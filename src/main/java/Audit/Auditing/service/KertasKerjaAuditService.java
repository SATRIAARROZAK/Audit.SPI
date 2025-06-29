package Audit.Auditing.service;

import Audit.Auditing.dto.KertasKerjaAuditDto;
import Audit.Auditing.model.KertasKerjaAudit;
import Audit.Auditing.model.User;

import java.util.List;

public interface KertasKerjaAuditService {
    KertasKerjaAudit save(KertasKerjaAuditDto dto, User user);
    List<KertasKerjaAudit> getBySuratTugasId(Long suratTugasId);
    List<KertasKerjaAudit> getAll();
}