package outbid.clone.ranking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import outbid.clone.ranking.entity.Bid;

import java.util.Optional;
import java.util.UUID;

public interface BidRepository extends JpaRepository<Bid, UUID> {

    Optional<Bid> findByPaymentId(UUID paymentId);

    boolean existsByPaymentId(UUID paymentId);
}
