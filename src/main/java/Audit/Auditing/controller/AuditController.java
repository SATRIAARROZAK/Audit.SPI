package Audit.Auditing.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/audit")
public class AuditController {

    @GetMapping("/rencana")
    public String rencanaAudit(Model model) {
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