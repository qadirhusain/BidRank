package outbid.clone.ranking.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import outbid.clone.ranking.client.ProductClient;
import outbid.clone.ranking.dto.*;
import outbid.clone.ranking.entity.Bid;
import outbid.clone.ranking.entity.DailyRanking;
import outbid.clone.ranking.entity.ProductRanking;
import outbid.clone.ranking.exception.RankingNotFoundException;
import outbid.clone.ranking.repository.BidRepository;
import outbid.clone.ranking.repository.DailyRankingRepository;
import outbid.clone.ranking.repository.ProductRankingRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RankingServiceImpl implements RankingService {

    private static final long RUPEE = 100L;
    private static final int MAX_ATTEMPTS = 3;

    private final BidRepository bidRepository;
    private final ProductRankingRepository productRankingRepository;
    private final DailyRankingRepository dailyRankingRepository;
    private final ProductClient productClient;
    private final TransactionTemplate transactionTemplate;
    private final ZoneId zone;

    public RankingServiceImpl(
            BidRepository bidRepository,
            ProductRankingRepository productRankingRepository,
            DailyRankingRepository dailyRankingRepository,
            ProductClient productClient,
            PlatformTransactionManager transactionManager,
            @Value("${ranking.zone:Asia/Kolkata}") String zone
    ) {
        this.bidRepository = bidRepository;
        this.productRankingRepository = productRankingRepository;
        this.dailyRankingRepository = dailyRankingRepository;
        this.productClient = productClient;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.zone = ZoneId.of(zone);
    }

    @Override
    public RankingResponse recordBid(BidRequest request) {

        validateAmount(request);

        // Idempotency: a retried payment confirmation returns the current standing
        if (bidRepository.existsByPaymentId(request.getPaymentId())) {
            return getProductRanking(request.getProductId());
        }

        // The Product Service is the source of truth for the product's category
        ProductSummary product = productClient.getProduct(request.getProductId());

        for (int attempt = 1; ; attempt++) {
            try {
                return transactionTemplate.execute(
                        status -> applyBid(request, product.getCategoryId()));
            } catch (ObjectOptimisticLockingFailureException
                     | DataIntegrityViolationException e) {

                if (bidRepository.existsByPaymentId(request.getPaymentId())) {
                    return getProductRanking(request.getProductId());
                }
                if (attempt >= MAX_ATTEMPTS) {
                    throw e;
                }
            }
        }
    }

    private RankingResponse applyBid(BidRequest request, UUID categoryId) {
        LocalDateTime now = LocalDateTime.now(zone);
        LocalDate today = now.toLocalDate();

        bidRepository.save(Bid.builder()
                .productId(request.getProductId())
                .categoryId(categoryId)
                .amount(request.getAmount())
                .paymentId(request.getPaymentId())
                .bidDate(today)
                .build());

        ProductRanking ranking = productRankingRepository.findById(request.getProductId())
                .orElseGet(() -> ProductRanking.builder()
                        .productId(request.getProductId())
                        .firstBidAt(now)
                        .build());
        ranking.setCategoryId(categoryId);
        ranking.setTotalAmount(ranking.getTotalAmount() + request.getAmount());
        ranking.setBidCount(ranking.getBidCount() + 1);
        ranking.setLastBidAt(now);
        productRankingRepository.save(ranking);

        DailyRanking daily = dailyRankingRepository
                .findByProductIdAndBidDate(request.getProductId(), today)
                .orElseGet(() -> DailyRanking.builder()
                        .productId(request.getProductId())
                        .bidDate(today)
                        .build());
        daily.setCategoryId(categoryId);
        daily.setAmount(daily.getAmount() + request.getAmount());
        daily.setLastBidAt(now);
        dailyRankingRepository.save(daily);

        return buildStanding(ranking, daily.getAmount());
    }

    private void validateAmount(BidRequest request) {
        if (request.getAmount() % RUPEE != 0) {
            throw new IllegalArgumentException("Amount must be in whole rupees");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RankingResponse> getLeaderboard(UUID categoryId, Pageable pageable) {

        Page<ProductRanking> page = categoryId == null
                ? productRankingRepository.findAllByOrderByTotalAmountDescLastBidAtAsc(pageable)
                : productRankingRepository.findByCategoryIdOrderByTotalAmountDescLastBidAtAsc(categoryId, pageable);

        List<ProductRanking> entries = page.getContent();
        Map<UUID, Long> todayAmounts = todayAmounts(
                entries.stream().map(ProductRanking::getProductId).toList());

        List<RankingResponse> content = new ArrayList<>(entries.size());
        for (int i = 0; i < entries.size(); i++) {
            ProductRanking entry = entries.get(i);
            content.add(RankingResponse.builder()
                    .productId(entry.getProductId())
                    .categoryId(entry.getCategoryId())
                    .rank(position(pageable, i))
                    .categoryRank(categoryId != null ? position(pageable, i) : null)
                    .totalAmount(entry.getTotalAmount())
                    .todayAmount(todayAmounts.getOrDefault(entry.getProductId(), 0L))
                    .bidCount(entry.getBidCount())
                    .lastBidAt(entry.getLastBidAt())
                    .build());
        }

        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Override
    public Page<RankingResponse> getTodayLeaderboard(UUID categoryId, Pageable pageable) {

        return getDailyLeaderboard(today(), categoryId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RankingResponse> getDailyLeaderboard(LocalDate date, UUID categoryId, Pageable pageable) {

        Page<DailyRanking> page = categoryId == null
                ? dailyRankingRepository.findByBidDateOrderByAmountDescLastBidAtAsc(date, pageable)
                : dailyRankingRepository.findByBidDateAndCategoryIdOrderByAmountDescLastBidAtAsc(
                date, categoryId, pageable);

        List<DailyRanking> entries = page.getContent();
        List<RankingResponse> content = new ArrayList<>(entries.size());
        for (int i = 0; i < entries.size(); i++) {
            DailyRanking entry = entries.get(i);
            content.add(RankingResponse.builder()
                    .productId(entry.getProductId())
                    .categoryId(entry.getCategoryId())
                    .rank(position(pageable, i))
                    .todayAmount(entry.getAmount())
                    .lastBidAt(entry.getLastBidAt())
                    .build());
        }

        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public RankingResponse getProductRanking(UUID productId) {

        ProductRanking ranking = productRankingRepository.findById(productId)
                .orElseThrow(() -> new RankingNotFoundException(
                        "No ranking found for product: " + productId));

        long todayAmount = dailyRankingRepository
                .findByProductIdAndBidDate(productId, today())
                .map(DailyRanking::getAmount)
                .orElse(0L);

        return buildStanding(ranking, todayAmount);
    }

    @Override
    @Transactional(readOnly = true)
    public AmountToBeatResponse getAmountToBeat(UUID productId) {
        ProductRanking leader = productRankingRepository
                .findFirstByOrderByTotalAmountDescLastBidAtAsc()
                .orElse(null);

        long leaderAmount = leader == null ? 0L : leader.getTotalAmount();
        long current = productId == null
                ? 0L
                : productRankingRepository.findById(productId)
                .map(ProductRanking::getTotalAmount)
                .orElse(0L);

        boolean alreadyLeader = leader != null && leader.getProductId().equals(productId);

        long needed = alreadyLeader ? 0L : leaderAmount - current + RUPEE;

        return new AmountToBeatResponse(leaderAmount, needed);
    }

    private RankingResponse buildStanding(ProductRanking ranking, long todayAmount) {

        long ahead = productRankingRepository.countAhead(
                ranking.getTotalAmount(), ranking.getLastBidAt());
        long aheadInCategory = productRankingRepository.countAheadInCategory(
                ranking.getCategoryId(), ranking.getTotalAmount(), ranking.getLastBidAt());

        return RankingResponse.builder()
                .productId(ranking.getProductId())
                .categoryId(ranking.getCategoryId())
                .rank((int) ahead + 1)
                .categoryRank((int) aheadInCategory + 1)
                .totalAmount(ranking.getTotalAmount())
                .todayAmount(todayAmount)
                .bidCount(ranking.getBidCount())
                .lastBidAt(ranking.getLastBidAt())
                .build();
    }

    private Map<UUID, Long> todayAmounts(List<UUID> productIds) {

        if (productIds.isEmpty()) {
            return Map.of();
        }

        return dailyRankingRepository.findByBidDateAndProductIdIn(today(), productIds)
                .stream()
                .collect(Collectors.toMap(DailyRanking::getProductId, DailyRanking::getAmount));
    }

    private int position(Pageable pageable, int indexOnPage) {
        return (int) pageable.getOffset() + indexOnPage + 1;
    }

    private LocalDate today() {
        return LocalDate.now(zone);
    }

}