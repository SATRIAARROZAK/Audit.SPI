package Audit.Auditing.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private AuthenticationSuccessHandler authenticationSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        // Izinkan akses ke URL profil dan foto
                        .requestMatchers("/login", "/register", "/css/**", "/js/**", "/error", "/profile/edit", "/profile/update", "/profile-photos/**").permitAll()
                        // Hanya ADMIN yang bisa akses halaman /admin/**
                        .requestMatchers("/admin/**").hasAuthority("ADMIN") // FIX: Simplified and corrected rule
                        // KEPALASPI bisa akses dashboard dan fitur spesifiknya
                        .requestMatchers("/kepalaspi/**").hasAuthority("KEPALASPI")
                        // SEKRETARIS bisa akses dashboard dan fitur spesifiknya
                        .requestMatchers("/sekretaris/**").hasAuthority("SEKRETARIS")
                        // KARYAWAN bisa akses dashboard dan fitur spesifiknya
                        .requestMatchers("/pegawai/**").hasAuthority("PEGAWAI")
                        .requestMatchers("/dashboard").hasAnyAuthority("ADMIN", "KEPALASPI", "SEKRETARIS", "PEGAWAI")
                        .anyRequest().authenticated())
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .loginProcessingUrl("/perform_login")
                        // Gunakan custom success handler
                        .successHandler(authenticationSuccessHandler)
                        .failureUrl("/login?error=true")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .exceptionHandling(exceptionHandling -> exceptionHandling.accessDeniedPage("/access-denied")
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(passwordEncoder);
        authProvider.setUserDetailsService(userDetailsService);
        return authProvider;
    }
}