package com.portfolio.config.SecurityConfig;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Bean
    public OpenAPI portfolioOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Portfolio Tracker API")
                        .description("""
                                Enterprise-grade Indian equity portfolio management system backed by:
                                - **Zerodha Kite Connect** for live market data & holdings sync
                                - **Claude AI (Sonnet 4.6)** for portfolio analysis, recommendations, and chat
                                - **WebSocket** for real-time price streaming

                                ## Authentication
                                All endpoints except `/api/auth/**` require a Bearer JWT token.
                                Use the **Authorize** button above to set your token.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Portfolio Tracker")
                                .url(frontendUrl))
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
                .addServersItem(new Server().url("http://localhost:8080").description("Local Development"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token. Obtain it from POST /api/auth/login")))
                .tags(List.of(
                        new Tag().name("Authentication").description("Register and login to get a JWT token"),
                        new Tag().name("Portfolio Management").description("Create portfolios, manage holdings"),
                        new Tag().name("Stock Management").description("Search stocks, get prices and technical analysis"),
                        new Tag().name("Transactions").description("Execute buy/sell transactions"),
                        new Tag().name("Kite Connect").description("Connect Zerodha account and sync live holdings"),
                        new Tag().name("AI Portfolio Analysis").description("Claude AI-powered portfolio assessment, chat, and recommendations")
                ));
    }
}
