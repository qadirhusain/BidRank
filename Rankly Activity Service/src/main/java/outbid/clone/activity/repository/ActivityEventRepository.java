package outbid.clone.activity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import outbid.clone.activity.entity.ActivityEvent;
import outbid.clone.activity.entity.ActivityType;

import java.time.LocalDate;
import java.util.UUID;

public interface ActivityEventRepository extends JpaRepository<ActivityEvent, UUID> {

    boolean existsByProductIdAndActivityTypeAndVisitorHashAndEventDate(
            UUID productId,
            ActivityType activityType,
            String visitorHash,
            LocalDate eventDate
    );

    long countByActivityType(ActivityType activityType);
}
