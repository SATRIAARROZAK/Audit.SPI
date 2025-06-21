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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
        // Check for duplicate nomorSurat
        if (suratTugasRepository.findByNomorSurat(dto.getNomorSurat()).isPresent()) {
            throw new IllegalArgumentException("Nomor surat '" + dto.getNomorSurat() + "' sudah ada.");
        }

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
        suratTugas.setNomorSurat(dto.getNomorSurat());
        suratTugas.setDeskripsiSurat(dto.getDeskripsiSurat());
        suratTugas.setJenisAudit(dto.getJenisAudit());
        suratTugas.setFilePath(fileName);
        suratTugas.setKetuaTim(ketuaTim);
        suratTugas.setAnggotaTim(anggotaTim);
        suratTugas.setTanggalMulaiAudit(dto.getTanggalMulaiAudit());
        suratTugas.setTanggalSelesaiAudit(dto.getTanggalSelesaiAudit());
        suratTugas.setStatus(StatusSuratTugas.BARU);
        suratTugasRepository.save(suratTugas);

        List<User> sekretarisUsers = userRepository.findByRole(Role.sekretaris);
        for (User sekretaris : sekretarisUsers) {
            String message = "Surat tugas baru '" + StringUtils.abbreviate(suratTugas.getNomorSurat(), 20)
                    + "' perlu direview.";
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
    public Page<SuratTugas> getAllSuratTugas(Pageable pageable) {
        return suratTugasRepository.findAll(pageable);
    }

    @Override
    public Page<SuratTugas> searchSuratTugas(String keyword, Pageable pageable) {
        if (StringUtils.isBlank(keyword)) {
            return suratTugasRepository.findAll(pageable);
        }
        return suratTugasRepository.searchSuratTugas(keyword, pageable);
    }

    @Override
    public List<SuratTugas> getSuratByStatus(StatusSuratTugas status) {
        if (status == StatusSuratTugas.BARU) {
            return suratTugasRepository.findByStatusIn(
                    List.of(StatusSuratTugas.BARU, StatusSuratTugas.DIKEMBALIKAN_KE_ADMIN),
                    Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        return suratTugasRepository.findByStatus(status, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    // Updated method to return Page<SuratTugas> for status-based retrieval
    @Override
    public Page<SuratTugas> getSuratByStatus(StatusSuratTugas status, Pageable pageable) {
        if (status == StatusSuratTugas.BARU) {
            return suratTugasRepository.findByStatusIn(
                    List.of(StatusSuratTugas.BARU, StatusSuratTugas.DIKEMBALIKAN_KE_ADMIN),
                    pageable);
        }
        return suratTugasRepository.findByStatus(status, pageable);
    }

    @Override
    @Transactional
    public void updateSuratTugas(Long id, SuratTugasDTO dto) {
        SuratTugas suratTugas = suratTugasRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Surat Tugas dengan ID " + id + " tidak ditemukan."));

        Optional<SuratTugas> existingByNomorSurat = suratTugasRepository.findByNomorSurat(dto.getNomorSurat());
        if (existingByNomorSurat.isPresent() && !existingByNomorSurat.get().getId().equals(id)) {
            throw new IllegalArgumentException(
                    "Nomor surat '" + dto.getNomorSurat() + "' sudah digunakan oleh surat tugas lain.");
        }

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
        } else {
            if (suratTugas.getFilePath() == null || suratTugas.getFilePath().isEmpty()) {
            }
        }

        User ketuaTim = userRepository.findById(dto.getKetuaTimId())
                .orElseThrow(() -> new EntityNotFoundException("Ketua Tim tidak ditemukan"));

        List<Long> anggotaIds = dto.getAnggotaTimIds() != null ? dto.getAnggotaTimIds() : Collections.emptyList();
        List<User> anggotaList = userRepository.findAllById(anggotaIds);
        Set<User> anggotaTim = new HashSet<>(anggotaList);

        suratTugas.setNomorSurat(dto.getNomorSurat());
        suratTugas.setDeskripsiSurat(dto.getDeskripsiSurat());
        suratTugas.setJenisAudit(dto.getJenisAudit());
        suratTugas.setKetuaTim(ketuaTim);
        suratTugas.setAnggotaTim(anggotaTim);
        suratTugas.setTanggalMulaiAudit(dto.getTanggalMulaiAudit());
        suratTugas.setTanggalSelesaiAudit(dto.getTanggalSelesaiAudit());

        StatusSuratTugas oldStatus = suratTugas.getStatus();

        if (oldStatus == StatusSuratTugas.DIKEMBALIKAN_KE_ADMIN) {
            suratTugas.setStatus(StatusSuratTugas.BARU);
        }

        suratTugasRepository.save(suratTugas);

        if (oldStatus == StatusSuratTugas.DIKEMBALIKAN_KE_ADMIN) {
            List<User> sekretarisUsers = userRepository.findByRole(Role.sekretaris);
            String message = "Surat Tugas '" + StringUtils.abbreviate(suratTugas.getNomorSurat(), 20)
                    + "' telah direvisi oleh Admin. Silakan review kembali.";
            for (User sekretaris : sekretarisUsers) {
                notificationService.createNotification(sekretaris, message,
                        "/sekretaris/surat-tugas/review/" + suratTugas.getId());
            }
        }

        SuratTugasHistory history = new SuratTugasHistory(suratTugas, suratTugas.getStatus(), null,
                "Surat tugas diperbarui oleh Admin.");
        suratTugasHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public void reviewSuratTugas(Long suratId) {
        SuratTugas suratTugas = suratTugasRepository.findById(suratId)
                .orElseThrow(
                        () -> new EntityNotFoundException("Surat Tugas dengan ID " + suratId + " tidak ditemukan."));

        if (suratTugas.getStatus() != StatusSuratTugas.BARU
                && suratTugas.getStatus() != StatusSuratTugas.DIKEMBALIKAN_KE_ADMIN) {
            throw new IllegalStateException(
                    "Hanya surat dengan status 'Baru Dibuat Admin' atau 'Dikembalikan ke Admin untuk Revisi' yang dapat direview.");
        }

        suratTugas.setStatus(StatusSuratTugas.REVIEW_SEKRETARIS);

        suratTugasRepository.save(suratTugas);
        List<User> kepalaSpiUsers = userRepository.findByRole(Role.kepalaspi);
        for (User kepalaSpi : kepalaSpiUsers) {
            String message = "Surat tugas '" + StringUtils.abbreviate(suratTugas.getNomorSurat(), 20)
                    + "' menunggu persetujuan.";
            notificationService.createNotification(kepalaSpi, message,
                    "/kepalaspi/surat-tugas/view/" + suratTugas.getId());
        }

        SuratTugasHistory history = new SuratTugasHistory(suratTugas, StatusSuratTugas.REVIEW_SEKRETARIS, null,
                "Direview oleh Sekretaris dan diteruskan ke Kepala SPI.");
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
        String message = "Surat Tugas '" + StringUtils.abbreviate(suratTugas.getNomorSurat(), 20)
                + "' telah DISETUJUI.";

        for (User admin : adminUsers) {
            notificationService.createNotification(admin, message, "/admin/surat-tugas/view/" + suratTugas.getId());
        }

        String linkPegawai = "/pegawai/surat-tugas/view/" + suratTugas.getId();
        notificationService.createNotification(suratTugas.getKetuaTim(), message, linkPegawai);
        for (User anggota : suratTugas.getAnggotaTim()) {
            notificationService.createNotification(anggota, message, linkPegawai);
        }

        SuratTugasHistory history = new SuratTugasHistory(suratTugas, StatusSuratTugas.DISETUJUI, approver,
                "Surat disetujui.");
        suratTugasHistoryRepository.save(history);
    }

    @Override
    public List<SuratTugas> getTugasUntukPegawai(User user) {
        return suratTugasRepository.findTugasForUserByStatus(user, StatusSuratTugas.DISETUJUI);
    }

    @Override
    public Page<SuratTugas> getTugasUntukPegawai(User user, Pageable pageable) {
        return suratTugasRepository.findTugasForUserByStatus(user, StatusSuratTugas.DISETUJUI, pageable); //
    }

    // Implementation for the new method to get tasks specifically for a Ketua Tim
    @Override
    public Page<SuratTugas> getTugasUntukKetuaTim(User ketuaTim, Pageable pageable, String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return suratTugasRepository.findByKetuaTimAndStatus(ketuaTim, StatusSuratTugas.DISETUJUI, pageable);
        } else {
            return suratTugasRepository.findByKetuaTimAndStatusAndKeyword(ketuaTim, StatusSuratTugas.DISETUJUI, keyword,
                    pageable);
        }

        // Di dalam class SuratTugasServiceImpl
        // @Override
        // public Page<SuratTugas> getTugasUntukKetuaTim(User ketuaTim, Pageable
        // pageable, String keyword) {
        // if (StringUtils.isBlank(keyword)) {
        // return suratTugasRepository.findTugasForKetuaTimByStatus(ketuaTim,
        // StatusSuratTugas.DISETUJUI, pageable);
        // } else {
        // return suratTugasRepository.searchTugasForKetuaTimByStatus(ketuaTim,
        // StatusSuratTugas.DISETUJUI, keyword,
        // pageable);
        // }
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
        String message = "Surat Tugas '" + StringUtils.abbreviate(suratTugas.getNomorSurat(), 20) + "' DITOLAK.";
        for (User admin : adminUsers) {
            notificationService.createNotification(admin, message, "/admin/surat-tugas/view/" + suratTugas.getId());
        }
        for (User sekretaris : sekretarisUsers) {
            notificationService.createNotification(sekretaris, message,
                    "/sekretaris/surat-tugas/review/" + suratTugas.getId());
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

    @Override
    @Transactional
    public void returnSuratTugasForRevision(Long suratId, String catatan, User secretary) {
        SuratTugas suratTugas = suratTugasRepository.findById(suratId)
                .orElseThrow(() -> new EntityNotFoundException("Surat Tugas tidak ditemukan."));

        if (suratTugas.getStatus() != StatusSuratTugas.BARU
                && suratTugas.getStatus() != StatusSuratTugas.REVIEW_SEKRETARIS) {
            throw new IllegalStateException("Surat tugas tidak dalam status yang bisa direvisi oleh Sekretaris.");
        }

        suratTugas.setStatus(StatusSuratTugas.DIKEMBALIKAN_KE_ADMIN);
        suratTugas.setCatatanPersetujuan(catatan);

        suratTugasRepository.save(suratTugas);

        List<User> adminUsers = userRepository.findByRole(Role.admin);
        String message = "Surat Tugas '" + StringUtils.abbreviate(suratTugas.getNomorSurat(), 20)
                + "' dikembalikan untuk revisi: " + StringUtils.abbreviate(catatan, 50);
        for (User admin : adminUsers) {
            notificationService.createNotification(admin, message, "/admin/surat-tugas/edit/" + suratTugas.getId());
        }

        SuratTugasHistory history = new SuratTugasHistory(suratTugas, StatusSuratTugas.DIKEMBALIKAN_KE_ADMIN, secretary,
                catatan);
        suratTugasHistoryRepository.save(history);
    }
}