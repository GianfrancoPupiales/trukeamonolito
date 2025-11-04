package com.apirip.trukeamonolito.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String STUDENTS_DIR = "file:/C:/trukeamonolito/uploads/students/"; // <- agrega la barra
    private static final String PRODUCTS_DIR = "file:/C:/trukeamonolito/uploads/products/"; // <- agrega la barra

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/student-images/**")
                .addResourceLocations(STUDENTS_DIR);
        registry.addResourceHandler("/product-images/**")
                .addResourceLocations(PRODUCTS_DIR);
    }
}
