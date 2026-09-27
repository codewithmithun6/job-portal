package com.geeksforgeeks.jobportal.repository;

import com.geeksforgeeks.jobportal.model.JobApplication;
import com.geeksforgeeks.jobportal.model.JobPosting;
import com.geeksforgeeks.jobportal.model.PortalUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    boolean existsByJobAndCandidate(JobPosting job, PortalUser candidate);
    long countByJob(JobPosting job);
    List<JobApplication> findByCandidateOrderByAppliedAtDesc(PortalUser candidate);
    List<JobApplication> findAllByOrderByAppliedAtDesc();
    Optional<JobApplication> findByIdAndStatus(Long id, JobApplication.Status status);
}
