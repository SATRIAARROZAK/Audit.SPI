package Audit.Auditing.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class KertasKerjaAuditDto {

    @NotNull
    private Long suratTugasId;

    @NotEmpty(message = "Prosedur tidak boleh kosong")
    private String prosedur;

    private String tahapan;

    private MultipartFile dokumen;
}