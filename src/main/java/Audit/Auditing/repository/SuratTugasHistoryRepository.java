package Audit.Auditing.repository;

import Audit.Auditing.model.SuratTugasHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuratTugasHistoryRepository extends JpaRepository<SuratTugasHistory, Long> {
    List<SuratTugasHistory> findBySuratTugasIdOrderByTimestampAsc(Long suratTugasId);
}