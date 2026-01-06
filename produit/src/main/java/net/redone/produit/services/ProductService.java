package net.redone.produit.services;

import java.util.List;
import net.redone.produit.dtos.ProductCreateRequest;
import net.redone.produit.dtos.ProductResponse;
import net.redone.produit.dtos.ProductUpdateRequest;

public interface ProductService {

    ProductResponse create(ProductCreateRequest request);

    ProductResponse update(Long id, ProductUpdateRequest request);

    void delete(Long id);

    List<ProductResponse> findAll();

    ProductResponse findById(Long id);
}
