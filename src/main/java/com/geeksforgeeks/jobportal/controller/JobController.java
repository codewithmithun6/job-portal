package com.geeksforgeeks.jobportal.controller;

import com.geeksforgeeks.jobportal.model.ApplicationForm;
import com.geeksforgeeks.jobportal.model.JobPosting;
import com.geeksforgeeks.jobportal.model.PortalUser;
import com.geeksforgeeks.jobportal.service.JobPortalService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/jobs")
public class JobController {
    private final JobPortalService portal;

    public JobController(JobPortalService portal) { this.portal = portal; }

    @GetMapping
    public String list(@AuthenticationPrincipal PortalUser viewer,
                       @RequestParam(defaultValue = "") String q,
                       @RequestParam(defaultValue = "") String location,
                       @RequestParam(defaultValue = "") String type, Model model) {
        model.addAttribute("jobs", portal.searchJobs(q, location, type, viewer));
        model.addAttribute("q", q);
        model.addAttribute("location", location);
        model.addAttribute("type", type);
        model.addAttribute("employmentTypes", JobPosting.EmploymentType.values());
        return "jobs";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, @AuthenticationPrincipal PortalUser viewer, Model model) {
        model.addAttribute("job", portal.findOpenJob(id, viewer));
        model.addAttribute("applicationForm", new ApplicationForm());
        return "job-detail";
    }

    @PostMapping("/{id}/apply")
    public String apply(@PathVariable Long id, @Valid @ModelAttribute ApplicationForm applicationForm,
                        BindingResult result, @AuthenticationPrincipal PortalUser candidate, Model model,
                        RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("job", portal.findOpenJob(id, candidate));
            return "job-detail";
        }
        try {
            portal.apply(id, applicationForm.getCoverLetter(), candidate);
            redirect.addFlashAttribute("notice", "Application submitted. You can follow its review status in My applications.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/jobs/" + id;
    }

    @GetMapping("/my-applications")
    public String myApplications(@AuthenticationPrincipal PortalUser candidate, Model model) {
        model.addAttribute("applications", portal.applicationsForCandidate(candidate));
        return "my-applications";
    }
}
