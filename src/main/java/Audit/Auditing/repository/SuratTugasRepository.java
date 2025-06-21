package Audit.Auditing.repository;

import Audit.Auditing.model.StatusSuratTugas;
import Audit.Auditing.model.SuratTugas;
import Audit.Auditing.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;

@Repository
public interface SuratTugasRepository extends JpaRepository<SuratTugas, Long> {
        List<SuratTugas> findByStatus(StatusSuratTugas status, Sort sort);

        // Add method to find by multiple statuses
        List<SuratTugas> findByStatusIn(List<StatusSuratTugas> statuses, Sort sort);

        @Query("SELECT DISTINCT st FROM SuratTugas st LEFT JOIN st.anggotaTim a WHERE (st.ketuaTim = :user OR a = :user) AND st.status = :status ORDER BY st.tanggalMulaiAudit DESC")
        List<SuratTugas> findTugasForUserByStatus(@Param("user") User user, @Param("status") StatusSuratTugas status);

        // New method for finding tasks for a user by status with pagination
        @Query("SELECT DISTINCT st FROM SuratTugas st LEFT JOIN st.anggotaTim a WHERE (st.ketuaTim = :user OR a = :user) AND st.status = :status ORDER BY st.tanggalMulaiAudit DESC")
        Page<SuratTugas> findTugasForUserByStatus(@Param("user") User user, @Param("status") StatusSuratTugas status,
                        Pageable pageable); //

        @Query("SELECT st FROM SuratTugas st WHERE st.ketuaTim = :ketuaTim AND st.status = :status ORDER BY st.tanggalMulaiAudit DESC")
        Page<SuratTugas> findByKetuaTimAndStatus(@Param("ketuaTim") User ketuaTim,
                        @Param("status") StatusSuratTugas status, Pageable pageable);

        @Query("SELECT st FROM SuratTugas st WHERE st.ketuaTim = :ketuaTim AND st.status = :status AND " +
                        "(LOWER(st.nomorSurat) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(st.deskripsiSurat) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(st.jenisAudit) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(st.ketuaTim.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(st.ketuaTim.username) LIKE LOWER(CONCAT('%', :keyword, '%')))")
        Page<SuratTugas> findByKetuaTimAndStatusAndKeyword(@Param("ketuaTim") User ketuaTim,
                        @Param("status") StatusSuratTugas status, @Param("keyword") String keyword, Pageable pageable);

        // New method to find by NomorSurat for uniqueness check
        Optional<SuratTugas> findByNomorSurat(String nomorSurat);

        // Existing methods for general pagination and search
        Page<SuratTugas> findAll(Pageable pageable);

        @Query("SELECT st FROM SuratTugas st WHERE " +
                        "LOWER(st.nomorSurat) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(st.deskripsiSurat) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(st.jenisAudit) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(st.ketuaTim.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(st.ketuaTim.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
        Page<SuratTugas> searchSuratTugas(@Param("keyword") String keyword, Pageable pageable);

        // Added this for findByStatus with Pageable, assuming you'll use it in
        // SuratTugasServiceImpl
        Page<SuratTugas> findByStatus(StatusSuratTugas status, Pageable pageable);

        Page<SuratTugas> findByStatusIn(List<StatusSuratTugas> statuses, Pageable pageable);

        // Di dalam interface SuratTugasRepository
        @Query("SELECT st FROM SuratTugas st WHERE st.ketuaTim = :ketuaTim AND st.status = :status ORDER BY st.tanggalMulaiAudit DESC")
        Page<SuratTugas> findTugasForKetuaTimByStatus(@Param("ketuaTim") User ketuaTim,
                        @Param("status") StatusSuratTugas status, Pageable pageable);

        // Opsional: Untuk search khusus ketua tim juga (jika ingin menggabungkan search
        // dengan filter ketua tim)
        @Query("SELECT st FROM SuratTugas st WHERE st.ketuaTim = :ketuaTim AND st.status = :status AND " +
                        "(LOWER(st.nomorSurat) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(st.deskripsiSurat) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(st.jenisAudit) LIKE LOWER(CONCAT('%', :keyword, '%')) ) " +
                        "ORDER BY st.tanggalMulaiAudit DESC")
        Page<SuratTugas> searchTugasForKetuaTimByStatus(@Param("ketuaTim") User ketuaTim,
                        @Param("status") StatusSuratTugas status, @Param("keyword") String keyword, Pageable pageable);
}