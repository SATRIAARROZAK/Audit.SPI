package Audit.Auditing.service;

import Audit.Auditing.dto.SuratTugasDTO;
import Audit.Auditing.model.StatusSuratTugas;
import Audit.Auditing.model.SuratTugas;
import Audit.Auditing.model.User;
import Audit.Auditing.repository.SuratTugasRepository;
import Audit.Auditing.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.nio.file.Files; // import ini
import java.nio.file.Paths; // import ini
import java.time.LocalDate;
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
    private FileStorageService fileStorageService;

    

    @Override
    @Transactional
    public void createSuratTugas(SuratTugasDTO dto) {
        // 1. Simpan file yang di-upload
        String fileName = fileStorageService.storeFile(dto.getSuratFile());
        if (fileName == null) {
            throw new RuntimeException("Gagal menyimpan file.");
        }

        // 2. Cari user untuk ketua dan anggota tim
        User ketuaTim = userRepository.findById(dto.getKetuaTimId())
                .orElseThrow(() -> new EntityNotFoundException("Ketua Tim tidak ditemukan"));

        List<User> anggotaList = userRepository.findAllById(dto.getAnggotaTimIds());
        if (anggotaList.size() != dto.getAnggotaTimIds().size()) {
            throw new EntityNotFoundException("Satu atau lebih Anggota Tim tidak ditemukan");
        }
        Set<User> anggotaTim = new HashSet<>(anggotaList);

        // 3. Buat dan simpan entitas SuratTugas
        SuratTugas suratTugas = new SuratTugas();
        suratTugas.setTujuan(dto.getTujuan());
        suratTugas.setFilePath(fileName);
        suratTugas.setKetuaTim(ketuaTim);
        suratTugas.setAnggotaTim(anggotaTim);
        suratTugas.setStatus(StatusSuratTugas.BARU); // Status awal
        suratTugasRepository.save(suratTugas);
    }

    @Override
    public List<SuratTugas> getAllSuratTugas() {
        // Mengurutkan berdasarkan tanggal dibuat, yang terbaru di atas
        return suratTugasRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Override
    public Optional<SuratTugas> getSuratTugasById(Long id) {
        return suratTugasRepository.findById(id);
    }

    @Override
    @Transactional
    public void updateSuratTugas(Long id, SuratTugasDTO dto) {
        SuratTugas suratTugas = suratTugasRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Surat Tugas dengan ID " + id + " tidak ditemukan."));

        // Handle file update: hanya update jika file baru di-upload
         // PERBAIKAN 1: Logika update file yang lebih aman
        if (dto.getSuratFile() != null && !dto.getSuratFile().isEmpty()) {
            // Hanya hapus file lama jika path-nya ada (tidak null atau kosong)
            if (StringUtils.hasText(suratTugas.getFilePath())) {
                try {
                    Files.deleteIfExists(Paths.get(fileStorageService.getFileStorageLocation().toString(), suratTugas.getFilePath()));
                } catch (Exception e) {
                    System.err.println("Gagal menghapus file lama: " + e.getMessage());
                }
            }
            String newFileName = fileStorageService.storeFile(dto.getSuratFile());
            suratTugas.setFilePath(newFileName);
        }

        // PERBAIKAN 2: Logika update relasi yang lebih aman
        User ketuaTim = userRepository.findById(dto.getKetuaTimId())
                .orElseThrow(() -> new EntityNotFoundException("Ketua Tim tidak ditemukan"));

        // Cek jika daftar ID anggota tidak null sebelum digunakan
        List<Long> anggotaIds = dto.getAnggotaTimIds() != null ? dto.getAnggotaTimIds() : Collections.emptyList();
        Set<User> anggotaTim = new HashSet<>(userRepository.findAllById(anggotaIds));

        // Update field lainnya
        suratTugas.setTujuan(dto.getTujuan());
        suratTugas.setKetuaTim(ketuaTim);
        suratTugas.setAnggotaTim(anggotaTim); // Menggunakan set yang sudah aman (bisa jadi kosong, tapi tidak null)

        suratTugasRepository.save(suratTugas);

    }

    @Override
    public List<SuratTugas> getSuratByStatus(StatusSuratTugas status) {
        return suratTugasRepository.findByStatus(status, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Override
    @Transactional
    public void reviewAndSetDates(Long suratId, LocalDate tanggalMulai, LocalDate tanggalSelesai) {
        SuratTugas suratTugas = suratTugasRepository.findById(suratId)
                .orElseThrow(() -> new EntityNotFoundException("Surat Tugas dengan ID " + suratId + " tidak ditemukan."));

        // Validasi status sebelum diubah
        if (suratTugas.getStatus() != StatusSuratTugas.BARU) {
            throw new IllegalStateException("Hanya surat dengan status 'BARU' yang dapat direview.");
        }
        
        suratTugas.setTanggalMulaiAudit(tanggalMulai);
        suratTugas.setTanggalSelesaiAudit(tanggalSelesai);
        suratTugas.setStatus(StatusSuratTugas.REVIEW_SEKRETARIS); // Status diubah

        suratTugasRepository.save(suratTugas);
    }

    @Override
    @Transactional
    public void deleteSuratTugas(Long id) {
        SuratTugas suratTugas = suratTugasRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Surat Tugas dengan ID " + id + " tidak ditemukan."));

        // --- LOGIKA BARU ---
        // 1. Putuskan hubungan Many-to-Many dengan membersihkan list anggota
        suratTugas.getAnggotaTim().clear();
        suratTugasRepository.save(suratTugas); // Simpan perubahan untuk menghapus relasi di join table

        // 2. Hapus file dari storage
        try {
            // Pastikan Anda memiliki metode getFileStorageLocation() di FileStorageService
            Files.deleteIfExists(
                    Paths.get(fileStorageService.getFileStorageLocation().toString(), suratTugas.getFilePath()));
        } catch (Exception e) {
            System.err.println("Gagal menghapus file terkait: " + e.getMessage());
            // Sebaiknya log error ini, tapi jangan hentikan proses penghapusan data
        }

        // 3. Hapus entitas utama setelah relasi dibersihkan
        suratTugasRepository.deleteById(id);
    }
}