package net.redone.commande;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import net.redone.commande.entities.Commande;
import net.redone.commande.entities.CommandeItem;
import net.redone.commande.repositories.CommandeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class CommandeApplication {

	public static void main(String[] args) {
		SpringApplication.run(CommandeApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	CommandLineRunner seedCommandes(CommandeRepository commandeRepository) {
		return args -> {
			if (commandeRepository.count() > 0) {
				return;
			}
			List<Commande> commandes = new ArrayList<>();
			for (int i = 1; i <= 20; i++) {
				Commande commande = new Commande();
				commande.setClientId("client-" + ((i % 5) + 1));
				commande.setOrderDate(LocalDateTime.now().minusDays(20 - i));
				commande.setStatus("VALIDATED");

				List<CommandeItem> items = new ArrayList<>();
				CommandeItem item1 = new CommandeItem();
				item1.setProductId((long) ((i % 10) + 1));
				item1.setQuantity((i % 3) + 1);
				item1.setPrice(new BigDecimal("49.99"));
				item1.setCommande(commande);
				items.add(item1);

				CommandeItem item2 = new CommandeItem();
				item2.setProductId((long) (((i + 3) % 10) + 1));
				item2.setQuantity(1);
				item2.setPrice(new BigDecimal("19.90"));
				item2.setCommande(commande);
				items.add(item2);

				BigDecimal total = item1.getPrice().multiply(BigDecimal.valueOf(item1.getQuantity()))
						.add(item2.getPrice().multiply(BigDecimal.valueOf(item2.getQuantity())));
				commande.setTotalAmount(total);
				commande.setItems(items);

				commandes.add(commande);
			}
			commandeRepository.saveAll(commandes);
		};
	}
}
