
package com.app.TechSphere.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.servlet.MultipartConfigFactory;
import org.springframework.util.unit.DataSize;

@Configuration
public class FileUploadConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();

        // Correct way using DataSize
        factory.setMaxFileSize(DataSize.ofMegabytes(10));     // per file
        factory.setMaxRequestSize(DataSize.ofMegabytes(50));  // total request

        return factory.createMultipartConfig();
    }
}
