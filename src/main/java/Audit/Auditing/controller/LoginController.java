package Audit.Auditing.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Username atau password salah!");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "Anda berhasil logout.");
        }
        return "login"; // Nama file HTML (login.html)
    }

    // Contoh halaman setelah login sukses
    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "layouts/dashboard"; // Buat file dashboard.html
    }

    // Anda bisa menambahkan controller untuk registrasi di sini
    // @GetMapping("/register")
    // public String registerPage(Model model) {
    //     model.addAttribute("user", new com.example.auditingapp.model.User());
    //     return "register";
    // }

    // @PostMapping("/register")
    // public String registerUser(@ModelAttribute com.example.auditingapp.model.User user, BindingResult result, Model model) {
    //     // Tambahkan validasi dan logika penyimpanan user
    //     // Jangan lupa encode password sebelum disimpan
    //     // userService.registerUser(user);
    //     return "redirect:/login";
    // }
}