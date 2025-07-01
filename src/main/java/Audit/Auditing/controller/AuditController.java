package Audit.Auditing.controller;

import Audit.Auditing.config.CustomUserDetails;
import Audit.Auditing.dto.KertasKerjaAuditDto; // Tambahkan import
import Audit.Auditing.model.KertasKerjaAudit; // Tambahkan import
import Audit.Auditing.model.StatusSuratTugas;
import Audit.Auditing.model.SuratTugas;
import Audit.Auditing.service.KertasKerjaAuditService; // Tambahkan import
import Audit.Auditing.service.SuratTugasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // Tambahkan import
import org.springframework.validation.annotation.Validated; // Tambahkan import
import org.springframework.web.bind.annotation.*; // Ganti menjadi wildcard
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.UUID;
@Controller
@RequestMapping("/audit")
public class AuditController {

    @Autowired
    private SuratTugasService suratTugasService;
    
    // Tambahkan service baru
    @Autowired
    private KertasKerjaAuditService kertasKerjaAuditService;

    @GetMapping("/rencana")
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
        model.addAttribute("currentUser", userDetails.getUser()); 
        return "audit/rencana-audit";
    }
    
    // Modifikasi method ini
    @GetMapping("/kertas-kerja/new/{suratTugasId}")
    @PreAuthorize("hasAuthority('PEGAWAI')")
    public String showCreateKertasKerjaForm(@PathVariable("suratTugasId") Long suratTugasId, Model model,
                                            @AuthenticationPrincipal CustomUserDetails userDetails,
                                            RedirectAttributes ra) {
        Optional<SuratTugas> suratOpt = suratTugasService.getSuratTugasById(suratTugasId);
        if (suratOpt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Surat Tugas tidak ditemukan.");
            return "redirect:/audit/rencana";
        }

        SuratTugas surat = suratOpt.get();

        if (!surat.getKetuaTim().getId().equals(userDetails.getUser().getId())) {
            ra.addFlashAttribute("errorMessage", "Anda bukan ketua tim dari surat tugas ini.");
            return "redirect:/audit/rencana";
        }
        
        KertasKerjaAuditDto dto = new KertasKerjaAuditDto();
        dto.setSuratTugasId(surat.getId());

        List<KertasKerjaAudit> existingKertasKerja = kertasKerjaAuditService.getBySuratTugasId(suratTugasId);

        // Mengelompokkan KertasKerjaAudit berdasarkan prosedurGroup
        Map<UUID, List<KertasKerjaAudit>> groupedKertasKerja = existingKertasKerja.stream()
                .collect(Collectors.groupingBy(KertasKerjaAudit::getProsedurGroup));

        model.addAttribute("suratTugas", surat);
        model.addAttribute("kertasKerjaDto", dto);
        model.addAttribute("listKertasKerja", groupedKertasKerja); // Kirim map ke view
        model.addAttribute("pageTitle", "Buat Kertas Kerja");
        return "audit/halaman-kertas-kerja";
    }

    // Ganti method ini dari PostMapping("/halaman-kerja/save")
    @PostMapping("/kertas-kerja/save")
    @PreAuthorize("hasAuthority('PEGAWAI')")
    public String saveKertasKerja(@Validated @ModelAttribute("kertasKerjaDto") KertasKerjaAuditDto kertasKerjaDto,
                                  BindingResult result,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  RedirectAttributes ra, Model model) {
        
        Optional<SuratTugas> suratOpt = suratTugasService.getSuratTugasById(kertasKerjaDto.getSuratTugasId());
        if (suratOpt.isEmpty() || !suratOpt.get().getKetuaTim().getId().equals(userDetails.getUser().getId())) {
             ra.addFlashAttribute("errorMessage", "Surat Tugas tidak ditemukan atau Anda tidak memiliki izin.");
            return "redirect:/audit/rencana";
        }
        
        if (result.hasErrors()) {
            model.addAttribute("suratTugas", suratOpt.get());
            model.addAttribute("pageTitle", "Buat Kertas Kerja");
            // Muat ulang data untuk ditampilkan jika ada error
            List<KertasKerjaAudit> existingKertasKerja = kertasKerjaAuditService.getBySuratTugasId(kertasKerjaDto.getSuratTugasId());
            Map<UUID, List<KertasKerjaAudit>> groupedKertasKerja = existingKertasKerja.stream()
                    .collect(Collectors.groupingBy(KertasKerjaAudit::getProsedurGroup));
            model.addAttribute("listKertasKerja", groupedKertasKerja);
            return "audit/halaman-kertas-kerja";
        }

        kertasKerjaAuditService.saveDynamic(kertasKerjaDto, userDetails.getUser());

        ra.addFlashAttribute("successMessage", "Kertas Kerja berhasil disimpan!");
        // return "redirect:/audit/kertas-kerja/new/" + kertasKerjaDto.getSuratTugasId();

          return "redirect:/audit/kertas-kerja/new/" + kertasKerjaDto.getSuratTugasId() + "?tab=list";
    }

    @GetMapping("/review")
    public String rencanaReviewAudit(Model model) {
        model.addAttribute("pageTitle", "Rencana Review Audit");
        return "audit/rencana-review-audit";
    }

    // Method baru untuk menampilkan semua kertas kerja
    @GetMapping("/kertas-kerja/list")
    public String listAllKertasKerja(Model model) {
        List<KertasKerjaAudit> list = kertasKerjaAuditService.getAll();
        model.addAttribute("listKertasKerja", list);
        model.addAttribute("pageTitle", "Daftar Semua Kertas Kerja Audit");
        return "audit/list-kertas-kerja"; // file HTML baru
    }
}