package Audit.Auditing.controller;

import Audit.Auditing.dto.ProfileDto;
import Audit.Auditing.model.User;
import Audit.Auditing.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping("/edit")
    public String showProfileForm(Model model, Principal principal) {
        // Dapatkan user yang sedang login
        User user = userService.findByUsername(principal.getName()).orElse(null);

        if (user == null) {
            return "redirect:/login?error=true";
        }

        ProfileDto profileDto = new ProfileDto();
        // Isi DTO dengan data yang sudah ada jika ada (untuk edit di kemudian hari)
        profileDto.setFullName(user.getFullName());
        profileDto.setPosition(user.getPosition());
        profileDto.setPhoneNumber(user.getPhoneNumber());
        profileDto.setAddress(user.getAddress());

        model.addAttribute("profileDto", profileDto);
        model.addAttribute("isFirstTime", !user.isProfileComplete());

        return "edit-profile";
    }

    @PostMapping("/update")
    public String updateProfile(@Validated @ModelAttribute("profileDto") ProfileDto profileDto,
                                BindingResult result,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "edit-profile";
        }

        try {
            userService.updateProfile(principal.getName(), profileDto);
            redirectAttributes.addFlashAttribute("successMessage", "Profil berhasil diperbarui!");
            // Setelah profil lengkap, arahkan ke dashboard
            return "redirect:/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal memperbarui profil: " + e.getMessage());
            return "redirect:/profile/edit";
        }
    }
}