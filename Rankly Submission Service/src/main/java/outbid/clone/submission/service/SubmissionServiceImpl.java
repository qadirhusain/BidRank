package outbid.clone.submission.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import outbid.clone.submission.client.CategoryClient;
import outbid.clone.submission.client.ProductClient;
import outbid.clone.submission.dto.ProductCreateRequest;
import outbid.clone.submission.dto.SubmissionRequest;
import outbid.clone.submission.dto.SubmissionResponse;
import outbid.clone.submission.entity.Submission;
import outbid.clone.submission.entity.SubmissionStatus;
import outbid.clone.submission.exception.InvalidSubmissionRequestException;
import outbid.clone.submission.exception.SubmissionConflictException;
import outbid.clone.submission.exception.SubmissionNotFoundException;
import outbid.clone.submission.repository.SubmissionRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private static final int MAX_REJECTION_REASON_LENGTH = 1000;

    private static final List<SubmissionStatus> ACTIVE_STATUSES =
            List.of(SubmissionStatus.PENDING, SubmissionStatus.APPROVED);

    private final SubmissionRepository submissionRepository;
    private final CategoryClient categoryClient;
    private final ProductClient productClient;
    private final ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionResponse> getAllSubmissions() {

        return submissionRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionResponse getSubmissionById(UUID id) {

        return mapToResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionResponse> getSubmissionsByStatus(SubmissionStatus status) {

        return submissionRepository
                .findByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionResponse> getSubmissionsByCategory(UUID categoryId) {

        return submissionRepository
                .findByCategoryIdOrderByCreatedAtDesc(categoryId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public SubmissionResponse createSubmission(SubmissionRequest request) {

        String url = request.getUrl().trim();

        if (submissionRepository.existsByUrlIgnoreCaseAndStatusIn(url, ACTIVE_STATUSES)) {
            throw new SubmissionConflictException(
                    "A submission for url '" + url + "' already exists");
        }

        categoryClient.validateCategory(request.getCategoryId());

        Submission submission = modelMapper.map(request, Submission.class);

        submission.setTitle(request.getTitle().trim());
        submission.setDescription(blankToNull(request.getDescription()));
        submission.setUrl(request.getUrl().trim());
        submission.setLogoUrl(blankToNull(request.getLogoUrl()));
        submission.setStatus(SubmissionStatus.PENDING);
        submission.setRejectionReason(null);
        submission.setProductId(null);

        Submission saved = submissionRepository.save(submission);

        log.info("Submission {} created for url '{}'", saved.getId(), saved.getUrl());

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public SubmissionResponse updateSubmissionStatus(UUID id, SubmissionStatus status, String rejectionReason) {

        if (status == SubmissionStatus.PENDING) {
            throw new InvalidSubmissionRequestException(
                    "A submission cannot be moved back to PENDING");
        }

        String reason = blankToNull(rejectionReason);

        if (reason != null && reason.length() > MAX_REJECTION_REASON_LENGTH) {
            throw new InvalidSubmissionRequestException(
                    "Rejection reason must not exceed "
                            + MAX_REJECTION_REASON_LENGTH + " characters");
        }

        Submission submission = submissionRepository
                .findByIdForUpdate(id)
                .orElseThrow(() -> notFound(id));

        if (submission.getStatus() == SubmissionStatus.APPROVED) {

            if (status == SubmissionStatus.APPROVED) {
                return mapToResponse(submission);
            }

            throw new SubmissionConflictException(
                    "An approved submission cannot be rejected");
        }

        if (status == SubmissionStatus.APPROVED) {

            if (submission.getListingType() == null) {
                throw new InvalidSubmissionRequestException(
                        "Submission has no listing type and cannot be approved");
            }

            // A rejected submission is not "active", so its url may have been submitted again since
            if (submission.getStatus() == SubmissionStatus.REJECTED
                    && submissionRepository.existsByUrlIgnoreCaseAndStatusIn(submission.getUrl(), ACTIVE_STATUSES)) {
                throw new SubmissionConflictException(
                        "Cannot approve: another submission for url '" + submission.getUrl() + "' already exists");
            }

            UUID productId = productClient.createProduct(toProductRequest(submission));

            submission.setProductId(productId);
            submission.setStatus(SubmissionStatus.APPROVED);
            submission.setRejectionReason(null);

            log.info("Submission {} approved, product {} created", id, productId);

        } else {

            submission.setStatus(SubmissionStatus.REJECTED);
            submission.setRejectionReason(reason);

            log.info("Submission {} rejected", id);
        }

        return mapToResponse(submissionRepository.save(submission));
    }

    @Override
    @Transactional
    public void deleteSubmission(UUID id) {

        Submission submission = findOrThrow(id);

        // Payment finds the product through the approved submission, so it must stay
        if (submission.getStatus() == SubmissionStatus.APPROVED) {
            throw new SubmissionConflictException(
                    "An approved submission cannot be deleted because a product is linked to it");
        }

        submissionRepository.delete(submission);

        log.info("Submission {} deleted", id);
    }

    private Submission findOrThrow(UUID id) {
        return submissionRepository
                .findById(id)
                .orElseThrow(() -> notFound(id));
    }

    private SubmissionNotFoundException notFound(UUID id) {
        return new SubmissionNotFoundException("Submission not found with id: " + id);
    }

    private ProductCreateRequest toProductRequest(Submission submission) {

        return ProductCreateRequest.builder()
                .title(submission.getTitle())
                .description(submission.getDescription())
                .listingType(submission.getListingType())
                .url(submission.getUrl())
                .categoryId(submission.getCategoryId())
                .logoUrl(submission.getLogoUrl())
                .build();
    }

    private SubmissionResponse mapToResponse(Submission submission) {
        return modelMapper.map(submission, SubmissionResponse.class);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

}