package com.mastertest.lasttest.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity()
@Configuration
public class SecurityConfig  {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests(
                        (authz) -> authz
                                .requestMatchers("/api/people/add", "/api/people/update/{id}").hasRole("ADMIN")
                                .requestMatchers("/api/people/search").permitAll()
                                .requestMatchers("/api/employees/{employeeId}/positions").hasAnyRole("ADMIN", "EMPLOYEE")
                                .requestMatchers("/api/import/employees", "/api/import/importstatus/{id}").hasAnyRole("ADMIN", "IMPORTER")
                                .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(User.withUsername("admin")
                .password(encoder.encode("adminpassword"))
                .roles("ADMIN").build());
        manager.createUser(User.withUsername("importer")
                .password(encoder.encode("importerpassword"))
                .roles("IMPORTER").build());
        manager.createUser(User.withUsername("employee")
                .password(encoder.encode("employeepassword"))
                .roles("EMPLOYEE").build());

        return manager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}
