package Audit.Auditing.controller;

import Audit.Auditing.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PdfController {

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/pdf/preview/{fileName}")
    public ResponseEntity<Resource> previewPdfPage(@PathVariable String fileName, @RequestParam(defaultValue = "0") int page) {
        try {
            byte[] imageBytes = fileStorageService.convertPdfPageToImage(fileName, page);
            ByteArrayResource resource = new ByteArrayResource(imageBytes);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "-page-" + page + ".png\"")
                    .contentType(MediaType.IMAGE_PNG)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}