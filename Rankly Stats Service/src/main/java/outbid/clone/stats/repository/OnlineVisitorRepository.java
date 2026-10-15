package outbid.clone.stats.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import outbid.clone.stats.entity.OnlineVisitor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface OnlineVisitorRepository extends JpaRepository<OnlineVisitor, String> {

    long countByLastSeenAfter(LocalDateTime time);

    @Transactional
    @Modifying
    @Query(value = """
            INSERT INTO online_visitors (visitor_id, last_seen)
            VALUES (:visitorId, :now)
            ON DUPLICATE KEY UPDATE last_seen = :now
            """, nativeQuery = true)
    void touch(@Param("visitorId") String visitorId,
               @Param("now") LocalDateTime now);

    @Transactional
    @Modifying
    @Query("delete from OnlineVisitor v where v.lastSeen < :cutoff")
    int deleteInactive(@Param("cutoff") LocalDateTime cutoff);
}