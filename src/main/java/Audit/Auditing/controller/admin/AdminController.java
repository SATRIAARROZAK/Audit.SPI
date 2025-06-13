package Audit.Auditing.controller.admin;

import Audit.Auditing.dto.UserDto;
import Audit.Auditing.model.User;
import Audit.Auditing.service.UserService;
// Hapus import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
// Tambahkan import ini
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    private final List<String> availableRoles = Arrays.asList("ADMIN", "KEPALASPI", "SEKRETARIS", "PEGAWAI");

    @GetMapping("/users/add")
    public String showAddUserForm(Model model) {
        model.addAttribute("userDto", new UserDto());
        model.addAttribute("availableRoles", availableRoles);
        return "admin/add-user";
    }

    @PostMapping("/users/save")
    // Gunakan @Validated dengan grup Create.class
    public String saveUser(@Validated(UserDto.Create.class) @ModelAttribute("userDto") UserDto userDto,
            BindingResult result, Model model, RedirectAttributes redirectAttributes) {

        if (userService.findByUsername(userDto.getUsername()).isPresent()) {
            result.rejectValue("username", "username.exists", "Username sudah digunakan");
        }
        if (userService.findByEmail(userDto.getEmail()).isPresent()) {
            result.rejectValue("email", "email.exists", "Email sudah digunakan");
        }

        // Cek manual password tidak lagi diperlukan karena sudah ditangani oleh grup
        // validasi

        if (result.hasErrors()) {
            model.addAttribute("availableRoles", availableRoles);
            return "admin/add-user";
        }

        userService.saveUser(userDto);
        redirectAttributes.addFlashAttribute("successMessage", "User baru berhasil ditambahkan!");
        return "redirect:/admin/users/list";
    }

    // Metode listUsers dan showEditUserForm tidak perlu diubah...
    @GetMapping("/users/list")
    public String listUsers(Model model) {
        List<User> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "admin/list-user";
    }

    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        // ... (kode tetap sama)
        Optional<User> userOptional = userService.findById(id);
        if (userOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User tidak ditemukan.");
            return "redirect:/admin/users/list";
        }
        User user = userOptional.get();
        UserDto userDto = new UserDto();
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setRole(user.getRole().name().toUpperCase()); // Ini akan menghasilkan "PEGAWAI"

        model.addAttribute("userDto", userDto);
        model.addAttribute("userId", id);
        model.addAttribute("availableRoles", availableRoles);
        return "admin/edit-user";
    }

    @PostMapping("/users/update/{id}")
    // Gunakan @Validated dengan grup Update.class
    public String updateUser(@PathVariable("id") Long id,
            @Validated(UserDto.Update.class) @ModelAttribute("userDto") UserDto userDto,
            BindingResult result, Model model, RedirectAttributes redirectAttributes) {

        // ... (validasi duplikasi username/email tetap sama)
        Optional<User> existingUserByUsername = userService.findByUsername(userDto.getUsername());
        if (existingUserByUsername.isPresent() && !existingUserByUsername.get().getId().equals(id)) {
            result.rejectValue("username", "username.exists", "Username sudah digunakan oleh user lain.");
        }

        Optional<User> existingUserByEmail = userService.findByEmail(userDto.getEmail());
        if (existingUserByEmail.isPresent() && !existingUserByEmail.get().getId().equals(id)) {
            result.rejectValue("email", "email.exists", "Email sudah digunakan oleh user lain.");
        }

        if (result.hasErrors()) {
            model.addAttribute("userId", id);
            model.addAttribute("availableRoles", availableRoles);
            return "admin/edit-user";
        }

        // Pastikan role di-handle dengan benar (perbaikan dari masalah sebelumnya)
        userService.updateUser(id, userDto);
        redirectAttributes.addFlashAttribute("successMessage", "User berhasil diupdate!");
        return "redirect:/admin/users/list";
    }

    // Metode deleteUser tidak perlu diubah...
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        // ... (kode tetap sama)
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User berhasil dihapus.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal menghapus user. " + e.getMessage());
        }
        return "redirect:/admin/users/list";
    }
}