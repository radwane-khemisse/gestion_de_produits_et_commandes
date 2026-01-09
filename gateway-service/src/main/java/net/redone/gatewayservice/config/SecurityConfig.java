package net.redone.gatewayservice.config;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/produits").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/produits/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/produits").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/produits/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/produits").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/produits/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/api/produits").hasAnyRole("ADMIN", "CLIENT")
                        .pathMatchers(HttpMethod.GET, "/api/produits/**").hasAnyRole("ADMIN", "CLIENT")
                        .pathMatchers(HttpMethod.GET, "/catalog/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/commandes").hasRole("CLIENT")
                        .pathMatchers(HttpMethod.POST, "/api/commandes/**").hasRole("CLIENT")
                        .pathMatchers(HttpMethod.GET, "/api/commandes/client/**").hasRole("CLIENT")
                        .pathMatchers(HttpMethod.GET, "/api/commandes").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/api/commandes/**").hasAnyRole("ADMIN", "CLIENT")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtAuthenticationConverter delegate = new JwtAuthenticationConverter();
        delegate.setJwtGrantedAuthoritiesConverter(this::extractRoles);
        return jwt -> Mono.justOrEmpty(delegate.convert(jwt));
    }

    private Collection<GrantedAuthority> extractRoles(Jwt jwt) {
        Set<String> roleNames = new HashSet<>();

        Object realmAccess = jwt.getClaim("realm_access");
        if (realmAccess instanceof Map<?, ?> realmAccessMap) {
            Object roles = realmAccessMap.get("roles");
            if (roles instanceof Collection<?> roleList) {
                roleList.forEach(role -> roleNames.add(role.toString()));
            }
        }

        Object resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess instanceof Map<?, ?> resourceAccessMap) {
            for (Object value : resourceAccessMap.values()) {
                if (value instanceof Map<?, ?> clientMap) {
                    Object roles = clientMap.get("roles");
                    if (roles instanceof Collection<?> roleList) {
                        roleList.forEach(role -> roleNames.add(role.toString()));
                    }
                }
            }
        }

        return roleNames.stream()
                .map(Object::toString)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
    }
}
