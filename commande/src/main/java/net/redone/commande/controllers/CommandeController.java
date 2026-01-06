package net.redone.commande.controllers;

import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.redone.commande.dtos.CommandeCreateRequest;
import net.redone.commande.dtos.CommandeResponse;
import net.redone.commande.services.CommandeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/commandes")
@RequiredArgsConstructor
public class CommandeController {

    private final CommandeService commandeService;

    @PostMapping
    public ResponseEntity<CommandeResponse> create(
            @RequestBody CommandeCreateRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
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
    public ResponseEntity<List<CommandeResponse>> findByClient(@PathVariable String clientId) {
        return ResponseEntity.ok(commandeService.findByClientId(clientId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommandeResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(commandeService.findById(id));
    }
}
