package com.upiiz.practica02.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.upiiz.practica02.usuario.Role;
import com.upiiz.practica02.usuario.User;
import com.upiiz.practica02.usuario.UserRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class AppConfig {
    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);
    private final UserRepository userRepository;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            final User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .roles(user.getRole().name())
                    .build();
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CommandLineRunner initAdminUser(PasswordEncoder encoder) {
        return args -> {
            // Administrador principal
            crearAdminSiNoExiste("admin@ipn.mx", "admin123", "Administrador IPN", encoder);

            // Puedes descomentar o agregar más administradores aquí:
            // crearAdminSiNoExiste("segundo.admin@ipn.mx", "admin456", "Segundo Administrador", encoder);
        };
    }

    private void crearAdminSiNoExiste(String email, String rawPassword, String name, PasswordEncoder encoder) {
        userRepository.findByEmail(email).ifPresentOrElse(
                existing -> {
                    if (existing.getRole() != Role.ADMIN) {
                        existing.setRole(Role.ADMIN);
                        userRepository.save(existing);
                    }
                },
                () -> {
                    User admin = User.builder()
                            .name(name)
                            .email(email)
                            .password(encoder.encode(rawPassword))
                            .role(Role.ADMIN)
                            .build();
                    userRepository.save(admin);
                    log.info("Usuario ADMIN registrado: {} / {}", email, rawPassword);
                });
    }
}
