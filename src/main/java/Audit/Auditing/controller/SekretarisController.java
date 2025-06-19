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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/sekretaris/surat-tugas")
public class SekretarisController {

    @Autowired
    private SuratTugasService suratTugasService;

    // Halaman untuk menampilkan daftar surat yang perlu direview
    @GetMapping("/list")
    public String listSuratUntukDireview(Model model) {
        // Ambil surat dengan status BARU atau yang DIKEMBALIKAN_KE_ADMIN
        List<SuratTugas> listSurat = suratTugasService.getSuratByStatus(StatusSuratTugas.BARU);
        model.addAttribute("listSurat", listSurat);
        model.addAttribute("pageTitle", "Daftar Surat untuk Direview");
        // Pass StatusSuratTugas enum to the view for dynamic styling/text based on status
        model.addAttribute("T(Audit.Auditing.model.StatusSuratTugas)", StatusSuratTugas.class);
        return "sekretaris/list-surat-tugas";
    }

    // Halaman untuk form review
    @GetMapping("/review/{id}")
    public String showReviewForm(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        Optional<SuratTugas> suratOpt = suratTugasService.getSuratTugasById(id);
        if (suratOpt.isEmpty() || (suratOpt.get().getStatus() != StatusSuratTugas.BARU && suratOpt.get().getStatus() != StatusSuratTugas.DIKEMBALIKAN_KE_ADMIN)) {
            ra.addFlashAttribute("errorMessage", "Surat tugas tidak valid atau tidak dalam status yang benar untuk direview.");
            return "redirect:/sekretaris/surat-tugas/list";
        }
        SuratTugas surat = suratOpt.get();
        String filePath = surat.getFilePath();
        
        model.addAttribute("surat", surat);
        boolean isPdf = filePath != null && filePath.toLowerCase().endsWith(".pdf");
        model.addAttribute("fileUrl", "/profile-photos/" + filePath);
        model.addAttribute("isPdf", isPdf);
        model.addAttribute("T(Audit.Auditing.model.StatusSuratTugas)", StatusSuratTugas.class); // Pass Status enum
        
        return "sekretaris/form-review-surat";
    }

    // Proses submit form review (now just changes status)
    @PostMapping("/submit-review")
    public String submitReview(@RequestParam("suratId") Long suratId,
                               RedirectAttributes ra) { // Remove date params as dates are set by admin

        try {
            suratTugasService.reviewSuratTugas(suratId); // Simplified call
            ra.addFlashAttribute("successMessage", "Surat tugas telah direview dan diteruskan ke Kepala SPI.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Gagal memproses review: " + e.getMessage());
        }

        return "redirect:/sekretaris/surat-tugas/list";
    }

    // New POST endpoint for returning surat for revision
    @PostMapping("/return-for-revision")
    public String returnForRevision(@RequestParam("suratId") Long suratId,
                                    @RequestParam("catatanRevisi") String catatan,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    RedirectAttributes ra) {
        if (catatan == null || catatan.trim().isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Catatan revisi harus diisi.");
            return "redirect:/sekretaris/surat-tugas/review/" + suratId;
        }
        try {
            User secretary = userDetails.getUser();
            suratTugasService.returnSuratTugasForRevision(suratId, catatan, secretary);
            ra.addFlashAttribute("successMessage", "Surat tugas berhasil dikembalikan untuk revisi ke Admin.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Gagal mengembalikan surat tugas untuk revisi: " + e.getMessage());
        }
        return "redirect:/sekretaris/surat-tugas/list";
    }
}