package Audit.Auditing.controller;

import Audit.Auditing.config.CustomUserDetails;
import Audit.Auditing.model.StatusSuratTugas;
import Audit.Auditing.model.SuratTugas;
import Audit.Auditing.service.SuratTugasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/kepalaspi/surat-tugas")
public class KepalaSpiController {

    @Autowired
    private SuratTugasService suratTugasService;

    @GetMapping("/list")
    public String listSuratUntukDisetujui(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<SuratTugas> suratTugasPage;
        // In KepalaSpi, you typically filter by status, so keyword search would apply ON TOP of that filter
        // For simplicity, I'll demonstrate search on the 'REVIEW_SEKRETARIS' status
        if (keyword != null && !keyword.trim().isEmpty()) {
            // This is a simplified search for KepalaSpi. You might need a more complex query in repository.
            // For now, it will search through all surat tugas and then filter by status in service.
            // A more robust solution would be to add a custom query to SuratTugasRepository for search by status.
            suratTugasPage = suratTugasService.searchSuratTugas(keyword.trim(), pageable);
             suratTugasPage = new org.springframework.data.domain.PageImpl<>(
                suratTugasPage.getContent().stream()
                    .filter(s -> s.getStatus() == StatusSuratTugas.REVIEW_SEKRETARIS)
                    .collect(Collectors.toList()),
                pageable,
                suratTugasPage.getTotalElements()
            );
        } else {
            suratTugasPage = suratTugasService.getSuratByStatus(StatusSuratTugas.REVIEW_SEKRETARIS);
        }

        model.addAttribute("listSurat", suratTugasPage.getContent());
        model.addAttribute("currentPage", suratTugasPage.getNumber());
        model.addAttribute("totalPages", suratTugasPage.getTotalPages());
        model.addAttribute("totalItems", suratTugasPage.getTotalElements());
        model.addAttribute("pageSize", suratTugasPage.getSize());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("keyword", keyword);
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
        SuratTugas surat = suratOpt.get();
        String filePath = surat.getFilePath();

        boolean isPdf = filePath != null && filePath.toLowerCase().endsWith(".pdf");

        model.addAttribute("surat", surat);
        model.addAttribute("fileUrl", "/profile-photos/" + filePath);
        model.addAttribute("isPdf", isPdf); // Kirim flag isPdf ke view

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