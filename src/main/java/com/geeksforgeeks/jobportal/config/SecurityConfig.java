package com.geeksforgeeks.jobportal.config;

import com.geeksforgeeks.jobportal.repository.PortalUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register", "/css/**", "/error").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/recruiter/**").hasRole("RECRUITER")
                        .requestMatchers("/jobs/*/apply", "/jobs/my-applications").hasRole("CANDIDATE")
                        .anyRequest().authenticated())
                .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/jobs", true).permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/login?logout"))
                .build();
    }

    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    UserDetailsService userDetailsService(PortalUserRepository users) {
        return email -> users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("Account not found"));
    }
}
