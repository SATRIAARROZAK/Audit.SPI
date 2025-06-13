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
import java.nio.file.Files; // import ini
import java.nio.file.Paths; // import ini
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
        if (anggotaList.size() != dto.getAnggotaTimIds().size()){
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
        if (dto.getSuratFile() != null && !dto.getSuratFile().isEmpty()) {
            // Hapus file lama sebelum menyimpan yang baru
            try {
                Files.deleteIfExists(Paths.get(fileStorageService.getFileStorageLocation().toString(), suratTugas.getFilePath()));
            } catch (Exception e) {
                // Log error jika perlu, tapi jangan hentikan proses
                System.err.println("Gagal menghapus file lama: " + e.getMessage());
            }
            String newFileName = fileStorageService.storeFile(dto.getSuratFile());
            suratTugas.setFilePath(newFileName);
        }

        // Update field lainnya
        User ketuaTim = userRepository.findById(dto.getKetuaTimId())
                .orElseThrow(() -> new EntityNotFoundException("Ketua Tim tidak ditemukan"));
        Set<User> anggotaTim = new HashSet<>(userRepository.findAllById(dto.getAnggotaTimIds()));

        suratTugas.setTujuan(dto.getTujuan());
        suratTugas.setKetuaTim(ketuaTim);
        suratTugas.setAnggotaTim(anggotaTim);

        suratTugasRepository.save(suratTugas);
    }

    @Override
    @Transactional
    public void deleteSuratTugas(Long id) {
        SuratTugas suratTugas = suratTugasRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Surat Tugas dengan ID " + id + " tidak ditemukan."));
        
        // Hapus file dari storage
        try {
            Files.deleteIfExists(Paths.get(fileStorageService.getFileStorageLocation().toString(), suratTugas.getFilePath()));
        } catch (Exception e) {
            // Log error jika perlu
            System.err.println("Gagal menghapus file terkait: " + e.getMessage());
        }
        
        suratTugasRepository.deleteById(id);
    
    }
}