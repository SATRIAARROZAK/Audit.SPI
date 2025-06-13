package Audit.Auditing.controller.admin;

import Audit.Auditing.dto.SuratTugasDTO;
import Audit.Auditing.model.Role;
import Audit.Auditing.model.SuratTugas;
import Audit.Auditing.model.User;
import Audit.Auditing.repository.UserRepository;
import Audit.Auditing.service.SuratTugasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/surat-tugas")
public class SuratTugasController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SuratTugasService suratTugasService;

    @GetMapping("/list")
    public String listSuratTugas(Model model) {
        List<SuratTugas> listSurat = suratTugasService.getAllSuratTugas();
        model.addAttribute("listSurat", listSurat);
        return "admin/list-surat-tugas";
    }

    // Menampilkan form upload
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        // Ambil hanya user dengan role PEGAWAI
        List<User> pegawai = userRepository.findByRole(Role.pegawai);

        model.addAttribute("suratTugasDTO", new SuratTugasDTO());
        model.addAttribute("pegawai", pegawai);
        return "admin/form-surat-tugas";
    }

    // Memproses data dari form
    @PostMapping("/save")
    public String createSuratTugas(@Validated @ModelAttribute("suratTugasDTO") SuratTugasDTO suratTugasDTO,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (suratTugasDTO.getAnggotaTimIds().contains(suratTugasDTO.getKetuaTimId())) {
            result.rejectValue("anggotaTimIds", "error.anggotaTimIds",
                    "Ketua tim tidak boleh menjadi anggota tim juga.");
        }

        if (result.hasErrors()) {
            List<User> pegawai = userRepository.findByRole(Role.pegawai);
            model.addAttribute("pegawai", pegawai);
            return "admin/form-surat-tugas";
        }

        try {
            suratTugasService.createSuratTugas(suratTugasDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Surat tugas berhasil dibuat!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal membuat surat tugas: " + e.getMessage());
        }

        return "redirect:/admin/surat-tugas/list"; // Arahkan ke halaman list (akan dibuat nanti)
    }

    // GET - Tampilkan halaman detail
    @GetMapping("/view/{id}")
    public String viewSuratTugas(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        Optional<SuratTugas> suratOpt = suratTugasService.getSuratTugasById(id);
        if (suratOpt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Surat tugas tidak ditemukan.");
            return "redirect:/admin/surat-tugas/list";
        }
        model.addAttribute("surat", suratOpt.get());
        // Arahkan ke file yang ada di direktori upload. Pastikan WebConfig sudah
        // tersetting.
        model.addAttribute("fileUrl", "/profile-photos/" + suratOpt.get().getFilePath());
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
        dto.setTujuan(surat.getTujuan());
        dto.setKetuaTimId(surat.getKetuaTim().getId());
        dto.setAnggotaTimIds(surat.getAnggotaTim().stream().map(User::getId).collect(Collectors.toList()));

        List<User> pegawai = userRepository.findByRole(Role.pegawai);

        model.addAttribute("suratTugasDTO", dto);
        model.addAttribute("pegawai", pegawai);
        model.addAttribute("suratId", id);
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

        if (result.hasErrors()) {
            List<User> pegawai = userRepository.findByRole(Role.pegawai);
            model.addAttribute("pegawai", pegawai);
            model.addAttribute("suratId", id);
            return "admin/form-surat-tugas-edit";
        }

        try {
            suratTugasService.updateSuratTugas(id, suratTugasDTO);
            ra.addFlashAttribute("successMessage", "Surat tugas berhasil diupdate!");
        } catch (Exception e) {
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