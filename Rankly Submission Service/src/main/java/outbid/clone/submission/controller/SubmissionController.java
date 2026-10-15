package outbid.clone.submission.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import outbid.clone.submission.dto.SubmissionRequest;
import outbid.clone.submission.dto.SubmissionResponse;
import outbid.clone.submission.entity.SubmissionStatus;
import outbid.clone.submission.service.SubmissionService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @GetMapping
    public List<SubmissionResponse> getAllSubmissions(
            @RequestParam(required = false) SubmissionStatus status
    ) {

        return status != null
                ? submissionService.getSubmissionsByStatus(status)
                : submissionService.getAllSubmissions();
    }

    @GetMapping("/{id}")
    public SubmissionResponse getSubmissionById(@PathVariable UUID id) {

        return submissionService.getSubmissionById(id);
    }

    @GetMapping("/category/{categoryId}")
    public List<SubmissionResponse> getSubmissionsByCategory(@PathVariable UUID categoryId) {

        return submissionService.getSubmissionsByCategory(categoryId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionResponse createSubmission(@Valid @RequestBody SubmissionRequest request) {

        return submissionService.createSubmission(request);
    }

    @PutMapping("/{id}/status")
    public SubmissionResponse updateSubmissionStatus(
            @PathVariable UUID id,
            @RequestParam SubmissionStatus status,
            @RequestParam(required = false) String rejectionReason
    ) {

        return submissionService.updateSubmissionStatus(id, status, rejectionReason);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubmission(@PathVariable UUID id) {

        submissionService.deleteSubmission(id);
    }
}