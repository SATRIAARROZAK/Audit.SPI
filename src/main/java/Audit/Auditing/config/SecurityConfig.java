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
                                                // Izinkan akses ke URL publik
                                                .requestMatchers("/login", "/register", "/css/**", "/js/**", "/error",
                                                                "/profile/edit", "/profile/update",
                                                                "/profile-photos/**", "/pdf/**") // <-- TAMBAHKAN INI
                                                .permitAll()
                                                .requestMatchers("/api/**").authenticated() // <-- TAMBAHKAN BARIS INI

                                                // Aturan untuk role spesifik
                                                .requestMatchers("/admin/**").hasAuthority("ADMIN")
                                                // PERUBAHAN: Berikan akses ke SEKRETARIS dan KEPALASPI untuk fitur
                                                // persetujuan
                                                .requestMatchers("/kepalaspi/**")
                                                .hasAnyAuthority("KEPALASPI", "SEKRETARIS")
                                                // SEKRETARIS tetap punya akses ke fitur review-nya
                                                .requestMatchers("/sekretaris/**").hasAuthority("SEKRETARIS")
                                                // .requestMatchers("/pegawai/**").hasAuthority("PEGAWAI")
                                                .requestMatchers("/pegawai/**").hasAuthority("PEGAWAI") // <-- TAMBAHKAN
                                                                                                        // BARIS INI
                                                .requestMatchers("/audit/kertas-kerja/new/**",
                                                                "/audit/kertas-kerja/save")
                                                .hasAuthority("PEGAWAI") // Explicitly allow PEGAWAI
                                                .requestMatchers("/audit/**")
                                                .hasAnyAuthority("ADMIN", "KEPALASPI", "SEKRETARIS", "PEGAWAI")

                                                .requestMatchers("/dashboard")
                                                .hasAnyAuthority("ADMIN", "KEPALASPI", "SEKRETARIS", "PEGAWAI")
                                                .anyRequest().authenticated())

                                .formLogin(formLogin -> formLogin
                                                .loginPage("/login")
                                                .loginProcessingUrl("/perform_login")
                                                .successHandler(authenticationSuccessHandler)
                                                .failureUrl("/login?error=true")
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout=true")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll())
                                .exceptionHandling(exceptionHandling -> exceptionHandling
                                                .accessDeniedPage("/access-denied"));
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