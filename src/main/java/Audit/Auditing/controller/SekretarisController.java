package Audit.Auditing.controller;

import Audit.Auditing.model.StatusSuratTugas;
import Audit.Auditing.model.SuratTugas;
import Audit.Auditing.service.SuratTugasService;
import org.springframework.beans.factory.annotation.Autowired;
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
        // Ambil surat dengan status BARU atau yang dikembalikan oleh KepalaSPI (akan dibuat nanti)
        List<SuratTugas> listSurat = suratTugasService.getSuratByStatus(StatusSuratTugas.BARU);
        model.addAttribute("listSurat", listSurat);
        model.addAttribute("pageTitle", "Daftar Surat untuk Direview");
        return "sekretaris/list-surat-tugas";
    }

    // Halaman untuk form review
    @GetMapping("/review/{id}")
    public String showReviewForm(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        Optional<SuratTugas> suratOpt = suratTugasService.getSuratTugasById(id);
        if (suratOpt.isEmpty() || suratOpt.get().getStatus() != StatusSuratTugas.BARU) {
            ra.addFlashAttribute("errorMessage", "Surat tugas tidak valid atau tidak dalam status yang benar untuk direview.");
            return "redirect:/sekretaris/surat-tugas/list";
        }
        SuratTugas surat = suratOpt.get();
        String filePath = surat.getFilePath();
        
        model.addAttribute("surat", suratOpt.get());
          boolean isPdf = filePath != null && filePath.toLowerCase().endsWith(".pdf");

        model.addAttribute("surat", surat);
        model.addAttribute("fileUrl", "/profile-photos/" + filePath);
        model.addAttribute("isPdf", isPdf); // Kirim flag isPdf ke view
        return "sekretaris/form-review-surat";
    }

    // Proses submit form review
    @PostMapping("/submit-review")
    public String submitReview(@RequestParam("suratId") Long suratId,
                               @RequestParam("tanggalMulaiAudit") LocalDate tanggalMulaiAudit,
                               @RequestParam("tanggalSelesaiAudit") LocalDate tanggalSelesaiAudit,
                               RedirectAttributes ra) {

        if (tanggalSelesaiAudit.isBefore(tanggalMulaiAudit)) {
            ra.addFlashAttribute("errorMessage", "Tanggal selesai tidak boleh sebelum tanggal mulai.");
            return "redirect:/sekretaris/surat-tugas/review/" + suratId;
        }

        try {
            suratTugasService.reviewAndSetDates(suratId, tanggalMulaiAudit, tanggalSelesaiAudit);
            ra.addFlashAttribute("successMessage", "Surat tugas telah direview dan diteruskan ke Kepala SPI.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Gagal memproses review: " + e.getMessage());
        }

        return "redirect:/sekretaris/surat-tugas/list";
    }
}