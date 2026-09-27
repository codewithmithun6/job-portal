package com.geeksforgeeks.jobportal.controller;

import com.geeksforgeeks.jobportal.model.PortalUser;
import com.geeksforgeeks.jobportal.service.JobPortalService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final JobPortalService portal;

    public AdminController(JobPortalService portal) { this.portal = portal; }

    @GetMapping("/applications")
    public String applications(@AuthenticationPrincipal PortalUser admin, Model model) {
        model.addAttribute("applications", portal.allApplications());
        return "admin-applications";
    }

    @PostMapping("/applications/{id}/review")
    public String review(@PathVariable Long id, @RequestParam String decision,
                         @AuthenticationPrincipal PortalUser admin, RedirectAttributes redirect) {
        try {
            portal.reviewApplication(id, decision, admin);
            redirect.addFlashAttribute("notice", "Application status updated.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/applications";
    }
}
