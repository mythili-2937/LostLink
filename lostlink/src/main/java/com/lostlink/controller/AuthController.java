
package com.lostlink.controller;

import com.lostlink.model.CampusUser;
import com.lostlink.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/change-password")
    public String changePasswordPage() {
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            Authentication authentication,
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Model model) {

        if (authentication == null) {
            return "redirect:/login";
        }

        CampusUser user = userRepository
                .findByCampusId(authentication.getName())
                .orElse(null);

        if (user == null) {
            return "redirect:/login";
        }

        if (!passwordEncoder.matches(
                currentPassword, user.getPasswordHash())) {

            model.addAttribute("error",
                    "Current password is incorrect.");
            return "change-password";
        }

        if (newPassword.length() < 12) {
            model.addAttribute("error",
                    "New password must contain at least 12 characters.");
            return "change-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error",
                    "New passwords do not match.");
            return "change-password";
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);

        userRepository.save(user);

        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {

        if (authentication == null) {
            return "redirect:/login";
        }

        model.addAttribute("campusId", authentication.getName());

        return "dashboard";
    }
}
