package com.example.cricket_app.config;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {//this is like telling spring that i have created a bean->openapi object.
        final String securitySchemeName = "bearerAuth";

        SecurityScheme securityScheme = new SecurityScheme()
                .name(securitySchemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");//this means telling the swagger that use Http authentication and token is jwt.

        SecurityRequirement securityRequirement = new SecurityRequirement();
        securityRequirement.addList(securitySchemeName);//adding authorization to the list of apis.

        Info apiInfo = new Info()
                .title("Cricket App API")
                .version("1.0")
                .description("API documentation for Cricket Application");//this is api metadata appears on the ui page.

        // Build and return the OpenAPI object
        return new OpenAPI()
                .info(apiInfo)
                .addSecurityItem(securityRequirement)
                .components(new Components().addSecuritySchemes(securitySchemeName, securityScheme));
    }
}