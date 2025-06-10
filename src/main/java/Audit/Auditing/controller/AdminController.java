package Audit.Auditing.controller;

import Audit.Auditing.dto.UserDto;
import Audit.Auditing.model.User;
import Audit.Auditing.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin") // Semua endpoint di controller ini diawali /admin
public class AdminController {

    @Autowired
    private UserService userService;

    // Daftar role yang bisa dipilih
    private final List<String> availableRoles = Arrays.asList("ADMIN", "KEPALASPI", "SEKRETARIS", "PEGAWAI");

    @GetMapping("/users/add")
    public String showAddUserForm(Model model) {
        model.addAttribute("userDto", new UserDto());
        model.addAttribute("availableRoles", availableRoles); // Kirim daftar role ke view
        return "admin/add-user"; // Path ke template: templates/admin/add-user.html
    }

    @PostMapping("/users/save")
    public String saveUser(@Valid @ModelAttribute("userDto") UserDto userDto,
                           BindingResult result, Model model) {
        // Cek validasi custom jika diperlukan (misal username/email sudah ada)
        if (userService.findByUsername(userDto.getUsername()).isPresent()) {
            result.rejectValue("username", "username.exists", "Username sudah digunakan");
        }
        if (userService.findByEmail(userDto.getEmail()).isPresent()) {
            result.rejectValue("email", "email.exists", "Email sudah digunakan");
        }

        if (result.hasErrors()) {
            model.addAttribute("availableRoles", availableRoles);
            return "admin/add-user";
        }

        userService.saveUser(userDto);
        return "redirect:/admin/users/list?success"; // Redirect ke daftar user dengan notifikasi sukses
    }

    @GetMapping("/users/list")
    public String listUsers(Model model) {
        List<User> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "admin/list-users"; // Path ke template: templates/admin/list-users.html
    }
}