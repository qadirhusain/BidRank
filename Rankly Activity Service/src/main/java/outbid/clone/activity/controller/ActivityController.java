package outbid.clone.activity.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import outbid.clone.activity.dto.ActivityRequest;
import outbid.clone.activity.dto.ActivityStatsResponse;
import outbid.clone.activity.dto.ActivityTotalsResponse;
import outbid.clone.activity.entity.ActivityType;
import outbid.clone.activity.service.ActivityService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {

    private static final int MAX_IDS_PER_LOOKUP = 100;

    private final ActivityService activityService;

    /** Called by the frontend just before it opens a listing's website. */
    @PostMapping("/clicks")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recordClick(
            @Valid @RequestBody ActivityRequest request,
            HttpServletRequest httpRequest
    ) {
        record(ActivityType.CLICK, request, httpRequest);
    } //TESTED

    /** Called by the frontend when a product detail page is opened. */
    @PostMapping("/views")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recordView(
            @Valid @RequestBody ActivityRequest request,
            HttpServletRequest httpRequest
    ) {
        record(ActivityType.VIEW, request, httpRequest);
    } //TESTED

    @GetMapping("/products/{productId}")
    public ActivityStatsResponse getStats(@PathVariable UUID productId) {
        return activityService.getStats(productId);
    } //TESTED

    /** Totals for many products, in the same order as the given ids. */
    @PostMapping("/products/by-ids")
    public List<ActivityStatsResponse> getStatsByIds(@RequestBody List<UUID> productIds) {
        if (productIds != null && productIds.size() > MAX_IDS_PER_LOOKUP) {
            throw new IllegalArgumentException(
                    "A maximum of " + MAX_IDS_PER_LOOKUP + " ids is allowed per request");
        }
        return activityService.getStatsByIds(productIds);
    } //TESTED

    @GetMapping("/totals")
    public ActivityTotalsResponse getTotals() {
        return activityService.getTotals();
    }

    private void record(ActivityType type, ActivityRequest request, HttpServletRequest httpRequest) {
        activityService.recordActivity(
                type,
                request.getProductId(),
                clientIp(httpRequest),
                httpRequest.getHeader(HttpHeaders.USER_AGENT));
    }

    /** Uses the first X-Forwarded-For address, which the API Gateway must set itself. */
    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
