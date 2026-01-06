package net.redone.produit;

import java.math.BigDecimal;
import java.util.List;
import net.redone.produit.entities.Product;
import net.redone.produit.repositories.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ProduitApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProduitApplication.class, args);
    }

//    @Bean
//    CommandLineRunner seedProducts(ProductRepository productRepository) {
//        return args -> {
//            if (productRepository.count() > 0) {
//                return;
//            }
//            List<Product> products = List.of(
//                    new Product(null, "Laptop Pro 14", "Portable workstation 14 inch", new BigDecimal("1299.00"), 15),
//                    new Product(null, "Wireless Mouse", "Ergonomic wireless mouse", new BigDecimal("29.90"), 120),
//                    new Product(null, "Mechanical Keyboard", "Compact mechanical keyboard", new BigDecimal("89.50"), 60),
//                    new Product(null, "USB-C Hub", "6-in-1 USB-C hub", new BigDecimal("49.99"), 80),
//                    new Product(null, "27-inch Monitor", "IPS 2K monitor", new BigDecimal("249.00"), 25),
//                    new Product(null, "External SSD 1TB", "Portable SSD USB 3.2", new BigDecimal("119.00"), 40),
//                    new Product(null, "Noise Canceling Headphones", "Over-ear ANC headphones", new BigDecimal("199.00"), 30),
//                    new Product(null, "Webcam 1080p", "Full HD webcam", new BigDecimal("59.00"), 55),
//                    new Product(null, "Desk Lamp", "LED lamp with dimmer", new BigDecimal("39.00"), 70),
//                    new Product(null, "Smartphone Stand", "Adjustable metal stand", new BigDecimal("14.90"), 150)
//            );
//            productRepository.saveAll(products);
//        };
//    }
}
