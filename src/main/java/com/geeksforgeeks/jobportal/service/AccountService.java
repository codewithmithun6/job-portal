package com.geeksforgeeks.jobportal.service;

import com.geeksforgeeks.jobportal.model.PortalUser;
import com.geeksforgeeks.jobportal.repository.PortalUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    private final PortalUserRepository users;
    private final PasswordEncoder encoder;

    public AccountService(PortalUserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Transactional
    public void register(String displayName, String email, String password, String requestedRole) {
        PortalUser.Role role;
        try {
            role = PortalUser.Role.valueOf(requestedRole);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Choose candidate or recruiter for your account.");
        }
        if (role == PortalUser.Role.ADMIN) {
            throw new IllegalArgumentException("Admin accounts cannot be registered here.");
        }
        String normalizedEmail = email.trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        users.save(new PortalUser(normalizedEmail, displayName.trim(), encoder.encode(password), role));
    }
}
