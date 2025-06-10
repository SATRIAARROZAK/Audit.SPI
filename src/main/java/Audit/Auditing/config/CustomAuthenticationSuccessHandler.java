package Audit.Auditing.config;

import Audit.Auditing.model.User;
import Audit.Auditing.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user != null && !user.isProfileComplete()) {
            // Jika profil belum lengkap, arahkan ke halaman edit profil
            getRedirectStrategy().sendRedirect(request, response, "/profile/edit");
        } else {
            // Jika sudah lengkap, arahkan ke dashboard
            super.setDefaultTargetUrl("/dashboard");
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
}