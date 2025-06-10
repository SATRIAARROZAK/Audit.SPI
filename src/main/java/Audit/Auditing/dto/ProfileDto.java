package Audit.Auditing.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProfileDto {

    private MultipartFile photo;

    // Field baru untuk tanda tangan
    private MultipartFile signatureImage;
    private String signatureDataUrl; // Untuk menampung base64 dari canvas

    @NotEmpty(message = "Nama lengkap tidak boleh kosong")
    private String fullName;

    @NotEmpty(message = "Jabatan tidak boleh kosong")
    private String position;

    @NotEmpty(message = "Nomor telepon tidak boleh kosong")
    private String phoneNumber;

    @NotEmpty(message = "Alamat tidak boleh kosong")
    private String address;
}