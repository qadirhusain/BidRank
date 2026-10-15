package outbid.clone.stats.service;

import outbid.clone.stats.dto.StatsResponse;

public interface StatsService {

    /** Numbers for the live stats page. A count is null if its service is unreachable. */
    StatsResponse getStats();

    /** Marks the visitor as online right now. */
    void heartbeat(String visitorId);

    /** Visitors who sent a heartbeat within the online window. */
    long getOnlineUserCount();
}