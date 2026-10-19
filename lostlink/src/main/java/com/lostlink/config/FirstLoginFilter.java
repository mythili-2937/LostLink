
package com.lostlink.config;

import java.io.IOException;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.lostlink.model.CampusUser;
import com.lostlink.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FirstLoginFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public FirstLoginFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();

        // Allow login, logout, password change, and static resources.
        if (path.equals("/login")
                || path.equals("/logout")
                || path.equals("/change-password")
                || path.equals("/error")
                || path.equals("/favicon.ico")
                || path.startsWith("/css/")
                || path.startsWith("/images/")
                || path.startsWith("/js/")) {

            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        // Only check users who have actually logged in.
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {

            String campusId = authentication.getName();

            CampusUser user = userRepository
                    .findByCampusId(campusId)
                    .orElse(null);

            // Force first-time users to change their password.
            if (user != null && user.isMustChangePassword()) {

                response.sendRedirect(
                        request.getContextPath() + "/change-password"
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
