package Audit.Auditing.repository;

import Audit.Auditing.model.KertasKerjaAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KertasKerjaAuditRepository extends JpaRepository<KertasKerjaAudit, Long> {
    List<KertasKerjaAudit> findBySuratTugasId(Long suratTugasId);
}