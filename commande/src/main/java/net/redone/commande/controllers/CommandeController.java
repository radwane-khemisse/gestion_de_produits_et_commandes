package net.redone.commande.controllers;

import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.redone.commande.dtos.CommandeCreateRequest;
import net.redone.commande.dtos.CommandeResponse;
import net.redone.commande.services.CommandeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/commandes")
@RequiredArgsConstructor
public class CommandeController {

    private final CommandeService commandeService;

    @PostMapping
    public ResponseEntity<CommandeResponse> create(
            @RequestBody CommandeCreateRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @AuthenticationPrincipal Jwt jwt
    ) {
        if ((request.getClientId() == null || request.getClientId().isBlank()) && jwt != null) {
            String username = jwt.getClaimAsString("preferred_username");
            request.setClientId(username != null ? username : jwt.getSubject());
        }
        CommandeResponse response = commandeService.create(authorization, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CommandeResponse>> findAll() {
        return ResponseEntity.ok(commandeService.findAll());
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<CommandeResponse>> findByClient(
            @PathVariable String clientId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        if (jwt != null && !hasRole(jwt, "ADMIN")) {
            String username = jwt.getClaimAsString("preferred_username");
            String principalId = username != null ? username : jwt.getSubject();
            if (!clientId.equals(principalId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot access other client orders");
            }
        }
        return ResponseEntity.ok(commandeService.findByClientId(clientId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommandeResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(commandeService.findById(id));
    }

    private boolean hasRole(Jwt jwt, String role) {
        java.util.Set<String> roleNames = new java.util.HashSet<>();

        Object realmAccess = jwt.getClaim("realm_access");
        if (realmAccess instanceof java.util.Map<?, ?> realmAccessMap) {
            Object roles = realmAccessMap.get("roles");
            if (roles instanceof java.util.Collection<?> roleList) {
                roleList.forEach(r -> roleNames.add(r.toString()));
            }
        }

        Object resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess instanceof java.util.Map<?, ?> resourceAccessMap) {
            for (Object value : resourceAccessMap.values()) {
                if (value instanceof java.util.Map<?, ?> clientMap) {
                    Object roles = clientMap.get("roles");
                    if (roles instanceof java.util.Collection<?> roleList) {
                        roleList.forEach(r -> roleNames.add(r.toString()));
                    }
                }
            }
        }

        return roleNames.contains(role);
    }
}
