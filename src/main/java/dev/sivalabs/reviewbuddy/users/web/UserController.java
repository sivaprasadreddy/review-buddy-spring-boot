package dev.sivalabs.reviewbuddy.users.web;

import dev.sivalabs.reviewbuddy.users.domain.UserService;
import dev.sivalabs.reviewbuddy.users.domain.models.CreateUserCmd;
import dev.sivalabs.reviewbuddy.users.domain.models.Role;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
class UserController {
    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    String loginForm() {
        return "login";
    }

    @GetMapping("/registration")
    String registrationForm(Model model) {
        model.addAttribute("user", new RegisterUserRequest("", "", ""));
        return "registration";
    }

    @PostMapping("/registration")
    String registerUser(
            @ModelAttribute("user") @Valid RegisterUserRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "registration";
        }
        try {
            var cmd = new CreateUserCmd(request.name(), request.email(), request.password(), Role.ROLE_USER);
            userService.createUser(cmd);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Registration failed: " + e.getMessage());
            return "redirect:/registration";
        }
    }
}
