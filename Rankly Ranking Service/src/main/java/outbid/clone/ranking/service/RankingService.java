package outbid.clone.ranking.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import outbid.clone.ranking.dto.AmountToBeatResponse;
import outbid.clone.ranking.dto.BidRequest;
import outbid.clone.ranking.dto.RankingResponse;

import java.time.LocalDate;
import java.util.UUID;

public interface RankingService {

    RankingResponse recordBid(BidRequest request);

    Page<RankingResponse> getLeaderboard(UUID categoryId, Pageable pageable);

    Page<RankingResponse> getTodayLeaderboard(UUID categoryId, Pageable pageable);

    Page<RankingResponse> getDailyLeaderboard(LocalDate date, UUID categoryId, Pageable pageable);

    RankingResponse getProductRanking(UUID productId);

    AmountToBeatResponse getAmountToBeat(UUID productId);
}