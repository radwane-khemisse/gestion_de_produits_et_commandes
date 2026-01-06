package net.redone.commande.services;

import java.util.List;
import net.redone.commande.dtos.CommandeCreateRequest;
import net.redone.commande.dtos.CommandeResponse;

public interface CommandeService {

    CommandeResponse create(String authorization, CommandeCreateRequest request);

    List<CommandeResponse> findAll();

    List<CommandeResponse> findByClientId(String clientId);

    CommandeResponse findById(Long id);
}
