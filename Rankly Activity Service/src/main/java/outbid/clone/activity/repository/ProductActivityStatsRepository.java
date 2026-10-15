package outbid.clone.activity.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import outbid.clone.activity.entity.ProductActivityStats;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ProductActivityStatsRepository extends JpaRepository<ProductActivityStats, UUID> {

    List<ProductActivityStats> findByProductIdIn(Collection<UUID> productIds);

    @Modifying
    @Query("""
            update ProductActivityStats s
            set s.clickCount = s.clickCount + 1, s.lastClickedAt = :now
            where s.productId = :productId
            """)
    int incrementClicks(@Param("productId") UUID productId,
                        @Param("now") LocalDateTime now);

    @Modifying
    @Query("""
            update ProductActivityStats s
            set s.viewCount = s.viewCount + 1
            where s.productId = :productId
            """)
    int incrementViews(@Param("productId") UUID productId);
}