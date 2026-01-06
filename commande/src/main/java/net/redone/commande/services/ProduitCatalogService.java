package net.redone.commande.services;

import net.redone.commande.dtos.ProductSnapshot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class ProduitCatalogService {

    private final RestTemplate restTemplate;

    @Value("${produit.base-url:http://localhost:8081}")
    private String produitBaseUrl;

    public ProduitCatalogService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ProductSnapshot getProduct(Long productId, String authorization) {
        HttpHeaders headers = new HttpHeaders();
        if (authorization != null && !authorization.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<ProductSnapshot> response = restTemplate.exchange(
                    produitBaseUrl + "/api/produits/{id}",
                    HttpMethod.GET,
                    entity,
                    ProductSnapshot.class,
                    productId
            );
            ProductSnapshot body = response.getBody();
            if (body == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Empty response from produit service");
            }
            return body;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Produit service unavailable");
        }
    }
}
