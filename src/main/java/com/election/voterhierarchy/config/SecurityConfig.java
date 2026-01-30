package com.election.voterhierarchy.config;

import com.election.voterhierarchy.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(UserService userService,
            PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                // Public resources
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/login").permitAll()
                // Admin-only endpoints
                .requestMatchers("/users/**").hasRole("ADMIN")
                // ADMIN and OPERATOR can create/edit/delete
                .requestMatchers("/families/new", "/families/create").hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers("/families/*/edit", "/families/*/update").hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers("/families/*/delete").hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers("/families/*/members/new", "/families/*/members/add").hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers("/families/*/members/*/edit", "/families/*/members/*/update").hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers("/families/*/members/*/delete").hasAnyRole("ADMIN", "OPERATOR")
                // All authenticated users can view
                .requestMatchers("/families/**").authenticated()
                // Require authentication for all other requests
                .anyRequest().authenticated()
                )
                .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/families/dashboard", true)
                .failureUrl("/login?error")
                .permitAll()
                )
                .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
                )
                .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .invalidSessionUrl("/login")
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .expiredUrl("/login?expired")
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
                );

        return http.build();
    }
}
