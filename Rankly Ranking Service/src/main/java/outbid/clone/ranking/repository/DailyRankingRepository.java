package outbid.clone.ranking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import outbid.clone.ranking.entity.DailyRanking;

import java.time.LocalDate;
import java.util.*;

public interface DailyRankingRepository extends JpaRepository<DailyRanking, UUID> {

    Optional<DailyRanking> findByProductIdAndBidDate(UUID productId, LocalDate bidDate);

    Page<DailyRanking> findByBidDateOrderByAmountDescLastBidAtAsc(
            LocalDate bidDate, Pageable pageable);

    Page<DailyRanking> findByBidDateAndCategoryIdOrderByAmountDescLastBidAtAsc(
            LocalDate bidDate, UUID categoryId, Pageable pageable);

    List<DailyRanking> findByBidDateAndProductIdIn(
            LocalDate bidDate, Collection<UUID> productIds);
}
