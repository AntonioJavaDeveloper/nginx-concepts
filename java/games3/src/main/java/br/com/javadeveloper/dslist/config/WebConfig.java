package br.com.javadeveloper.dslist.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
/*Just a example for real situation*/
//    @Value("${cors.origins.public:*}")
//    private String publicOrigins;
//
//    @Value("${cors.origins.admin}")
//    private String adminOrigins;
//
//    @Value("${cors.origins.api}")
//    private String apiOrigins;
//
//    @Bean
//    WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//
//                // CORS for public endpoints
//                registry.addMapping("/public/**")
//                        .allowedOrigins(publicOrigins.split(","))
//                        .allowedMethods("GET", "POST");
//
//                // CORS for admin endpoints
//                registry.addMapping("/admin/**")
//                        .allowedOrigins(adminOrigins.split(","))
//                        .allowedMethods("GET", "POST", "PUT", "DELETE")
//                        .allowedHeaders("Authorization", "Content-Type");
//
//                // CORS for main API endpoints
//                registry.addMapping("/api/**")
//                        .allowedOrigins(apiOrigins.split(","))
//                        .allowedMethods("*")
//                        .allowedHeaders("Authorization", "Content-Type", "X-Custom-Header")
//                        .exposedHeaders("X-Total-Count")
//                        .allowCredentials(true);
//            }
//        };
//    }

/*
# CORS origins for each path group in application.properties
cors.origins.public=*
cors.origins.admin=https://admin.my.com
cors.origins.api=https://frontend.my.com
*/
}

