package net.redone.produit.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import net.redone.produit.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ProductImageController {

    private static final Logger logger = LoggerFactory.getLogger(ProductImageController.class);

    private final ProductRepository productRepository;
    private final Path imageDir;

    public ProductImageController(
            ProductRepository productRepository,
            @Value("${produit.image-dir:./catalog}") String imageDir
    ) {
        this.productRepository = productRepository;
        this.imageDir = resolveImageDir(imageDir);
        try {
            Files.createDirectories(this.imageDir);
            logger.info("Product images directory: {}", this.imageDir.toAbsolutePath());
        } catch (IOException ex) {
            logger.warn("Unable to create product image directory {}: {}", this.imageDir, ex.getMessage());
        }
    }

    @PostMapping(value = "/api/produits/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is required");
        }
        String contentType = file.getContentType();
        if (contentType == null
                || (!contentType.equalsIgnoreCase(MediaType.IMAGE_JPEG_VALUE)
                && !contentType.equalsIgnoreCase("image/jpg")
                && !contentType.equalsIgnoreCase("image/pjpeg"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only JPG images are supported");
        }

        try {
            saveImage(file, imageDir.resolve(id + ".jpg"));
        } catch (IOException ex) {
            Path fallbackDir = locateFrontendCatalog();
            if (fallbackDir != null && !fallbackDir.equals(imageDir)) {
                try {
                    saveImage(file, fallbackDir.resolve(id + ".jpg"));
                    return ResponseEntity.noContent().build();
                } catch (IOException fallbackEx) {
                    logger.error("Failed to save product image to fallback dir {}: {}",
                            fallbackDir, fallbackEx.getMessage());
                    throw new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Failed to save image: " + fallbackEx.getMessage()
                    );
                }
            }
            logger.error("Failed to save product image to {}: {}", imageDir, ex.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to save image: " + ex.getMessage()
            );
        } catch (RuntimeException ex) {
            logger.error("Unexpected error saving product image to {}: {}", imageDir, ex.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to save image: " + ex.getMessage()
            );
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/catalog/{id}.jpg", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<Resource> getImage(@PathVariable Long id) {
        Path imagePath = imageDir.resolve(id + ".jpg");
        Resource resource = new FileSystemResource(imagePath);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(resource);
    }

    private Path resolveImageDir(String configured) {
        Path configuredPath = Path.of(configured);
        if (configuredPath.isAbsolute()) {
            return configuredPath.normalize();
        }
        Path repoRoot = findRepoRoot();
        if (repoRoot != null) {
            return repoRoot.resolve(configuredPath).normalize();
        }
        Path cwd = Path.of(System.getProperty("user.dir"));
        return cwd.resolve(configuredPath).normalize();
    }

    private void saveImage(MultipartFile file, Path target) throws IOException {
        Files.createDirectories(target.getParent());
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private Path locateFrontendCatalog() {
        Path cwd = Path.of(System.getProperty("user.dir"));
        Path current = cwd;
        while (current != null) {
            Path candidate = current.resolve("frontend/src/assets/catalog").normalize();
            if (Files.exists(candidate) || Files.exists(candidate.getParent())) {
                return candidate;
            }
            current = current.getParent();
        }
        return null;
    }

    private Path findRepoRoot() {
        Path current = Path.of(System.getProperty("user.dir"));
        while (current != null) {
            if (Files.exists(current.resolve(".git"))) {
                return current;
            }
            current = current.getParent();
        }
        return null;
    }
}
