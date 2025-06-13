package Audit.Auditing.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class SuratTugasDTO {

    // Definisikan grup validasi
    public interface Create {}
    public interface Update {}

    @NotEmpty(message = "Tujuan surat tugas tidak boleh kosong", groups = {Create.class, Update.class})
    private String tujuan;

    // Wajib diisi hanya saat Create
    @NotNull(message = "File surat tugas tidak boleh kosong", groups = Create.class)
    private MultipartFile suratFile;

    @NotNull(message = "Ketua tim harus dipilih", groups = {Create.class, Update.class})
    private Long ketuaTimId;

    @NotEmpty(message = "Pilih minimal satu anggota tim", groups = {Create.class, Update.class})
    private List<Long> anggotaTimIds;
}