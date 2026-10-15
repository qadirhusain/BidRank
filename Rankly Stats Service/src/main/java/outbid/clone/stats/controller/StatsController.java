package outbid.clone.stats.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import outbid.clone.stats.dto.HeartbeatRequest;
import outbid.clone.stats.dto.OnlineUsersResponse;
import outbid.clone.stats.dto.StatsResponse;
import outbid.clone.stats.service.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping
    public ResponseEntity<StatsResponse> getStats() {

        return ResponseEntity.ok(statsService.getStats());
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<Void> heartbeat(
            @Valid @RequestBody HeartbeatRequest request
    ) {

        statsService.heartbeat(request.getVisitorId());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/online")
    public ResponseEntity<OnlineUsersResponse> getOnlineUsers() {

        Long count = statsService.getOnlineUserCount();

        return ResponseEntity.ok(new OnlineUsersResponse(count));
    }
}