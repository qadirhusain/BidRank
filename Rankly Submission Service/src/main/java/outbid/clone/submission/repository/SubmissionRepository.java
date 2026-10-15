package outbid.clone.submission.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import outbid.clone.submission.entity.Submission;
import outbid.clone.submission.entity.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    List<Submission> findAllByOrderByCreatedAtDesc();

    List<Submission> findByStatusOrderByCreatedAtDesc(SubmissionStatus status);

    List<Submission> findByCategoryIdOrderByCreatedAtDesc(UUID categoryId);

    boolean existsByUrlIgnoreCaseAndStatusIn(String url, Collection<SubmissionStatus> statuses);

    /** Loads the row and locks it until the transaction ends. Must be called inside a transaction. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Submission s where s.id = :id")
    Optional<Submission> findByIdForUpdate(@Param("id") UUID id);
}