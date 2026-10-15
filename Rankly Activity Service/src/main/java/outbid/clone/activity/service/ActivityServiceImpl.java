package outbid.clone.activity.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import outbid.clone.activity.client.ProductClient;
import outbid.clone.activity.dto.ActivityStatsResponse;
import outbid.clone.activity.dto.ActivityTotalsResponse;
import outbid.clone.activity.entity.ActivityEvent;
import outbid.clone.activity.entity.ActivityType;
import outbid.clone.activity.entity.ProductActivityStats;
import outbid.clone.activity.repository.ActivityEventRepository;
import outbid.clone.activity.repository.ProductActivityStatsRepository;
import outbid.clone.activity.util.VisitorHasher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ActivityServiceImpl implements ActivityService {

    private static final int MAX_ATTEMPTS = 3;
    private static final int MAX_KNOWN_PRODUCTS = 10_000;
    private static final Pattern BOT_PATTERN = Pattern.compile(
            "bot|crawl|spider|slurp|curl|wget|python-requests|httpclient|headless",
            Pattern.CASE_INSENSITIVE);

    private final ActivityEventRepository eventRepository;
    private final ProductActivityStatsRepository statsRepository;
    private final ProductClient productClient;
    private final VisitorHasher visitorHasher;
    private final TransactionTemplate transactionTemplate;
    private final ZoneId zone;

    private final Set<UUID> knownProducts = ConcurrentHashMap.newKeySet();

    public ActivityServiceImpl(
            ActivityEventRepository eventRepository,
            ProductActivityStatsRepository statsRepository,
            ProductClient productClient,
            VisitorHasher visitorHasher,
            PlatformTransactionManager transactionManager,
            @Value("${activity.zone:Asia/Kolkata}") String zone
    ) {
        this.eventRepository = eventRepository;
        this.statsRepository = statsRepository;
        this.productClient = productClient;
        this.visitorHasher = visitorHasher;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.zone = ZoneId.of(zone);
    }

    @Override
    public void recordActivity(ActivityType type, UUID productId, String ipAddress, String userAgent) {

        if (isBot(userAgent)) {
            return;
        }

        ensureProductExists(productId);

        String visitorHash = visitorHasher.hash(ipAddress, userAgent);
        LocalDateTime now = LocalDateTime.now(zone);
        LocalDate today = now.toLocalDate();

        for (int attempt = 1; ; attempt++) {
            try {
                transactionTemplate.executeWithoutResult(
                        status -> countEvent(type, productId, visitorHash, today, now));
                return;
            } catch (DataIntegrityViolationException e) {
                // The unique key rejected the event: this visitor already counted today
                if (eventRepository.existsByProductIdAndActivityTypeAndVisitorHashAndEventDate(
                        productId, type, visitorHash, today)) {
                    return;
                }
                // Otherwise two first events raced to create the stats row; try again
                if (attempt >= MAX_ATTEMPTS) {
                    throw e;
                }
            }
        }
    }

    public void countEvent(
            ActivityType type, UUID productId, String visitorHash,
            LocalDate today, LocalDateTime now
    ) {
        eventRepository.saveAndFlush(ActivityEvent.builder()
                .productId(productId)
                .activityType(type)
                .visitorHash(visitorHash)
                .eventDate(today)
                .build());

        int updated = type == ActivityType.CLICK
                ? statsRepository.incrementClicks(productId, now)
                : statsRepository.incrementViews(productId);

        if (updated == 0) {
            statsRepository.saveAndFlush(ProductActivityStats.builder()
                    .productId(productId)
                    .clickCount(type == ActivityType.CLICK ? 1L : 0L)
                    .viewCount(type == ActivityType.VIEW ? 1L : 0L)
                    .lastClickedAt(type == ActivityType.CLICK ? now : null)
                    .build());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityStatsResponse getStats(UUID productId) {

        return statsRepository.findById(productId)
                .map(this::toResponse)
                .orElseGet(() -> emptyResponse(productId));
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityTotalsResponse getTotals() {
        return ActivityTotalsResponse.builder()
                .totalClicks(eventRepository.countByActivityType(ActivityType.CLICK))
                .totalViews(eventRepository.countByActivityType(ActivityType.VIEW))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityStatsResponse> getStatsByIds(List<UUID> productIds) {

        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }

        Map<UUID, ProductActivityStats> statsById = statsRepository
                .findByProductIdIn(productIds)
                .stream()
                .collect(Collectors.toMap(ProductActivityStats::getProductId, Function.identity()));

        return productIds.stream()
                .map(id -> {
                    ProductActivityStats stats = statsById.get(id);
                    return stats == null ? emptyResponse(id) : toResponse(stats);
                })
                .toList();
    }

    private void ensureProductExists(UUID productId) {
        if (knownProducts.contains(productId)) {
            return;
        }

        productClient.validateProduct(productId);

        if (knownProducts.size() >= MAX_KNOWN_PRODUCTS) {
            knownProducts.clear();
        }
        knownProducts.add(productId);
    }

    private boolean isBot(String userAgent) {
        return userAgent == null
                || userAgent.isBlank()
                || BOT_PATTERN.matcher(userAgent).find();
    }

    private ActivityStatsResponse toResponse(ProductActivityStats stats) {
        return ActivityStatsResponse.builder()
                .productId(stats.getProductId())
                .clickCount(stats.getClickCount())
                .viewCount(stats.getViewCount())
                .lastClickedAt(stats.getLastClickedAt())
                .build();
    }

    private ActivityStatsResponse emptyResponse(UUID productId) {
        return ActivityStatsResponse.builder()
                .productId(productId)
                .clickCount(0L)
                .viewCount(0L)
                .build();
    }
}