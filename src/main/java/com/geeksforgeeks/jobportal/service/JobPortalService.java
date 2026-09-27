package com.geeksforgeeks.jobportal.service;

import com.geeksforgeeks.jobportal.model.JobApplication;
import com.geeksforgeeks.jobportal.model.JobPosting;
import com.geeksforgeeks.jobportal.model.PortalUser;
import com.geeksforgeeks.jobportal.repository.JobApplicationRepository;
import com.geeksforgeeks.jobportal.repository.JobPostingRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

@Service
public class JobPortalService {
    private final JobPostingRepository jobs;
    private final JobApplicationRepository applications;

    public JobPortalService(JobPostingRepository jobs, JobApplicationRepository applications) {
        this.jobs = jobs;
        this.applications = applications;
    }

    @Transactional(readOnly = true)
    public List<JobCard> searchJobs(String q, String location, String type, PortalUser viewer) {
        List<JobPosting> matches = jobs.searchOpen(safe(q), safe(location), JobPosting.Status.OPEN);
        return matches.stream()
                .filter(job -> type == null || type.isBlank() || job.getEmploymentType().name().equals(type))
                .map(job -> new JobCard(job.getId(), job.getTitle(), job.getCompany(), job.getLocation(),
                        job.getEmploymentType(), job.getSalaryRange(), job.getPostedAt(),
                        viewer != null && viewer.getRole() == PortalUser.Role.CANDIDATE && applications.existsByJobAndCandidate(job, viewer)))
                .toList();
    }

    @Transactional(readOnly = true)
    public JobDetail findOpenJob(Long id, PortalUser viewer) {
        JobPosting job = jobs.findByIdAndStatus(id, JobPosting.Status.OPEN)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        boolean alreadyApplied = viewer != null && viewer.getRole() == PortalUser.Role.CANDIDATE
                && applications.existsByJobAndCandidate(job, viewer);
        return new JobDetail(job.getId(), job.getTitle(), job.getCompany(), job.getLocation(), job.getEmploymentType(),
                job.getDescription(), job.getSalaryRange(), job.getPostedAt(), job.getRecruiter().getDisplayName(), alreadyApplied);
    }

    @Transactional
    public JobPosting postJob(JobFormData form, PortalUser recruiter) {
        requireRole(recruiter, PortalUser.Role.RECRUITER);
        JobPosting.EmploymentType type;
        try {
            type = JobPosting.EmploymentType.valueOf(form.employmentType());
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Choose a valid employment type.");
        }
        return jobs.save(new JobPosting(form.title().trim(), form.company().trim(), form.location().trim(), type,
                form.description().trim(), safeNullable(form.salaryRange()), recruiter));
    }

    @Transactional(readOnly = true)
    public List<RecruiterJob> jobsForRecruiter(PortalUser recruiter) {
        requireRole(recruiter, PortalUser.Role.RECRUITER);
        return jobs.findByRecruiterOrderByPostedAtDesc(recruiter).stream()
                .map(job -> new RecruiterJob(job.getId(), job.getTitle(), job.getCompany(), job.getLocation(),
                        job.getEmploymentType(), job.getStatus(), applications.countByJob(job)))
                .toList();
    }

    @Transactional
    public void closeJob(Long jobId, PortalUser recruiter) {
        requireRole(recruiter, PortalUser.Role.RECRUITER);
        JobPosting job = jobs.findById(jobId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        job.setStatus(JobPosting.Status.CLOSED);
    }

    @Transactional
    public void apply(Long jobId, String coverLetter, PortalUser candidate) {
        requireRole(candidate, PortalUser.Role.CANDIDATE);
        JobPosting job = jobs.findByIdAndStatus(jobId, JobPosting.Status.OPEN)
                .orElseThrow(() -> new IllegalArgumentException("This job is no longer open."));
        if (applications.existsByJobAndCandidate(job, candidate)) {
            throw new IllegalStateException("You already applied for this job.");
        }
        try {
            applications.saveAndFlush(new JobApplication(job, candidate, coverLetter.trim()));
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalStateException("You already applied for this job.", exception);
        }
    }

    @Transactional(readOnly = true)
    public List<ApplicationView> applicationsForCandidate(PortalUser candidate) {
        requireRole(candidate, PortalUser.Role.CANDIDATE);
        return applications.findByCandidateOrderByAppliedAtDesc(candidate).stream()
                .map(application -> view(application)).toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationView> allApplications() {
        return applications.findAllByOrderByAppliedAtDesc().stream().map(this::view).toList();
    }

    @Transactional
    public void reviewApplication(Long id, String decision, PortalUser admin) {
        requireRole(admin, PortalUser.Role.ADMIN);
        JobApplication application = applications.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (application.getStatus() != JobApplication.Status.PENDING) {
            throw new IllegalStateException("This application has already been reviewed.");
        }
        if (!"VERIFIED".equals(decision) && !"REJECTED".equals(decision)) {
            throw new IllegalArgumentException("Choose verify or reject.");
        }
        application.setStatus(JobApplication.Status.valueOf(decision));
    }

    private ApplicationView view(JobApplication application) {
        return new ApplicationView(application.getId(), application.getJob().getTitle(), application.getJob().getCompany(),
                application.getCandidate().getDisplayName(), application.getCandidate().getEmail(),
                application.getCoverLetter(), application.getStatus(), application.getAppliedAt());
    }

    private void requireRole(PortalUser user, PortalUser.Role role) {
        if (user == null || user.getRole() != role) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private String safe(String value) { return value == null ? "" : value.trim(); }
    private String safeNullable(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    public record JobCard(Long id, String title, String company, String location,
                          JobPosting.EmploymentType employmentType, String salaryRange,
                          java.time.LocalDateTime postedAt, boolean alreadyApplied) { }
    public record JobDetail(Long id, String title, String company, String location,
                            JobPosting.EmploymentType employmentType, String description,
                            String salaryRange, java.time.LocalDateTime postedAt,
                            String recruiterName, boolean alreadyApplied) { }
    public record RecruiterJob(Long id, String title, String company, String location,
                               JobPosting.EmploymentType employmentType, JobPosting.Status status,
                               long applicationCount) { }
    public record ApplicationView(Long id, String jobTitle, String company, String candidateName,
                                  String candidateEmail, String coverLetter, JobApplication.Status status,
                                  java.time.LocalDateTime appliedAt) { }
    public record JobFormData(String title, String company, String location, String employmentType,
                              String description, String salaryRange) { }
}
