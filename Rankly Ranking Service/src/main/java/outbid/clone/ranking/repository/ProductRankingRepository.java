package outbid.clone.ranking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import outbid.clone.ranking.entity.ProductRanking;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface ProductRankingRepository extends JpaRepository<ProductRanking, UUID> {

    Page<ProductRanking> findAllByOrderByTotalAmountDescLastBidAtAsc(Pageable pageable);

    Page<ProductRanking> findByCategoryIdOrderByTotalAmountDescLastBidAtAsc(
            UUID categoryId, Pageable pageable);

    Optional<ProductRanking> findFirstByOrderByTotalAmountDescLastBidAtAsc();

    @Query("""
            select count(p) from ProductRanking p
            where p.totalAmount > :amount
               or (p.totalAmount = :amount and p.lastBidAt < :lastBidAt)
            """)
    long countAhead(@Param("amount") Long amount,
                    @Param("lastBidAt") LocalDateTime lastBidAt);

    @Query("""
            select count(p) from ProductRanking p
            where p.categoryId = :categoryId
              and (p.totalAmount > :amount
                   or (p.totalAmount = :amount and p.lastBidAt < :lastBidAt))
            """)
    long countAheadInCategory(@Param("categoryId") UUID categoryId,
                              @Param("amount") Long amount,
                              @Param("lastBidAt") LocalDateTime lastBidAt);
}