package com.geeksforgeeks.jobportal.config;

import com.geeksforgeeks.jobportal.model.JobPosting;
import com.geeksforgeeks.jobportal.model.PortalUser;
import com.geeksforgeeks.jobportal.repository.JobPostingRepository;
import com.geeksforgeeks.jobportal.repository.PortalUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class DemoDataSeeder {
    @Bean
    ApplicationRunner seedDemoData(PortalUserRepository users, JobPostingRepository jobs, PasswordEncoder encoder,
                                   @Value("${app.demo.candidate.email}") String candidateEmail,
                                   @Value("${app.demo.candidate.password}") String candidatePassword,
                                   @Value("${app.demo.recruiter.email}") String recruiterEmail,
                                   @Value("${app.demo.recruiter.password}") String recruiterPassword,
                                   @Value("${app.demo.admin.email}") String adminEmail,
                                   @Value("${app.demo.admin.password}") String adminPassword) {
        return args -> seed(users, jobs, encoder, candidateEmail, candidatePassword,
                recruiterEmail, recruiterPassword, adminEmail, adminPassword);
    }

    @Transactional
    void seed(PortalUserRepository users, JobPostingRepository jobs, PasswordEncoder encoder,
              String candidateEmail, String candidatePassword, String recruiterEmail, String recruiterPassword,
              String adminEmail, String adminPassword) {
        PortalUser candidate = findOrCreate(users, encoder, candidateEmail, "Demo Candidate", candidatePassword, PortalUser.Role.CANDIDATE);
        PortalUser recruiter = findOrCreate(users, encoder, recruiterEmail, "Northstar Hiring", recruiterPassword, PortalUser.Role.RECRUITER);
        findOrCreate(users, encoder, adminEmail, "Portal Admin", adminPassword, PortalUser.Role.ADMIN);

        addJobIfMissing(jobs, recruiter, "Product Operations Associate", "Northstar Labs", "Toronto, ON",
                JobPosting.EmploymentType.FULL_TIME, "Help the product team turn customer feedback into a clear, reliable roadmap. Partner with design, support, and engineering.", "$72,000 - $88,000");
        addJobIfMissing(jobs, recruiter, "Junior Java Developer", "Northstar Labs", "Remote, Canada",
                JobPosting.EmploymentType.FULL_TIME, "Build and test Spring services with an experienced team. This role is suited to a developer who enjoys learning, code review, and thoughtful documentation.", "$68,000 - $82,000");
        addJobIfMissing(jobs, recruiter, "UX Research Intern", "Northstar Labs", "Toronto, ON",
                JobPosting.EmploymentType.INTERNSHIP, "Support moderated research sessions, synthesize findings, and help the design team make accessible product decisions.", "$25 - $30 / hour");
    }

    private PortalUser findOrCreate(PortalUserRepository users, PasswordEncoder encoder, String email,
                                   String name, String password, PortalUser.Role role) {
        return users.findByEmailIgnoreCase(email)
                .orElseGet(() -> users.save(new PortalUser(email.trim().toLowerCase(), name,
                        encoder.encode(password), role)));
    }

    private void addJobIfMissing(JobPostingRepository jobs, PortalUser recruiter, String title, String company,
                                 String location, JobPosting.EmploymentType type, String description, String salary) {
        if (!jobs.existsByTitleAndCompany(title, company)) {
            jobs.save(new JobPosting(title, company, location, type, description, salary, recruiter));
        }
    }
}
