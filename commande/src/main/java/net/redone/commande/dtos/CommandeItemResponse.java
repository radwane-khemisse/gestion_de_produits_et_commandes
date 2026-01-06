package net.redone.commande.dtos;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommandeItemResponse {

    private Long productId;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal lineTotal;
}
