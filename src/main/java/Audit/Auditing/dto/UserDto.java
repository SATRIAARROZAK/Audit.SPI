package Audit.Auditing.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserDto {

    // 1. Definisikan Grup Validasi sebagai interface kosong
    public interface Create {}
    public interface Update {}

    @NotEmpty(message = "Username tidak boleh kosong")
    private String username;

    @NotEmpty(message = "Email tidak boleh kosong")
    @Email(message = "Format email tidak valid")
    private String email;
    // 2. Terapkan validasi berbeda berdasarkan grup
    // Wajib diisi hanya saat proses 'Create'
    @NotEmpty(message = "Password tidak boleh kosong", groups = Create.class)
    // Jika diisi (baik Create maupun Update), harus minimal 8 karakter.
    // Regex ini berarti: string kosong ATAU string dengan 8 karakter atau lebih.
    @Pattern(regexp = "^$|^.{8,}$", message = "Password minimal 8 karakter", groups = {Create.class, Update.class})

    private String password;


    @NotEmpty(message = "Role tidak boleh kosong")
    private String role;
}