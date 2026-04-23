package com.trading.config;

import com.trading.model.User;
import com.trading.repository.UserRepository;
import com.trading.service.AuthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Optional;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;
    private final AuthService authService;

    public SecurityConfig(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                new AntPathRequestMatcher("/api/auth/**"),
                                new AntPathRequestMatcher("/login/**"),
                                new AntPathRequestMatcher("/oauth2/**"),
                                new AntPathRequestMatcher("/h2-console/**"))
                        .permitAll()
                        .anyRequest().permitAll() // Keep it open for now to avoid breaking existing flows
                )
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oauth2SuccessHandler()));

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler oauth2SuccessHandler() {
        return (request, response, authentication) -> {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String email = oauth2User.getAttribute("email");

            if (email == null) {
                response.sendRedirect("http://localhost:9000/login?error=no_email");
                return;
            }

            // Use email as username prefix
            String username = email.split("@")[0];

            Optional<User> existingUser = userRepository.findByUsername(username);
            if (existingUser.isEmpty()) {
                // Register new user via Google
                authService.register(username, "google_oauth_protected", "Google Auth", "N/A");
            }

            // Redirect back to frontend with username
            response.sendRedirect("http://localhost:9000/?googleLoginSuccess=" + username);
        };
    }
}