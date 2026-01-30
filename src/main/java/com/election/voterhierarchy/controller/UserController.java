package com.election.voterhierarchy.controller;

import com.election.voterhierarchy.dto.PasswordChangeDTO;
import com.election.voterhierarchy.dto.UserCreateDTO;
import com.election.voterhierarchy.dto.UserUpdateDTO;
import com.election.voterhierarchy.entity.User;
import com.election.voterhierarchy.enums.Role;
import com.election.voterhierarchy.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "user/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("userCreateDTO", new UserCreateDTO());
        model.addAttribute("roles", Role.values());
        return "user/create";
    }

    @PostMapping("/create")
    public String createUser(@Valid @ModelAttribute UserCreateDTO dto,
                            BindingResult result,
                            Authentication authentication,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.confirmPassword", "Passwords do not match");
        }

        if (userService.usernameExists(dto.getUsername())) {
            result.rejectValue("username", "error.username", "Username already exists");
        }

        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "user/create";
        }

        try {
            String currentUser = authentication.getName();
            userService.createUser(
                dto.getUsername(),
                dto.getPassword(),
                dto.getFullName(),
                dto.getEmail(),
                dto.getRoles(),
                currentUser
            );
            redirectAttributes.addFlashAttribute("successMessage", "User created successfully");
            return "redirect:/users";
        } catch (Exception e) {
            log.error("Error creating user", e);
            model.addAttribute("errorMessage", "Error: " + e.getMessage());
            model.addAttribute("roles", Role.values());
            return "user/create";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        
        UserUpdateDTO dto = UserUpdateDTO.builder()
            .fullName(user.getFullName())
            .email(user.getEmail())
            .roles(user.getRoles())
            .build();

        model.addAttribute("user", user);
        model.addAttribute("userUpdateDTO", dto);
        model.addAttribute("roles", Role.values());
        return "user/edit";
    }

    @PostMapping("/{id}/update")
    public String updateUser(@PathVariable Long id,
                            @Valid @ModelAttribute UserUpdateDTO dto,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            User user = userService.getUserById(id);
            model.addAttribute("user", user);
            model.addAttribute("roles", Role.values());
            return "user/edit";
        }

        try {
            userService.updateUser(id, dto.getFullName(), dto.getEmail(), dto.getRoles());
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully");
            return "redirect:/users";
        } catch (Exception e) {
            log.error("Error updating user", e);
            model.addAttribute("errorMessage", "Error: " + e.getMessage());
            User user = userService.getUserById(id);
            model.addAttribute("user", user);
            model.addAttribute("roles", Role.values());
            return "user/edit";
        }
    }

    @GetMapping("/{id}/change-password")
    public String showChangePasswordForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        model.addAttribute("passwordChangeDTO", new PasswordChangeDTO());
        return "user/change-password";
    }

    @PostMapping("/{id}/change-password")
    public String changePassword(@PathVariable Long id,
                                @Valid @ModelAttribute PasswordChangeDTO dto,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.confirmPassword", "Passwords do not match");
        }

        if (result.hasErrors()) {
            User user = userService.getUserById(id);
            model.addAttribute("user", user);
            return "user/change-password";
        }

        try {
            userService.changePassword(id, dto.getNewPassword());
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully");
            return "redirect:/users";
        } catch (Exception e) {
            log.error("Error changing password", e);
            model.addAttribute("errorMessage", "Error: " + e.getMessage());
            User user = userService.getUserById(id);
            model.addAttribute("user", user);
            return "user/change-password";
        }
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "User status updated successfully");
        } catch (Exception e) {
            log.error("Error toggling user status", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting user", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/users";
    }
}