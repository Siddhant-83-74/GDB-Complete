package com.gdb.aadhar.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI aadharServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Aadhar Verification Service API")
                        .description("Third-party simulation microservice for Aadhar (UIDAI) number verification. "
                                + "Used during Savings Account creation for KYC compliance.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("GDB Architecture Team")
                                .email("architecture@gdb.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8005").description("Development Server")));
    }
}
