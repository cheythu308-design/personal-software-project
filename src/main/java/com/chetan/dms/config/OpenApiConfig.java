package com.chetan.dms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** API metadata shown by the generated OpenAPI documentation. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI documentManagementOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Document Management System API")
                .version("0.1.0")
                .description("REST API for the Document Management System."));
    }
}
