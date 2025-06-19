package Audit.Auditing.dto;

import Audit.Auditing.model.JenisAudit; // Import new enum
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
public class SuratTugasDTO {

    // Definisikan grup validasi
    public interface Create {}
    public interface Update {}

    @NotEmpty(message = "Nomor surat tidak boleh kosong", groups = {Create.class, Update.class})
    private String nomorSurat; // 1. Nomer surat

    // 2. Deskripsi surat (opsional), so remove @NotEmpty for it to be optional
    private String deskripsiSurat; // Renamed from tujuan

    @NotNull(message = "Jenis audit harus dipilih", groups = {Create.class, Update.class})
    private JenisAudit jenisAudit; // 4. Tujuan (form select)

    // Wajib diisi hanya saat Create
    @NotNull(message = "File surat tugas tidak boleh kosong", groups = Create.class)
    // Untuk Update, kita tidak memaksa upload ulang, jadi tidak perlu NotNull di Update group
    private MultipartFile suratFile; // 7. Upload surat

    @NotNull(message = "Ketua tim harus dipilih", groups = {Create.class, Update.class})
    private Long ketuaTimId; // 5. Pilih pegawai sebagai ketua tim

    @NotEmpty(message = "Pilih minimal satu anggota tim", groups = {Create.class, Update.class})
    private List<Long> anggotaTimIds; // 6. Pilih pegawai sebagai anggota tim

    @NotNull(message = "Tanggal mulai audit tidak boleh kosong", groups = {Create.class, Update.class})
    // @FutureOrPresent(message = "Tanggal mulai audit tidak boleh di masa lalu", groups = {Create.class, Update.class}) // remove this for now, admin might set past dates for initial creation/planning
    private LocalDate tanggalMulaiAudit; // 3. set tgl mulai audit

    @NotNull(message = "Tanggal selesai audit tidak boleh kosong", groups = {Create.class, Update.class})
    // @FutureOrPresent(message = "Tanggal selesai audit tidak boleh di masa lalu", groups = {Create.class, Update.class}) // remove this for now
    private LocalDate tanggalSelesaiAudit; // 3. set tgl selesai audit
}