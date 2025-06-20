package Audit.Auditing.controller;

import Audit.Auditing.config.CustomUserDetails;
import Audit.Auditing.model.StatusSuratTugas;
import Audit.Auditing.model.SuratTugas;
import Audit.Auditing.model.User;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/pegawai/surat-tugas")
public class PegawaiController {

    @Autowired
    private SuratTugasService suratTugasService;

    @GetMapping("/list")
    public String listSuratTugasPegawai(
            Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "tanggalMulaiAudit") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword) {

        User currentUser = userDetails.getUser();
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<SuratTugas> suratTugasPage;
        // For pegawai, the logic is slightly different: we get tasks specifically for THIS user
        // and then apply search filter. This might require a custom query in repository if performance is critical.
        if (keyword != null && !keyword.trim().isEmpty()) {
            // This is a simplified search. A more robust solution would involve modifying the findTugasForUserByStatus query in the repository.
            List<SuratTugas> allUserSurat = suratTugasService.getTugasUntukPegawai(currentUser); // Get all, then filter
            List<SuratTugas> filteredSurat = allUserSurat.stream()
                .filter(s -> s.getNomorSurat().toLowerCase().contains(keyword.toLowerCase()) ||
                             s.getDeskripsiSurat() != null && s.getDeskripsiSurat().toLowerCase().contains(keyword.toLowerCase()) ||
                             s.getJenisAudit().getDisplayName().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());

            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), filteredSurat.size());
            suratTugasPage = new org.springframework.data.domain.PageImpl<>(
                filteredSurat.subList(start, end), pageable, filteredSurat.size());
        } else {
            suratTugasPage = suratTugasService.getTugasUntukPegawai(currentUser, pageable);
        }

        model.addAttribute("listSurat", suratTugasPage.getContent());
        model.addAttribute("currentPage", suratTugasPage.getNumber());
        model.addAttribute("totalPages", suratTugasPage.getTotalPages());
        model.addAttribute("totalItems", suratTugasPage.getTotalElements());
        model.addAttribute("pageSize", suratTugasPage.getSize());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("keyword", keyword);
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