package Audit.Auditing.controller;

import Audit.Auditing.dto.UserDto;
import Audit.Auditing.model.User;
import Audit.Auditing.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
        return "add-user";
    }

    @PostMapping("/users/save")
    public String saveUser(@Valid @ModelAttribute("userDto") UserDto userDto,
                           BindingResult result, Model model, RedirectAttributes redirectAttributes) {

        // Validasi custom untuk username/email duplikat
        if (userService.findByUsername(userDto.getUsername()).isPresent()) {
            result.rejectValue("username", "username.exists", "Username sudah digunakan");
        }
        if (userService.findByEmail(userDto.getEmail()).isPresent()) {
            result.rejectValue("email", "email.exists", "Email sudah digunakan");
        }

        // Validasi manual untuk password saat membuat user baru
        if (userDto.getPassword() == null || userDto.getPassword().isEmpty()) {
            result.rejectValue("password", "password.notempty", "Password tidak boleh kosong");
        } else if (userDto.getPassword().length() < 6) {
            result.rejectValue("password", "password.size", "Password minimal 6 karakter");
        }

        if (result.hasErrors()) {
            model.addAttribute("availableRoles", availableRoles);
            return "add-user";
        }

        userService.saveUser(userDto);
        redirectAttributes.addFlashAttribute("successMessage", "User baru berhasil ditambahkan!");
        return "redirect:/admin/users/list";
    }

    @GetMapping("/users/list")
    public String listUsers(Model model) {
        List<User> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "list-user";
    }

    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> userOptional = userService.findById(id);
        if (userOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User tidak ditemukan.");
            return "redirect:/admin/users/list";
        }
        User user = userOptional.get();
        UserDto userDto = new UserDto();
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setRole(user.getRole().name());
        // Password sengaja tidak di-set agar field di form kosong

        model.addAttribute("userDto", userDto);
        model.addAttribute("userId", id);
        model.addAttribute("availableRoles", availableRoles);
        return "edit-user";
    }

    @PostMapping("/users/update/{id}")
    public String updateUser(@PathVariable("id") Long id, @Valid @ModelAttribute("userDto") UserDto userDto,
                             BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        // Validasi duplikasi username/email untuk user yang berbeda
        userService.findByUsername(userDto.getUsername()).ifPresent(user -> {
            if (!user.getId().equals(id)) {
                result.rejectValue("username", "username.exists", "Username sudah digunakan oleh user lain.");
            }
        });
        userService.findByEmail(userDto.getEmail()).ifPresent(user -> {
            if (!user.getId().equals(id)) {
                result.rejectValue("email", "email.exists", "Email sudah digunakan oleh user lain.");
            }
        });

        if (result.hasErrors()) {
            model.addAttribute("userId", id);
            model.addAttribute("availableRoles", availableRoles);
            return "edit-user";
        }

        userService.updateUser(id, userDto);
        redirectAttributes.addFlashAttribute("successMessage", "User berhasil diupdate!");
        return "redirect:/admin/users/list";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User berhasil dihapus.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal menghapus user. " + e.getMessage());
        }
        return "redirect:/admin/users/list";
    }
}