package outbid.clone.submission.service;

import outbid.clone.submission.dto.SubmissionRequest;
import outbid.clone.submission.dto.SubmissionResponse;
import outbid.clone.submission.entity.SubmissionStatus;

import java.util.List;
import java.util.UUID;

public interface SubmissionService {

    List<SubmissionResponse> getAllSubmissions();

    SubmissionResponse getSubmissionById(UUID id);

    List<SubmissionResponse> getSubmissionsByStatus(SubmissionStatus status);

    List<SubmissionResponse> getSubmissionsByCategory(UUID categoryId);

    SubmissionResponse createSubmission(SubmissionRequest request);

    SubmissionResponse updateSubmissionStatus(UUID id, SubmissionStatus status, String rejectionReason);

    void deleteSubmission(UUID id);
}