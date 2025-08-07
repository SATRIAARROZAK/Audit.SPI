package Audit.Auditing.controller.admin;

import Audit.Auditing.dto.UserDto;
import Audit.Auditing.model.User;
import Audit.Auditing.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
        return "pages/admin/add-user";
    }

    @PostMapping("/users/save")
    public String saveUser(@Validated(UserDto.Create.class) @ModelAttribute("userDto") UserDto userDto,
            BindingResult result, Model model, RedirectAttributes redirectAttributes) {

        if (userService.findByUsername(userDto.getUsername()).isPresent()) {
            result.rejectValue("username", "username.exists", "Username sudah digunakan");
        }
        if (userService.findByEmail(userDto.getEmail()).isPresent()) {
            result.rejectValue("email", "email.exists", "Email sudah digunakan");
        }

        if (result.hasErrors()) {
            model.addAttribute("availableRoles", availableRoles);
            return "pages/admin/add-user";
        }

        userService.saveUser(userDto);
        redirectAttributes.addFlashAttribute("successMessage", "User baru berhasil ditambahkan!");
        return "redirect:/pages/admin/users/list";
    }

    @GetMapping("/users/list")
    public String listUsers(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "username") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String keyword) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> userPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            userPage = userService.searchUsers(keyword.trim(), pageable);
        } else {
            userPage = userService.findAllUsers(pageable);
        }

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", userPage.getNumber());
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalItems", userPage.getTotalElements());
        model.addAttribute("pageSize", userPage.getSize());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("keyword", keyword);
        return "pages/admin/list-user";
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
        userDto.setRole(user.getRole().name().toUpperCase());

        model.addAttribute("userDto", userDto);
        model.addAttribute("userId", id);
        model.addAttribute("availableRoles", availableRoles);
        return "pages/admin/edit-user";
    }

    @PostMapping("/users/update/{id}")
    public String updateUser(@PathVariable("id") Long id,
            @Validated(UserDto.Update.class) @ModelAttribute("userDto") UserDto userDto,
            BindingResult result, Model model, RedirectAttributes redirectAttributes) {

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
            return "pages/admin/edit-user";
        }

        userService.updateUser(id, userDto);
        redirectAttributes.addFlashAttribute("successMessage", "User berhasil diupdate!");
        return "redirect:/pages/admin/users/list";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User berhasil dihapus.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal menghapus user. " + e.getMessage());
        }
        return "redirect:/pages/admin/users/list";
    }
}