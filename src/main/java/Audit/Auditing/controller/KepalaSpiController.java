package Audit.Auditing.controller;

import Audit.Auditing.config.CustomUserDetails;
import Audit.Auditing.model.StatusSuratTugas;
import Audit.Auditing.model.SuratTugas;
import Audit.Auditing.service.SuratTugasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/kepalaspi/surat-tugas")
public class KepalaSpiController {

    @Autowired
    private SuratTugasService suratTugasService;

    @GetMapping("/list")
    public String listSuratUntukDisetujui(Model model) {
        List<SuratTugas> listSurat = suratTugasService.getSuratByStatus(StatusSuratTugas.REVIEW_SEKRETARIS);
        model.addAttribute("listSurat", listSurat);
        model.addAttribute("pageTitle", "Daftar Surat untuk Persetujuan");
        return "kepalaspi/list-surat-tugas";
    }

    @GetMapping("/view/{id}")
    public String showApprovalForm(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        Optional<SuratTugas> suratOpt = suratTugasService.getSuratTugasById(id);
        if (suratOpt.isEmpty() || suratOpt.get().getStatus() != StatusSuratTugas.REVIEW_SEKRETARIS) {
            ra.addFlashAttribute("errorMessage", "Surat tugas tidak valid atau tidak dalam status yang benar untuk persetujuan.");
            return "redirect:/kepalaspi/surat-tugas/list";
        }
        model.addAttribute("surat", suratOpt.get());
        return "kepalaspi/form-persetujuan";
    }

    @PostMapping("/approve")
    public String approveSurat(@RequestParam("suratId") Long suratId,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes ra) {
        try {
            suratTugasService.approveSuratTugas(suratId, userDetails.getUser());
            ra.addFlashAttribute("successMessage", "Surat Tugas berhasil DISETUJUI.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Gagal menyetujui surat: " + e.getMessage());
        }
        return "redirect:/kepalaspi/surat-tugas/list";
    }

    @PostMapping("/reject")
    public String rejectSurat(@RequestParam("suratId") Long suratId,
                              @RequestParam("catatanPersetujuan") String catatan,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes ra) {
        if (catatan == null || catatan.trim().isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Alasan penolakan harus diisi.");
            return "redirect:/kepalaspi/surat-tugas/view/" + suratId;
        }
        try {
            suratTugasService.rejectSuratTugas(suratId, catatan, userDetails.getUser());
            ra.addFlashAttribute("successMessage", "Surat Tugas telah DITOLAK.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Gagal menolak surat: " + e.getMessage());
        }
        return "redirect:/kepalaspi/surat-tugas/list";
    }
}