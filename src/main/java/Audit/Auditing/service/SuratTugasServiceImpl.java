// Path: src/main/java/Audit/Auditing/service/SuratTugasServiceImpl.java
package Audit.Auditing.service;

import Audit.Auditing.dto.SuratTugasDTO;
import Audit.Auditing.model.StatusSuratTugas;
import Audit.Auditing.model.SuratTugas;
import Audit.Auditing.model.SuratTugasHistory;
import Audit.Auditing.model.User;
import Audit.Auditing.repository.SuratTugasRepository;
import Audit.Auditing.repository.UserRepository;
import Audit.Auditing.repository.SuratTugasHistoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.lang3.StringUtils;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import Audit.Auditing.model.Role;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Sort;

@Service
public class SuratTugasServiceImpl implements SuratTugasService {

    @Autowired
    private SuratTugasRepository suratTugasRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SuratTugasHistoryRepository suratTugasHistoryRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FileStorageService fileStorageService;

    @Override
    @Transactional
    public void createSuratTugas(SuratTugasDTO dto) {
        String fileName = fileStorageService.storeFile(dto.getSuratFile());
        if (fileName == null) {
            throw new RuntimeException("Gagal menyimpan file.");
        }

        User ketuaTim = userRepository.findById(dto.getKetuaTimId())
                .orElseThrow(() -> new EntityNotFoundException("Ketua Tim tidak ditemukan"));

        List<User> anggotaList = userRepository.findAllById(dto.getAnggotaTimIds());
        if (anggotaList.size() != dto.getAnggotaTimIds().size()) {
            throw new EntityNotFoundException("Satu atau lebih Anggota Tim tidak ditemukan");
        }
        Set<User> anggotaTim = new HashSet<>(anggotaList);

        SuratTugas suratTugas = new SuratTugas();
        suratTugas.setTujuan(dto.getTujuan());
        suratTugas.setFilePath(fileName);
        suratTugas.setKetuaTim(ketuaTim);
        suratTugas.setAnggotaTim(anggotaTim);
        suratTugas.setStatus(StatusSuratTugas.BARU);
        suratTugasRepository.save(suratTugas);

        List<User> sekretarisUsers = userRepository.findByRole(Role.sekretaris);
        for (User sekretaris : sekretarisUsers) {
            String message = "Surat tugas baru '" + StringUtils.abbreviate(suratTugas.getTujuan(), 20)
                    + "' perlu direview.";
            // DIUBAH: Kembali ke halaman review spesifik
            notificationService.createNotification(sekretaris, message,
                    "/sekretaris/surat-tugas/review/" + suratTugas.getId());
        }

        SuratTugasHistory history = new SuratTugasHistory(suratTugas, StatusSuratTugas.BARU, null,
                "Surat tugas dibuat oleh Admin.");
        suratTugasHistoryRepository.save(history);
    }

    @Override
    public Optional<SuratTugas> getSuratTugasById(Long id) {
        return suratTugasRepository.findById(id);
    }

