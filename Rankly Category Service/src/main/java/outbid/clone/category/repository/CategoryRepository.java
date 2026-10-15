package outbid.clone.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import outbid.clone.category.entity.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByIsActiveTrueOrderByDisplayOrderAscNameAsc();

    Optional<Category> findBySlugAndIsActiveTrue(String slug);

    Optional<Category> findByIdAndIsActiveTrue(UUID id);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    long countByIsActiveTrue();
}
