package Audit.Auditing.repository;

import Audit.Auditing.model.SuratTugas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuratTugasRepository extends JpaRepository<SuratTugas, Long> {
}