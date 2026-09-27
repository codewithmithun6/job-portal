package com.geeksforgeeks.jobportal;

import com.geeksforgeeks.jobportal.model.JobApplication;
import com.geeksforgeeks.jobportal.model.JobPosting;
import com.geeksforgeeks.jobportal.model.PortalUser;
import com.geeksforgeeks.jobportal.repository.JobApplicationRepository;
import com.geeksforgeeks.jobportal.repository.JobPostingRepository;
import com.geeksforgeeks.jobportal.repository.PortalUserRepository;
import com.geeksforgeeks.jobportal.service.JobPortalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:jobportal;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "app.demo.candidate.email=candidate@demo.local",
        "app.demo.candidate.password=CandidateDemo123!",
        "app.demo.recruiter.email=recruiter@demo.local",
        "app.demo.recruiter.password=RecruiterDemo123!",
        "app.demo.admin.email=admin@demo.local",
        "app.demo.admin.password=AdminDemo123!"
})
class JobPortalIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private PortalUserRepository users;
    @Autowired private JobPostingRepository jobs;
    @Autowired private JobApplicationRepository applications;
    @Autowired private JobPortalService portal;

    @Test
    void seedsDemoAccountsAndJobsAndRendersSearchResults() throws Exception {
        PortalUser candidate = user("candidate@demo.local");
        assertEquals(PortalUser.Role.CANDIDATE, candidate.getRole());
        assertEquals(PortalUser.Role.RECRUITER, user("recruiter@demo.local").getRole());
        assertEquals(PortalUser.Role.ADMIN, user("admin@demo.local").getRole());
        assertEquals(3, portal.searchJobs("", "", "", candidate).size());

        mockMvc.perform(get("/jobs").with(SecurityMockMvcRequestPostProcessors.user(candidate)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Junior Java Developer")));
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Create account")));
        mockMvc.perform(get("/jobs/my-applications").with(SecurityMockMvcRequestPostProcessors.user(candidate)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("My applications")));
        JobPosting seededJob = jobs.findAll().stream().filter(posting -> posting.getStatus() == JobPosting.Status.OPEN)
                .findFirst().orElseThrow();
        mockMvc.perform(get("/jobs/{id}", seededJob.getId()).with(SecurityMockMvcRequestPostProcessors.user(candidate)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("About the role")));
        mockMvc.perform(get("/recruiter").with(SecurityMockMvcRequestPostProcessors.user(user("recruiter@demo.local"))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Post a role")));
    }

    @Test
    void candidateAppliesOnceAndAdminCanVerify() throws Exception {
        PortalUser candidate = user("candidate@demo.local");
        PortalUser admin = user("admin@demo.local");
        JobPosting job = jobs.findAll().stream().filter(posting -> posting.getStatus() == JobPosting.Status.OPEN)
                .findFirst().orElseThrow();

        mockMvc.perform(post("/jobs/{id}/apply", job.getId())
                        .param("coverLetter", "I am excited to contribute to this team.")
                        .with(SecurityMockMvcRequestPostProcessors.user(candidate)).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/jobs/" + job.getId()));

        assertThrows(IllegalStateException.class, () -> portal.apply(job.getId(), "Second attempt", candidate));
        JobApplication application = applications.findAll().stream()
                .filter(item -> item.getCandidate().getId().equals(candidate.getId()))
                .findFirst().orElseThrow();
        assertEquals(JobApplication.Status.PENDING, application.getStatus());

        mockMvc.perform(get("/admin/applications").with(SecurityMockMvcRequestPostProcessors.user(admin)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Application queue")));
        mockMvc.perform(post("/admin/applications/{id}/review", application.getId())
                        .param("decision", "VERIFIED")
                        .with(SecurityMockMvcRequestPostProcessors.user(admin)).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertEquals(JobApplication.Status.VERIFIED,
                applications.findById(application.getId()).orElseThrow().getStatus());
    }

    @Test
    void roleProtectedWorkspacesRejectOtherRoles() throws Exception {
        PortalUser candidate = user("candidate@demo.local");
        mockMvc.perform(get("/recruiter").with(SecurityMockMvcRequestPostProcessors.user(candidate)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/applications").with(SecurityMockMvcRequestPostProcessors.user(candidate)))
                .andExpect(status().isForbidden());
    }

    private PortalUser user(String email) {
        return users.findByEmailIgnoreCase(email).orElseThrow();
    }
}
