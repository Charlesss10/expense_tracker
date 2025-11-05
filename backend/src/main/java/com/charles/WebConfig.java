package com.charles;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("https://expense-tracker-eosin-one-95.vercel.app/")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}