package com.backandwhite;

import com.backandwhite.common.configuration.annotation.EnableCoreApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;

@EnableCoreApplication
@OpenAPIDefinition(servers = {
        @Server(url = "https://product-category.up.railway.app", description = "Production Server."),
        @Server(url = "https://localhost:6001", description = "Local Server.")
})
public class MicProductcategoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicProductcategoryApplication.class, args);
    }
}
