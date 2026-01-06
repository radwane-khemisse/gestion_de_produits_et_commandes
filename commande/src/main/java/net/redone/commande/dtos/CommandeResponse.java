package net.redone.commande.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommandeResponse {

    private Long id;
    private String clientId;
    private LocalDateTime orderDate;
    private String status;
    private BigDecimal totalAmount;
    private List<CommandeItemResponse> items;
}
