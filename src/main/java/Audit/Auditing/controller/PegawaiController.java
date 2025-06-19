package Audit.Auditing.controller;

import Audit.Auditing.config.CustomUserDetails;
import Audit.Auditing.model.StatusSuratTugas;
import Audit.Auditing.model.SuratTugas;
import Audit.Auditing.model.User;
import Audit.Auditing.service.SuratTugasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/pegawai/surat-tugas")
public class PegawaiController {

    @Autowired
    private SuratTugasService suratTugasService;

    @GetMapping("/list")
    public String listSuratTugasPegawai(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User currentUser = userDetails.getUser();
        List<SuratTugas> listSurat = suratTugasService.getTugasUntukPegawai(currentUser);
        model.addAttribute("listSurat", listSurat);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("pageTitle", "Daftar Surat Tugas Saya");
        return "pegawai/list-surat-tugas";
    }

    @GetMapping("/view/{id}")
    public String viewSuratTugas(@PathVariable("id") Long id, Model model,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes ra) {
        Optional<SuratTugas> suratOpt = suratTugasService.getSuratTugasById(id);
        User currentUser = userDetails.getUser();

        if (suratOpt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Surat tugas tidak ditemukan.");
            return "redirect:/pegawai/surat-tugas/list";
        }

        SuratTugas surat = suratOpt.get();
        // Verifikasi bahwa user yang login adalah bagian dari surat tugas ini
        boolean isKetua = surat.getKetuaTim().getId().equals(currentUser.getId());
        boolean isAnggota = surat.getAnggotaTim().stream().anyMatch(anggota -> anggota.getId().equals(currentUser.getId()));

        if (!isKetua && !isAnggota) {
            ra.addFlashAttribute("errorMessage", "Anda tidak memiliki akses ke surat tugas ini.");
            return "redirect:/pegawai/surat-tugas/list";
        }
        
        String filePath = surat.getFilePath();
        boolean isPdf = filePath != null && filePath.toLowerCase().endsWith(".pdf");

        model.addAttribute("surat", surat);
        model.addAttribute("fileUrl", "/profile-photos/" + filePath);
        model.addAttribute("isPdf", isPdf);
        model.addAttribute("pageTitle", "Detail Surat Tugas");

        return "pegawai/view-surat-tugas";
    }
}