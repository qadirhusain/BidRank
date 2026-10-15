package outbid.clone.product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import outbid.clone.product.dto.ProductRequest;
import outbid.clone.product.dto.ProductResponse;
import outbid.clone.product.entity.ListingType;
import outbid.clone.product.service.ProductService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private static final int MAX_IDS_PER_LOOKUP = 100;
    private final ProductService productService;

    @GetMapping
    public Page<ProductResponse> getAllProducts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return productService.getAllProducts(pageable);
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable UUID id) {
        return productService.getProductById(id);
    }

    @GetMapping("/by-url")
    public ProductResponse getProductByUrl(
            @RequestParam ListingType listingType,
            @RequestParam String url
    ) {
        return productService.getProductByUrl(listingType, url);
    }

    @GetMapping("/category/{categoryId}")
    public Page<ProductResponse> getProductsByCategory(
            @PathVariable UUID categoryId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return productService.getProductsByCategoryId(categoryId, pageable);
    }

    @GetMapping("/count")
    public long getProductCount() {
        return productService.getProductCount();
    }

    @PostMapping("/by-ids")
    public List<ProductResponse> getProductsByIds(@RequestBody List<UUID> ids) {
        if (ids != null && ids.size() > MAX_IDS_PER_LOOKUP) {
            throw new IllegalArgumentException(
                    "A maximum of " + MAX_IDS_PER_LOOKUP + " ids is allowed per request");
        }
        return productService.getProductsByIds(ids);
    }

    // PUT /api/categories/{id}
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request
    ) {

        ProductResponse product = productService.createProduct(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest request
    ) {

        ProductResponse product = productService.updateProduct(id, request);

        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(
            @PathVariable UUID id
    ) {

        productService.deleteProduct(id);

        return ResponseEntity.noContent().build();
    }

}
