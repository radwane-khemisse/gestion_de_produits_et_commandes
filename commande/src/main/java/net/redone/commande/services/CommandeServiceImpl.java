package net.redone.commande.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.redone.commande.dtos.CommandeCreateRequest;
import net.redone.commande.dtos.CommandeResponse;
import net.redone.commande.dtos.ProductSnapshot;
import net.redone.commande.entities.Commande;
import net.redone.commande.entities.CommandeItem;
import net.redone.commande.mappers.CommandeMapper;
import net.redone.commande.repositories.CommandeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
@RequiredArgsConstructor
public class CommandeServiceImpl implements CommandeService {

    private final CommandeRepository commandeRepository;
    private final CommandeMapper commandeMapper;
    private final ProduitCatalogService produitCatalogService;

    @Override
    public CommandeResponse create(String authorization, CommandeCreateRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order must contain items");
        }
        if (request.getClientId() == null || request.getClientId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Client id is required");
        }

        Commande commande = new Commande();
        commande.setClientId(request.getClientId());
        commande.setOrderDate(LocalDateTime.now());
        commande.setStatus("VALIDATED");

        List<CommandeItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (var itemRequest : request.getItems()) {
            ProductSnapshot product = produitCatalogService.getProduct(itemRequest.getProductId(), authorization);
            if (product.getQuantity() == null || product.getQuantity() < itemRequest.getQuantity()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Insufficient stock for product " + product.getId()
                );
            }
            BigDecimal price = product.getPrice();
            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(lineTotal);

            CommandeItem item = new CommandeItem();
            item.setProductId(product.getId());
            item.setQuantity(itemRequest.getQuantity());
            item.setPrice(price);
            item.setCommande(commande);
            items.add(item);
        }

        commande.setItems(items);
        commande.setTotalAmount(totalAmount);

        Commande saved = commandeRepository.save(commande);
        return commandeMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommandeResponse> findAll() {
        return commandeRepository.findAll()
                .stream()
                .map(commandeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommandeResponse> findByClientId(String clientId) {
        return commandeRepository.findByClientId(clientId)
                .stream()
                .map(commandeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CommandeResponse findById(Long id) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        return commandeMapper.toResponse(commande);
    }
}
