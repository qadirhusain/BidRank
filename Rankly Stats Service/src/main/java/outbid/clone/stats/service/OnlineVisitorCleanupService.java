package outbid.clone.stats.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import outbid.clone.stats.repository.OnlineVisitorRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@Slf4j
public class OnlineVisitorCleanupService {

    private final OnlineVisitorRepository onlineVisitorRepository;
    private final Clock clock;
    private final Duration onlineWindow;

    public OnlineVisitorCleanupService(
            OnlineVisitorRepository onlineVisitorRepository,
            Clock clock,
            @Value("${stats.online-window-seconds:60}") long onlineWindowSeconds
    ) {
        this.onlineVisitorRepository = onlineVisitorRepository;
        this.clock = clock;
        this.onlineWindow = Duration.ofSeconds(onlineWindowSeconds);
    }

    @Scheduled(fixedRateString = "${stats.cleanup-interval-ms:60000}")
    public void cleanupInactiveVisitors() {

        int removed = onlineVisitorRepository.deleteInactive(
                LocalDateTime.now(clock).minus(onlineWindow));

        if (removed > 0) {
            log.debug("Removed {} inactive visitors", removed);
        }
    }
}