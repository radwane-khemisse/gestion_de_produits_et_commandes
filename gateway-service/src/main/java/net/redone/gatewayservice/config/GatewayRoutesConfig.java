package net.redone.gatewayservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator gatewayRoutes(
            RouteLocatorBuilder builder,
            @Value("${services.produit-url:http://localhost:8081}") String produitUrl,
            @Value("${services.commande-url:http://localhost:8082}") String commandeUrl
    ) {
        return builder.routes()
                .route("produit-base", route -> route
                        .path("/api/produits")
                        .uri(produitUrl))
                .route("produit-service", route -> route
                        .path("/api/produits/**")
                        .uri(produitUrl))
                .route("commande-base", route -> route
                        .path("/api/commandes")
                        .uri(commandeUrl))
                .route("commande-service", route -> route
                        .path("/api/commandes/**")
                        .uri(commandeUrl))
                .route("catalog-images", route -> route
                        .path("/catalog/**")
                        .uri(produitUrl))
                .build();
    }
}
