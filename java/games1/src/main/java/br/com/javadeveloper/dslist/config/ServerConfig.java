package br.com.javadeveloper.dslist.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServerConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Ativa o "pretty print"
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }
}
