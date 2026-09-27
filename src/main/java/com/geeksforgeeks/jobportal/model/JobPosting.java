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

import java.time.LocalDateTime;

@Entity
@Table(name = "job_postings")
public class JobPosting {
    public enum EmploymentType { FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP }
    public enum Status { OPEN, CLOSED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 140)
    private String title;
    @Column(nullable = false, length = 120)
    private String company;
    @Column(nullable = false, length = 120)
    private String location;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmploymentType employmentType;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    @Column(length = 120)
    private String salaryRange;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Status status = Status.OPEN;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recruiter_id", nullable = false)
    private PortalUser recruiter;
    @Column(nullable = false)
    private LocalDateTime postedAt;

    protected JobPosting() { }

    public JobPosting(String title, String company, String location, EmploymentType employmentType,
                      String description, String salaryRange, PortalUser recruiter) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.employmentType = employmentType;
        this.description = description;
        this.salaryRange = salaryRange;
        this.recruiter = recruiter;
    }

    @PrePersist void markPosted() { postedAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getCompany() { return company; }
    public String getLocation() { return location; }
    public EmploymentType getEmploymentType() { return employmentType; }
    public String getDescription() { return description; }
    public String getSalaryRange() { return salaryRange; }
    public Status getStatus() { return status; }
    public PortalUser getRecruiter() { return recruiter; }
    public LocalDateTime getPostedAt() { return postedAt; }
    public void setStatus(Status status) { this.status = status; }
}
