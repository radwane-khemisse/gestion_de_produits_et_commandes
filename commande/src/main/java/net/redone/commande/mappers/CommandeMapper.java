package net.redone.commande.mappers;

import java.math.BigDecimal;
import java.util.List;
import net.redone.commande.dtos.CommandeItemResponse;
import net.redone.commande.dtos.CommandeResponse;
import net.redone.commande.entities.Commande;
import net.redone.commande.entities.CommandeItem;
import org.springframework.stereotype.Component;

@Component
public class CommandeMapper {

    public CommandeResponse toResponse(Commande commande) {
        List<CommandeItemResponse> items = commande.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();
        return new CommandeResponse(
                commande.getId(),
                commande.getClientId(),
                commande.getOrderDate(),
                commande.getStatus(),
                commande.getTotalAmount(),
                items
        );
    }

    private CommandeItemResponse toItemResponse(CommandeItem item) {
        BigDecimal lineTotal = item.getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));
        return new CommandeItemResponse(
                item.getProductId(),
                item.getQuantity(),
                item.getPrice(),
                lineTotal
        );
    }
}
