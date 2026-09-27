package com.geeksforgeeks.jobportal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_applications", uniqueConstraints = {
        @UniqueConstraint(name = "uk_application_candidate_job", columnNames = {"job_id", "candidate_id"})
})
public class JobApplication {
    public enum Status { PENDING, VERIFIED, REJECTED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private JobPosting job;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private PortalUser candidate;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String coverLetter;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Status status = Status.PENDING;
    @Column(nullable = false)
    private LocalDateTime appliedAt;

    protected JobApplication() { }

    public JobApplication(JobPosting job, PortalUser candidate, String coverLetter) {
        this.job = job;
        this.candidate = candidate;
        this.coverLetter = coverLetter;
    }

    @PrePersist void markApplied() { appliedAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public JobPosting getJob() { return job; }
    public PortalUser getCandidate() { return candidate; }
    public String getCoverLetter() { return coverLetter; }
    public Status getStatus() { return status; }
    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setStatus(Status status) { this.status = status; }
}
