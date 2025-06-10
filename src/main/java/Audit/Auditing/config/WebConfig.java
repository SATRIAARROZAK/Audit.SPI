package Audit.Auditing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Handler untuk menyajikan file foto profil
        registry.addResourceHandler("/profile-photos/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}