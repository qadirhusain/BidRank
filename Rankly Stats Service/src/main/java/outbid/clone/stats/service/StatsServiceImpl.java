package outbid.clone.stats.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import outbid.clone.stats.client.ActivityClient;
import outbid.clone.stats.client.CategoryClient;
import outbid.clone.stats.client.ProductClient;
import outbid.clone.stats.dto.ActivityTotalsResponse;
import outbid.clone.stats.dto.StatsResponse;
import outbid.clone.stats.exception.DownstreamServiceException;
import outbid.clone.stats.repository.OnlineVisitorRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Supplier;

@Service
public class StatsServiceImpl implements StatsService {

    private static final Logger log = LoggerFactory.getLogger(StatsServiceImpl.class);

    private final ProductClient productClient;
    private final CategoryClient categoryClient;
    private final ActivityClient activityClient;
    private final OnlineVisitorRepository onlineVisitorRepository;
    private final Clock clock;
    private final Duration onlineWindow;

    public StatsServiceImpl(
            ProductClient productClient,
            CategoryClient categoryClient,
            ActivityClient activityClient,
            OnlineVisitorRepository onlineVisitorRepository,
            Clock clock,
            @Value("${stats.online-window-seconds:60}") long onlineWindowSeconds
    ) {
        this.productClient = productClient;
        this.categoryClient = categoryClient;
        this.activityClient = activityClient;
        this.onlineVisitorRepository = onlineVisitorRepository;
        this.clock = clock;
        this.onlineWindow = Duration.ofSeconds(onlineWindowSeconds);
    }


    @Override
    public StatsResponse getStats() {

        Long products = orNull("Product", productClient::getProductCount);
        Long categories = orNull("Category", categoryClient::getCategoryCount);
        ActivityTotalsResponse activity = orNull("Activity", activityClient::getTotals);

        return new StatsResponse(
                products,
                categories,
                activity == null ? null : activity.getTotalClicks(),
                activity == null ? null : activity.getTotalViews(),
                getOnlineUserCount(),
                LocalDateTime.now(clock));
    }

    @Override
    public void heartbeat(String visitorId) {
        onlineVisitorRepository.touch(visitorId, LocalDateTime.now(clock));
    }

    @Override
    public long getOnlineUserCount() {
        return onlineVisitorRepository.countByLastSeenAfter(
                LocalDateTime.now(clock).minus(onlineWindow));
    }

    private <T> T orNull(String service, Supplier<T> call) {
        try {
            return call.get();
        } catch (DownstreamServiceException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            log.warn("{} service call failed: {}", service, cause.getMessage());
            return null;
        }
    }
}