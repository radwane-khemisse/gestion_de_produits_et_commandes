package net.redone.gatewayservice.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("produit-base", route -> route
                        .path("/api/produits")
                        .uri("http://localhost:8081"))
                .route("produit-service", route -> route
                        .path("/api/produits/**")
                        .uri("http://localhost:8081"))
                .route("commande-base", route -> route
                        .path("/api/commandes")
                        .uri("http://localhost:8082"))
                .route("commande-service", route -> route
                        .path("/api/commandes/**")
                        .uri("http://localhost:8082"))
                .route("catalog-images", route -> route
                        .path("/catalog/**")
                        .uri("http://localhost:8081"))
                .build();
    }
}
