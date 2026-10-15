package outbid.clone.product.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import outbid.clone.product.entity.Product;
import outbid.clone.product.entity.ProductStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    Page<Product> findByCategoryIdAndStatus(UUID categoryId, ProductStatus status, Pageable pageable);

    Optional<Product> findByIdAndStatus(UUID id, ProductStatus status);

    Optional<Product> findByCanonicalUrlAndStatus(String canonicalUrl, ProductStatus status);

    List<Product> findByIdInAndStatus(Collection<UUID> ids, ProductStatus status);

    boolean existsByCanonicalUrl(String canonicalUrl);

    boolean existsByCanonicalUrlAndIdNot(String canonicalUrl, UUID id);

    long countByStatus(ProductStatus status);
}
