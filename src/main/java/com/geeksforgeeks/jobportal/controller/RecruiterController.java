package com.geeksforgeeks.jobportal.controller;

import com.geeksforgeeks.jobportal.model.JobForm;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;

@Controller
@RequestMapping("/recruiter")
public class RecruiterController {
    private final JobPortalService portal;

    public RecruiterController(JobPortalService portal) { this.portal = portal; }

    @GetMapping
    public String dashboard(@AuthenticationPrincipal PortalUser recruiter, Model model) {
        model.addAttribute("jobs", portal.jobsForRecruiter(recruiter));
        model.addAttribute("jobForm", new JobForm());
        model.addAttribute("employmentTypes", JobPosting.EmploymentType.values());
        return "recruiter";
    }

    @PostMapping("/jobs")
    public String postJob(@Valid @ModelAttribute JobForm jobForm, BindingResult result,
                          @AuthenticationPrincipal PortalUser recruiter, Model model,
                          RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("jobs", portal.jobsForRecruiter(recruiter));
            model.addAttribute("employmentTypes", JobPosting.EmploymentType.values());
            return "recruiter";
        }
        try {
            portal.postJob(new JobPortalService.JobFormData(jobForm.getTitle(), jobForm.getCompany(),
                    jobForm.getLocation(), jobForm.getEmploymentType(), jobForm.getDescription(),
                    jobForm.getSalaryRange()), recruiter);
            redirect.addFlashAttribute("notice", "Your job opening is now published.");
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/recruiter";
    }

    @PostMapping("/jobs/{id}/close")
    public String closeJob(@org.springframework.web.bind.annotation.PathVariable Long id,
                           @AuthenticationPrincipal PortalUser recruiter, RedirectAttributes redirect) {
        portal.closeJob(id, recruiter);
        redirect.addFlashAttribute("notice", "The job posting is closed.");
        return "redirect:/recruiter";
    }
}
