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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
// import java.util.stream.Collectors; // Not needed if using direct repository method

@Controller
@RequestMapping("/audit")
public class AuditController {

    @Autowired
    private SuratTugasService suratTugasService;

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

        // **Panggil metode baru yang difilter untuk Ketua Tim**
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
        return "audit/rencana-audit";
    }

    @GetMapping("/review")
    public String rencanaReviewAudit(Model model) {
        model.addAttribute("pageTitle", "Rencana Review Audit");
        return "audit/rencana-review-audit";
    }
}