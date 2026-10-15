package outbid.clone.product.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import outbid.clone.product.dto.ProductRequest;
import outbid.clone.product.dto.ProductResponse;
import outbid.clone.product.entity.ListingType;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    Page<ProductResponse> getAllProducts(Pageable pageable);

    ProductResponse getProductById(UUID id);

    ProductResponse getProductByUrl(ListingType listingType, String url);

    Page<ProductResponse> getProductsByCategoryId(UUID categoryId, Pageable pageable);

    List<ProductResponse> getProductsByIds(List<UUID> ids);

    ProductResponse createProduct(ProductRequest request);

    ProductResponse updateProduct(UUID id, ProductRequest request);

    void deleteProduct(UUID id);

    long getProductCount();
}
