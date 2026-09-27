package com.geeksforgeeks.jobportal.controller;

import com.geeksforgeeks.jobportal.model.RegistrationForm;
import com.geeksforgeeks.jobportal.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Objects;

@Controller
public class AccountController {
    private final AccountService accounts;

    public AccountController(AccountService accounts) { this.accounts = accounts; }

    @GetMapping("/") public String home() { return "redirect:/jobs"; }
    @GetMapping("/login") public String login() { return "login"; }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegistrationForm form, BindingResult result, Model model) {
        if (!Objects.equals(form.getPassword(), form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match.");
        }
        if (result.hasErrors()) return "register";
        try {
            accounts.register(form.getDisplayName(), form.getEmail(), form.getPassword(), form.getRole());
        } catch (IllegalArgumentException exception) {
            model.addAttribute("registrationError", exception.getMessage());
            return "register";
        }
        return "redirect:/login?registered";
    }
}
