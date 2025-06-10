package Audit.Auditing.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDto {
    @NotEmpty(message = "Username tidak boleh kosong")
    private String username;

    @NotEmpty(message = "Email tidak boleh kosong")
    @Email(message = "Format email tidak valid")
    private String email;

    // Password tidak lagi @NotEmpty, tapi jika diisi harus min 8 karakter
    @Size(min = 8, message = "Password minimal 8 karakter")
    private String password;

    @NotEmpty(message = "Role tidak boleh kosong")
    private String role;
}