    @Override
    public List<SuratTugas> getAllSuratTugas() {
        return suratTugasRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Override
    public List<SuratTugas> getSuratByStatus(StatusSuratTugas status) {
        return suratTugasRepository.findByStatus(status, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Override
    @Transactional
    public void updateSuratTugas(Long id, SuratTugasDTO dto) {
        SuratTugas suratTugas = suratTugasRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Surat Tugas dengan ID " + id + " tidak ditemukan."));

        if (dto.getSuratFile() != null && !dto.getSuratFile().isEmpty()) {
            if (StringUtils.isNotBlank(suratTugas.getFilePath())) {
                try {
                    Files.deleteIfExists(Paths.get(fileStorageService.getFileStorageLocation().toString(),
                            suratTugas.getFilePath()));
                } catch (Exception e) {
                    System.err.println("Gagal menghapus file lama: " + e.getMessage());
                }
            }
            String newFileName = fileStorageService.storeFile(dto.getSuratFile());
            suratTugas.setFilePath(newFileName);
        }

        User ketuaTim = userRepository.findById(dto.getKetuaTimId())
                .orElseThrow(() -> new EntityNotFoundException("Ketua Tim tidak ditemukan"));

        List<Long> anggotaIds = dto.getAnggotaTimIds() != null ? dto.getAnggotaTimIds() : Collections.emptyList();
        Set<User> anggotaTim = new HashSet<>(userRepository.findAllById(anggotaIds));

        suratTugas.setTujuan(dto.getTujuan());
        suratTugas.setKetuaTim(ketuaTim);
        suratTugas.setAnggotaTim(anggotaTim);

        suratTugasRepository.save(suratTugas);
    }

    @Override
    @Transactional
    public void reviewAndSetDates(Long suratId, LocalDate tanggalMulai, LocalDate tanggalSelesai) {
        SuratTugas suratTugas = suratTugasRepository.findById(suratId)
                .orElseThrow(
                        () -> new EntityNotFoundException("Surat Tugas dengan ID " + suratId + " tidak ditemukan."));

        if (suratTugas.getStatus() != StatusSuratTugas.BARU) {
            throw new IllegalStateException("Hanya surat dengan status 'BARU' yang dapat direview.");
        }

        suratTugas.setTanggalMulaiAudit(tanggalMulai);
        suratTugas.setTanggalSelesaiAudit(tanggalSelesai);
        suratTugas.setStatus(StatusSuratTugas.REVIEW_SEKRETARIS);

        suratTugasRepository.save(suratTugas);
        List<User> kepalaSpiUsers = userRepository.findByRole(Role.kepalaspi);
        for (User kepalaSpi : kepalaSpiUsers) {
            String message = "Surat tugas '" + StringUtils.abbreviate(suratTugas.getTujuan(), 20)
                    + "' menunggu persetujuan.";
            // DIUBAH: Kembali ke halaman persetujuan spesifik
            notificationService.createNotification(kepalaSpi, message,
                    "/kepalaspi/surat-tugas/view/" + suratTugas.getId());
        }

        SuratTugasHistory history = new SuratTugasHistory(suratTugas, StatusSuratTugas.REVIEW_SEKRETARIS, null,
                "Direview oleh Sekretaris dan jadwal telah ditetapkan.");
        suratTugasHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public void approveSuratTugas(Long suratId, User approver) {
        SuratTugas suratTugas = suratTugasRepository.findById(suratId)
                .orElseThrow(() -> new EntityNotFoundException("Surat Tugas tidak ditemukan."));

        if (suratTugas.getStatus() != StatusSuratTugas.REVIEW_SEKRETARIS) {
            throw new IllegalStateException("Hanya surat yang sudah direview yang bisa disetujui.");
        }

        suratTugas.setStatus(StatusSuratTugas.DISETUJUI);
        suratTugas.setApprover(approver);
        suratTugas.setApprovalDate(LocalDateTime.now());
        suratTugas.setCatatanPersetujuan("Disetujui.");

        suratTugasRepository.save(suratTugas);
        List<User> adminUsers = userRepository.findByRole(Role.admin);
        String message = "Surat Tugas '" + StringUtils.abbreviate(suratTugas.getTujuan(), 20) + "' telah DISETUJUI.";
        for (User admin : adminUsers) {
            // DIUBAH: Kembali ke halaman detail untuk admin
            notificationService.createNotification(admin, message, "/admin/surat-tugas/view/" + suratTugas.getId());
        }
        // Tidak diubah: Pegawai tetap ke dashboard karena tidak punya halaman detail
        notificationService.createNotification(suratTugas.getKetuaTim(), message,
                "/dashboard");

        SuratTugasHistory history = new SuratTugasHistory(suratTugas, StatusSuratTugas.DISETUJUI, approver,
                "Surat disetujui.");
        suratTugasHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public void rejectSuratTugas(Long suratId, String catatan, User approver) {
        SuratTugas suratTugas = suratTugasRepository.findById(suratId)
                .orElseThrow(() -> new EntityNotFoundException("Surat Tugas tidak ditemukan."));

        if (suratTugas.getStatus() != StatusSuratTugas.REVIEW_SEKRETARIS) {
            throw new IllegalStateException("Hanya surat yang sudah direview yang bisa ditolak.");
        }

        suratTugas.setStatus(StatusSuratTugas.DITOLAK);
        suratTugas.setApprover(approver);
        suratTugas.setApprovalDate(LocalDateTime.now());
        suratTugas.setCatatanPersetujuan(catatan);

        suratTugasRepository.save(suratTugas);
        List<User> adminUsers = userRepository.findByRole(Role.admin);
        List<User> sekretarisUsers = userRepository.findByRole(Role.sekretaris);
        String message = "Surat Tugas '" + StringUtils.abbreviate(suratTugas.getTujuan(), 20) + "' DITOLAK.";
        for (User admin : adminUsers) {
            // DIUBAH: Kembali ke halaman detail untuk admin
            notificationService.createNotification(admin, message, "/admin/surat-tugas/view/" + suratTugas.getId());
        }
        for (User sekretaris : sekretarisUsers) {
            // DIUBAH: Kembali ke halaman detail (meskipun mungkin perlu penyesuaian hak akses)
            notificationService.createNotification(sekretaris, message,
                    "/admin/surat-tugas/view/" + suratTugas.getId());
        }

        SuratTugasHistory history = new SuratTugasHistory(suratTugas, StatusSuratTugas.DITOLAK, approver, catatan);
        suratTugasHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public void deleteSuratTugas(Long id) {
        SuratTugas suratTugas = suratTugasRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Surat Tugas dengan ID " + id + " tidak ditemukan."));

        suratTugas.getAnggotaTim().clear();
        suratTugasRepository.save(suratTugas);

        try {
            Files.deleteIfExists(
                    Paths.get(fileStorageService.getFileStorageLocation().toString(), suratTugas.getFilePath()));
        } catch (Exception e) {
            System.err.println("Gagal menghapus file terkait: " + e.getMessage());
        }

        suratTugasRepository.deleteById(id);
    }
}