package Audit.Auditing.controller;

import Audit.Auditing.config.CustomUserDetails;
import Audit.Auditing.model.StatusSuratTugas;
import Audit.Auditing.model.SuratTugas;
import Audit.Auditing.service.SuratTugasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/audit")
public class AuditController {

    @Autowired
    private SuratTugasService suratTugasService;

    @GetMapping("/rencana")
    public String rencanaAudit(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            // Handle case where user is not logged in or session expired
            return "redirect:/login";
        }
        
        // Mengambil surat tugas yang statusnya DISETUJUI untuk pegawai yang login
        List<SuratTugas> disetujuiSuratTugas = suratTugasService.getTugasUntukPegawai(userDetails.getUser());
        
        model.addAttribute("listSurat", disetujuiSuratTugas);
        model.addAttribute("pageTitle", "Rencana Audit");
        // Di sini Anda akan menambahkan logika untuk mengambil data rencana audit dari database
        // Contoh data dummy:
        // List<RencanaAudit> rencanaList = auditService.getAllRencanaAudit();
        // model.addAttribute("rencanaList", rencanaList);
        return "audit/rencana-audit"; // Mengarahkan ke template HTML baru
    }

    @GetMapping("/review")
    public String rencanaReviewAudit(Model model) {
        model.addAttribute("pageTitle", "Rencana Review Audit");
        // Di sini Anda akan menambahkan logika untuk mengambil data rencana review audit dari database
        // Contoh data dummy:
        // List<RencanaReviewAudit> reviewList = auditService.getAllRencanaReviewAudit();
        // model.addAttribute("reviewList", reviewList);
        return "audit/rencana-review-audit"; // Mengarahkan ke template HTML baru
    }
}