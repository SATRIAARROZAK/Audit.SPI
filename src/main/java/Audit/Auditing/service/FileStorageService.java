package Audit.Auditing.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Tidak dapat membuat direktori untuk menyimpan file.", ex);
        }
    }

    public Path getFileStorageLocation() {
        return this.fileStorageLocation;
    }

    public String storeFile(MultipartFile file) {
        if (file.isEmpty() || file.getOriginalFilename() == null) {
            return null;
        }

        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        try {
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
        } catch (Exception e) {
            // Abaikan jika tidak ada ekstensi
        }

        String fileName = UUID.randomUUID().toString() + fileExtension;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Gagal menyimpan file " + fileName, ex);
        }
    }

    public String storeBase64File(String base64Data) {
        if (base64Data == null || base64Data.isEmpty()) {
            return null;
        }

        String[] parts = base64Data.split(",");
        if (parts.length != 2) {
             throw new RuntimeException("Format data Base64 tidak valid.");
        }

        String imageBase64 = parts[1];
        byte[] imageBytes = Base64.getDecoder().decode(imageBase64);

        String extension;
        if (parts[0].contains("image/jpeg")) {
            extension = ".jpg";
        } else if (parts[0].contains("image/gif")) {
            extension = ".gif";
        } else {
            extension = ".png"; // Default
        }

        String fileName = UUID.randomUUID().toString() + extension;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.write(targetLocation, imageBytes);
            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Gagal menyimpan file dari Base64 " + fileName, ex);
        }
    }

    /**
     * Mengonversi halaman tertentu dari file PDF menjadi gambar PNG.
     * @param fileName Nama file PDF yang akan dikonversi.
     * @param pageNumber Nomor halaman yang akan diambil (dimulai dari 0).
     * @return Array byte dari gambar PNG.
     * @throws IOException Jika file tidak ditemukan atau gagal dibaca/dikonversi.
     */
    public byte[] convertPdfPageToImage(String fileName, int pageNumber) throws IOException {
        Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
        File file = filePath.toFile();

        if (!file.exists()) {
            throw new FileNotFoundException("File tidak ditemukan: " + fileName);
        }

        try (PDDocument document = PDDocument.load(file)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            
            // Pastikan nomor halaman valid
            if (pageNumber < 0 || pageNumber >= document.getNumberOfPages()) {
                throw new IllegalArgumentException("Nomor halaman tidak valid: " + pageNumber);
            }
            
            // Render halaman PDF menjadi gambar dengan resolusi 150 DPI
            BufferedImage bim = pdfRenderer.renderImageWithDPI(pageNumber, 150, ImageType.RGB);

            // Tulis gambar ke output stream dalam format PNG
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ImageIO.write(bim, "png", baos);
                return baos.toByteArray();
            }
        }
    }
}