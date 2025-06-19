package Audit.Auditing.repository;

import Audit.Auditing.model.StatusSuratTugas;
import Audit.Auditing.model.SuratTugas;
import Audit.Auditing.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.domain.Sort;

@Repository
public interface SuratTugasRepository extends JpaRepository<SuratTugas, Long> {
        List<SuratTugas> findByStatus(StatusSuratTugas status, Sort sort);

          @Query("SELECT DISTINCT st FROM SuratTugas st LEFT JOIN st.anggotaTim a WHERE (st.ketuaTim = :user OR a = :user) AND st.status = :status ORDER BY st.tanggalMulaiAudit DESC")
        List<SuratTugas> findTugasForUserByStatus(@Param("user") User user, @Param("status") StatusSuratTugas status);

}