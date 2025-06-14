package Audit.Auditing.repository;

import Audit.Auditing.model.StatusSuratTugas;
import Audit.Auditing.model.SuratTugas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.domain.Sort;

@Repository
public interface SuratTugasRepository extends JpaRepository<SuratTugas, Long> {
        List<SuratTugas> findByStatus(StatusSuratTugas status, Sort sort);

}