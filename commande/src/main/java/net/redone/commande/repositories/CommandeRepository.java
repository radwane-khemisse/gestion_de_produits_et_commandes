package net.redone.commande.repositories;

import java.util.List;
import net.redone.commande.entities.Commande;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommandeRepository extends JpaRepository<Commande, Long> {

    List<Commande> findByClientId(String clientId);
}
