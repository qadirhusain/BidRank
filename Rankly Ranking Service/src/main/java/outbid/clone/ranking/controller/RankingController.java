package outbid.clone.ranking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import outbid.clone.ranking.dto.AmountToBeatResponse;
import outbid.clone.ranking.dto.BidRequest;
import outbid.clone.ranking.dto.RankingResponse;
import outbid.clone.ranking.service.RankingService;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
public class RankingController {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final RankingService rankingService;

    @GetMapping
    public ResponseEntity<Page<RankingResponse>> getLeaderboard(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size
    ) {

        return ResponseEntity.ok(rankingService.getLeaderboard(categoryId, pageable(page, size)));
    }

    @GetMapping("/today")
    public ResponseEntity<Page<RankingResponse>> getTodayLeaderboard(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size
    ) {

        return ResponseEntity.ok(rankingService.getTodayLeaderboard(categoryId, pageable(page, size)));
    }

    @GetMapping("/daily/{date}")
    public ResponseEntity<Page<RankingResponse>> getDailyLeaderboard(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size
    ) {
        return ResponseEntity.ok(rankingService.getDailyLeaderboard(date, categoryId, pageable(page, size)));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<RankingResponse> getProductRanking(@PathVariable UUID productId) {
        return ResponseEntity.ok(rankingService.getProductRanking(productId));
    }

    @GetMapping("/amount-to-beat")
    public ResponseEntity<AmountToBeatResponse> getAmountToBeat(
            @RequestParam(required = false) UUID productId
    ) {
        return ResponseEntity.ok(rankingService.getAmountToBeat(productId));
    }

    @PostMapping("/bids")
    public ResponseEntity<RankingResponse> recordBid(@Valid @RequestBody BidRequest request) {
        return ResponseEntity.ok(rankingService.recordBid(request));
    }

    private Pageable pageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.clamp(size, 1, MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize);
    }
}