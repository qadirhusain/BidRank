package outbid.clone.activity.service;

import outbid.clone.activity.dto.ActivityStatsResponse;
import outbid.clone.activity.dto.ActivityTotalsResponse;
import outbid.clone.activity.entity.ActivityType;

import java.util.List;
import java.util.UUID;

public interface ActivityService {

    /**
     * Counts a click or a view. A visitor counts once per product, type and day;
     * repeats and bot traffic are ignored silently.
     */
    void recordActivity(ActivityType type, UUID productId, String ipAddress, String userAgent);

    /** Totals for one product. Returns zeros if the product has no activity yet. */
    ActivityStatsResponse getStats(UUID productId);

    /** Totals for many products, in the same order as the given ids. */
    List<ActivityStatsResponse> getStatsByIds(List<UUID> productIds);

    ActivityTotalsResponse getTotals();
}