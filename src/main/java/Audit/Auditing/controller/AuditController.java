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
import org.springframework.security.access.prepost.PreAuthorize; // Import this
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable; // Import this
import org.springframework.web.bind.annotation.PostMapping; // Import this
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Import this

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/audit")
public class AuditController {

    @Autowired
    private SuratTugasService suratTugasService;

    @GetMapping("/rencana")
    // @PreAuthorize("hasAnyAuthority('KEPALASPI', 'ADMIN', 'PEGAWAI')") // Otorisasi umum untuk halaman rencana audit
    public String rencanaAudit(
            Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "tanggalMulaiAudit") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String keyword) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<SuratTugas> suratTugasPage = suratTugasService.getTugasUntukKetuaTim(userDetails.getUser(), pageable, keyword);

        model.addAttribute("listSurat", suratTugasPage.getContent());
        model.addAttribute("currentPage", suratTugasPage.getNumber());
        model.addAttribute("totalPages", suratTugasPage.getTotalPages());
        model.addAttribute("totalItems", suratTugasPage.getTotalElements());
        model.addAttribute("pageSize", suratTugasPage.getSize());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("keyword", keyword);
        model.addAttribute("pageTitle", "Rencana Audit (Khusus Ketua Tim)");
        model.addAttribute("currentUser", userDetails.getUser()); // Pass current user for Thymeleaf check
        return "audit/rencana-audit";
    }

    // Endpoint untuk menampilkan form buat kertas kerja
    @GetMapping("/kertas-kerja/new/{suratTugasId}")
    @PreAuthorize("hasAuthority('PEGAWAI')") // Hanya pegawai yang bisa membuat kertas kerja
    public String showCreateKertasKerjaForm(@PathVariable("suratTugasId") Long suratTugasId, Model model,
                                            @AuthenticationPrincipal CustomUserDetails userDetails,
                                            RedirectAttributes ra) {
        Optional<SuratTugas> suratOpt = suratTugasService.getSuratTugasById(suratTugasId);
        if (suratOpt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Surat Tugas tidak ditemukan.");
            return "redirect:/audit/rencana";
        }

        SuratTugas surat = suratOpt.get();

        // Pastikan hanya ketua tim dari surat tugas ini yang bisa mengakses
        if (!surat.getKetuaTim().getId().equals(userDetails.getUser().getId())) {
            ra.addFlashAttribute("errorMessage", "Anda bukan ketua tim dari surat tugas ini.");
            return "redirect:/audit/rencana";
        }
        
        // Pastikan status surat tugas sudah DISETUJUI
        if (surat.getStatus() != StatusSuratTugas.DISETUJUI) {
            ra.addFlashAttribute("errorMessage", "Kertas kerja hanya bisa dibuat untuk surat tugas yang sudah disetujui.");
            return "redirect:/audit/rencana";
        }


        model.addAttribute("suratTugas", surat);
        model.addAttribute("pageTitle", "Buat Kertas Kerja");
        return "audit/buat-kertas-kerja";
    }

    // Endpoint untuk memproses submit form kertas kerja (contoh sederhana)
    @PostMapping("/kertas-kerja/save")
    @PreAuthorize("hasAuthority('PEGAWAI')")
    public String saveKertasKerja(@RequestParam("suratTugasId") Long suratTugasId,
                                  @RequestParam("judulKertasKerja") String judulKertasKerja,
                                  @RequestParam("deskripsiKertasKerja") String deskripsiKertasKerja,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  RedirectAttributes ra) {
        // Dalam implementasi nyata, Anda akan menyimpan data ini ke database
        // dan mungkin memperbarui status di SuratTugas jika ada.
        // Untuk demo ini, kita hanya menampilkan pesan sukses.

        Optional<SuratTugas> suratOpt = suratTugasService.getSuratTugasById(suratTugasId);
        if (suratOpt.isEmpty() || !suratOpt.get().getKetuaTim().getId().equals(userDetails.getUser().getId())) {
             ra.addFlashAttribute("errorMessage", "Surat Tugas tidak ditemukan atau Anda tidak memiliki izin.");
            return "redirect:/audit/rencana";
        }

        // Logic to save Kertas Kerja (dummy for now)
        System.out.println("Kertas Kerja Baru Dibuat:");
        System.out.println("Surat Tugas ID: " + suratTugasId);
        System.out.println("Judul: " + judulKertasKerja);
        System.out.println("Deskripsi: " + deskripsiKertasKerja);
        System.out.println("Dibuat oleh: " + userDetails.getUsername());

        ra.addFlashAttribute("successMessage", "Kertas Kerja '" + judulKertasKerja + "' berhasil dibuat!");
        return "redirect:/audit/rencana";
    }


    @GetMapping("/review")
    public String rencanaReviewAudit(Model model) {
        model.addAttribute("pageTitle", "Rencana Review Audit");
        return "audit/rencana-review-audit";
    }
}