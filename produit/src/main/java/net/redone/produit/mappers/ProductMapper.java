package net.redone.produit.mappers;

import net.redone.produit.dtos.ProductCreateRequest;
import net.redone.produit.dtos.ProductResponse;
import net.redone.produit.dtos.ProductUpdateRequest;
import net.redone.produit.entities.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(ProductCreateRequest request) {
        return new Product(
                null,
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getQuantity()
        );
    }

    public void updateEntity(ProductUpdateRequest request, Product product) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
    }

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity()
        );
    }
}
