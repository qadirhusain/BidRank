package outbid.clone.product.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import outbid.clone.product.client.CategoryClient;
import outbid.clone.product.dto.ProductRequest;
import outbid.clone.product.dto.ProductResponse;
import outbid.clone.product.entity.ListingType;
import outbid.clone.product.entity.Product;
import outbid.clone.product.entity.ProductStatus;
import outbid.clone.product.exception.DuplicateProductException;
import outbid.clone.product.exception.ProductNotFoundException;
import outbid.clone.product.repository.ProductRepository;
import outbid.clone.product.util.UrlNormalizer;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final CategoryClient categoryClient;
    private final UrlNormalizer urlNormalizer;

    @Override
    public Page<ProductResponse> getAllProducts(Pageable pageable) {

        return productRepository
                .findByStatus(ProductStatus.ACTIVE, pageable)
                .map(this::toResponse);
    }

    @Override
    public ProductResponse getProductById(UUID id) {

        return toResponse(findActiveById(id));
    }

    @Override
    public ProductResponse getProductByUrl(ListingType listingType, String url) {

        String canonicalUrl = urlNormalizer.normalize(listingType, url);

        Product product = productRepository
                .findByCanonicalUrlAndStatus(canonicalUrl, ProductStatus.ACTIVE)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product not found with url: " + url));

        return toResponse(product);
    }

    @Override
    public Page<ProductResponse> getProductsByCategoryId(UUID categoryId, Pageable pageable) {

        return productRepository
                .findByCategoryIdAndStatus(categoryId, ProductStatus.ACTIVE, pageable)
                .map(this::toResponse);
    }

    @Override
    public List<ProductResponse> getProductsByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        Map<UUID, Product> productsById = productRepository
                .findByIdInAndStatus(ids, ProductStatus.ACTIVE)
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        return ids.stream()
                .map(productsById::get)
                .filter(Objects::nonNull)
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {

        categoryClient.validateCategory(request.getCategoryId());

        String canonicalUrl = urlNormalizer.normalize(request.getListingType(), request.getUrl());

        if (productRepository.existsByCanonicalUrl(canonicalUrl)) {
            throw new DuplicateProductException(
                    "Product already exists for url: " + request.getUrl());
        }

        Product product = modelMapper.map(request, Product.class);
        product.setCanonicalUrl(canonicalUrl);

        return toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(UUID id, ProductRequest request) {

        Product product = findActiveById(id);

        categoryClient.validateCategory(request.getCategoryId());

        String canonicalUrl = urlNormalizer.normalize(request.getListingType(), request.getUrl());
        if (productRepository.existsByCanonicalUrlAndIdNot(canonicalUrl, id)) {
            throw new DuplicateProductException(
                    "Product already exists for url: " + request.getUrl());
        }

        modelMapper.map(request, product);
        product.setCanonicalUrl(canonicalUrl);

        return toResponse(productRepository.save(product));
    }

    @Override
    public void deleteProduct(UUID id) {

        Product product = findActiveById(id);
        product.setStatus(ProductStatus.REMOVED);
        productRepository.save(product);
    }

    @Override
    public long getProductCount() {
        return productRepository.countByStatus(ProductStatus.ACTIVE);
    }

    private Product findActiveById(UUID id) {
        return productRepository.findByIdAndStatus(id, ProductStatus.ACTIVE)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product not found with id: " + id));
    }

    private ProductResponse toResponse(Product product) {
        return modelMapper.map(product, ProductResponse.class);
    }
}