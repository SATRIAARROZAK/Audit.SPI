package Audit.Auditing.controller.admin;

import Audit.Auditing.dto.SuratTugasDTO;
import Audit.Auditing.model.Role;
import Audit.Auditing.model.SuratTugas;
import Audit.Auditing.model.SuratTugasHistory;
import Audit.Auditing.model.User;
import Audit.Auditing.model.JenisAudit;
import Audit.Auditing.model.StatusSuratTugas;
import Audit.Auditing.repository.UserRepository;
import Audit.Auditing.repository.SuratTugasHistoryRepository;
import Audit.Auditing.service.SuratTugasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page; // Import Page
import org.springframework.data.domain.PageRequest; // Import PageRequest
import org.springframework.data.domain.Pageable; // Import Pageable
import org.springframework.data.domain.Sort; // Import Sort
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; // Import RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Controller
@RequestMapping("/admin/surat-tugas")
public class SuratTugasController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SuratTugasService suratTugasService;

    @Autowired
    private SuratTugasHistoryRepository suratTugasHistoryRepository;

    @GetMapping("/list")
    public String listSuratTugas(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size, // Max 5 rows per page
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<SuratTugas> suratTugasPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            suratTugasPage = suratTugasService.searchSuratTugas(keyword.trim(), pageable);
        } else {
            suratTugasPage = suratTugasService.getAllSuratTugas(pageable);
        }

        model.addAttribute("listSurat", suratTugasPage.getContent());
        model.addAttribute("currentPage", suratTugasPage.getNumber());
        model.addAttribute("totalPages", suratTugasPage.getTotalPages());
        model.addAttribute("totalItems", suratTugasPage.getTotalElements());
        model.addAttribute("pageSize", suratTugasPage.getSize());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("keyword", keyword);
        model.addAttribute("pageTitle", "Daftar Surat Tugas");
        model.addAttribute("T(Audit.Auditing.model.StatusSuratTugas)", StatusSuratTugas.class);
        return "admin/list-surat-tugas";
    }

    // ... (other methods remain largely the same, but you might want to similarly update
    //     KepalaSpiController and PegawaiController for pagination and search)

    // Menampilkan form upload
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        List<User> pegawai = userRepository.findByRole(Role.pegawai);

        model.addAttribute("suratTugasDTO", new SuratTugasDTO());
        model.addAttribute("pegawai", pegawai);
        model.addAttribute("jenisAuditOptions", JenisAudit.values()); // Pass enum values to view
        model.addAttribute("currentDate", LocalDate.now()); // For setting default date
        return "admin/form-surat-tugas";
    }

    // Memproses data dari form
    @PostMapping("/save")
    public String createSuratTugas(@Validated(SuratTugasDTO.Create.class) @ModelAttribute("suratTugasDTO") SuratTugasDTO suratTugasDTO,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (suratTugasDTO.getAnggotaTimIds().contains(suratTugasDTO.getKetuaTimId())) {
            result.rejectValue("anggotaTimIds", "error.anggotaTimIds",
                    "Ketua tim tidak boleh menjadi anggota tim juga.");
        }

        // Add date validation
        if (suratTugasDTO.getTanggalSelesaiAudit() != null && suratTugasDTO.getTanggalMulaiAudit() != null) {
            if (suratTugasDTO.getTanggalSelesaiAudit().isBefore(suratTugasDTO.getTanggalMulaiAudit())) {
                result.rejectValue("tanggalSelesaiAudit", "date.invalid", "Tanggal selesai audit tidak boleh sebelum tanggal mulai.");
            }
        }


        if (result.hasErrors()) {
            List<User> pegawai = userRepository.findByRole(Role.pegawai);
            model.addAttribute("pegawai", pegawai);
            model.addAttribute("jenisAuditOptions", JenisAudit.values()); // Re-add for error case
            model.addAttribute("currentDate", LocalDate.now());
            return "admin/form-surat-tugas";
        }

        try {
            suratTugasService.createSuratTugas(suratTugasDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Surat tugas berhasil dibuat!");
        } catch (IllegalArgumentException e) { // Catch specific exception for duplicate nomorSurat
            result.rejectValue("nomorSurat", "nomorSurat.exists", e.getMessage());
            List<User> pegawai = userRepository.findByRole(Role.pegawai);
            model.addAttribute("pegawai", pegawai);
            model.addAttribute("jenisAuditOptions", JenisAudit.values()); // Re-add for error case
            model.addAttribute("currentDate", LocalDate.now());
            return "admin/form-surat-tugas";
        }
        catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal membuat surat tugas: " + e.getMessage());
        }

        return "redirect:/admin/surat-tugas/list";
    }

    // GET - Tampilkan halaman detail
    @GetMapping("/view/{id}")
    public String viewSuratTugas(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        Optional<SuratTugas> suratOpt = suratTugasService.getSuratTugasById(id);
        if (suratOpt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Surat tugas tidak ditemukan.");
            return "redirect:/admin/surat-tugas/list";
        }
        SuratTugas surat = suratOpt.get();
        List<SuratTugasHistory> history = suratTugasHistoryRepository.findBySuratTugasIdOrderByTimestampAsc(id);
        String filePath = surat.getFilePath();

        boolean isPdf = filePath != null && filePath.toLowerCase().endsWith(".pdf");
        
        model.addAttribute("surat", surat);
        model.addAttribute("history", history);
        model.addAttribute("fileUrl", "/profile-photos/" + filePath);
        model.addAttribute("isPdf", isPdf);
        model.addAttribute("T(Audit.Auditing.model.StatusSuratTugas)", StatusSuratTugas.class); // Pass Status enum
        
        return "admin/view-surat-tugas";
    }

    // GET - Tampilkan form 'Edit'
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        Optional<SuratTugas> suratOpt = suratTugasService.getSuratTugasById(id);
        if (suratOpt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Surat tugas tidak ditemukan.");
            return "redirect:/admin/surat-tugas/list";
        }

        SuratTugas surat = suratOpt.get();
        // Konversi dari Entity ke DTO untuk form
        SuratTugasDTO dto = new SuratTugasDTO();
        dto.setNomorSurat(surat.getNomorSurat()); // New field
        dto.setDeskripsiSurat(surat.getDeskripsiSurat()); // Renamed
        dto.setJenisAudit(surat.getJenisAudit()); // New field
        dto.setTanggalMulaiAudit(surat.getTanggalMulaiAudit());
        dto.setTanggalSelesaiAudit(surat.getTanggalSelesaiAudit());
        dto.setKetuaTimId(surat.getKetuaTim().getId());
        dto.setAnggotaTimIds(surat.getAnggotaTim().stream().map(User::getId).collect(Collectors.toList()));

        List<User> pegawai = userRepository.findByRole(Role.pegawai);

        model.addAttribute("suratTugasDTO", dto);
        model.addAttribute("pegawai", pegawai);
        model.addAttribute("suratId", id);
        model.addAttribute("jenisAuditOptions", JenisAudit.values()); // Pass enum values to view
        model.addAttribute("currentDate", LocalDate.now()); // For setting default date
        model.addAttribute("surat", surat); // Pass surat object for status/notes display
        model.addAttribute("T(Audit.Auditing.model.StatusSuratTugas)", StatusSuratTugas.class); // Pass Status enum
        return "admin/form-surat-tugas-edit";
    }

    // POST - Proses 'Update'
    @PostMapping("/update/{id}")
    public String updateSuratTugas(@PathVariable("id") Long id,
            @Validated(SuratTugasDTO.Update.class) @ModelAttribute("suratTugasDTO") SuratTugasDTO suratTugasDTO,
            BindingResult result, Model model, RedirectAttributes ra) {

        if (suratTugasDTO.getAnggotaTimIds() != null
                && suratTugasDTO.getAnggotaTimIds().contains(suratTugasDTO.getKetuaTimId())) {
            result.rejectValue("anggotaTimIds", "error.anggotaTimIds",
                    "Ketua tim tidak boleh menjadi anggota tim juga.");
        }

        // Add date validation
        if (suratTugasDTO.getTanggalSelesaiAudit() != null && suratTugasDTO.getTanggalMulaiAudit() != null) {
            if (suratTugasDTO.getTanggalSelesaiAudit().isBefore(suratTugasDTO.getTanggalMulaiAudit())) {
                result.rejectValue("tanggalSelesaiAudit", "date.invalid", "Tanggal selesai audit tidak boleh sebelum tanggal mulai.");
            }
        }

        if (result.hasErrors()) {
            List<User> pegawai = userRepository.findByRole(Role.pegawai);
            model.addAttribute("pegawai", pegawai);
            model.addAttribute("suratId", id);
            model.addAttribute("jenisAuditOptions", JenisAudit.values()); // Re-add for error case
            model.addAttribute("currentDate", LocalDate.now());
            // Re-fetch surat object for status/notes display
            suratTugasService.getSuratTugasById(id).ifPresent(s -> model.addAttribute("surat", s));
            model.addAttribute("T(Audit.Auditing.model.StatusSuratTugas)", StatusSuratTugas.class); // Pass Status enum
            return "admin/form-surat-tugas-edit";
        }

        try {
            suratTugasService.updateSuratTugas(id, suratTugasDTO);
            ra.addFlashAttribute("successMessage", "Surat tugas berhasil diupdate!");
        } catch (IllegalArgumentException e) { // Catch specific exception for duplicate nomorSurat
            result.rejectValue("nomorSurat", "nomorSurat.exists", e.getMessage());
            List<User> pegawai = userRepository.findByRole(Role.pegawai);
            model.addAttribute("pegawai", pegawai);
            model.addAttribute("suratId", id);
            model.addAttribute("jenisAuditOptions", JenisAudit.values()); // Re-add for error case
            model.addAttribute("currentDate", LocalDate.now());
            // Re-fetch surat object for status/notes display
            suratTugasService.getSuratTugasById(id).ifPresent(s -> model.addAttribute("surat", s));
            model.addAttribute("T(Audit.Auditing.model.StatusSuratTugas)", StatusSuratTugas.class); // Pass Status enum
            return "admin/form-surat-tugas-edit";
        }
        catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Gagal mengupdate surat tugas: " + e.getMessage());
        }
        return "redirect:/admin/surat-tugas/list";
    }

    // GET - Proses 'Delete'
    @GetMapping("/delete/{id}")
    public String deleteSuratTugas(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            suratTugasService.deleteSuratTugas(id);
            ra.addFlashAttribute("successMessage", "Surat tugas berhasil dihapus.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Gagal menghapus surat tugas: " + e.getMessage());
        }
        return "redirect:/admin/surat-tugas/list";
    }
}