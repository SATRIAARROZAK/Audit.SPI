package Audit.Auditing.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class KertasKerjaAuditDto {

    @NotNull
    private Long suratTugasId;

    // Menerima beberapa input prosedur dari form
    @NotEmpty(message = "Minimal harus ada satu prosedur.")
    private List<String> prosedur;

    // Menerima beberapa input tahapan dari form
    private List<String> tahapan;

    // Menerima informasi prosedur mana yang dimiliki oleh setiap tahapan
    @NotNull
    private List<Integer> prosedurIndex;

    private MultipartFile dokumen;
}