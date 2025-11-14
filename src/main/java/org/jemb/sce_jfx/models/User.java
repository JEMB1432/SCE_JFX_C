package org.jemb.sce_jfx.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class User {
    private String id;
    private String email;
    private String passwordHash;
    private String role;
    private String firstName;
    private String lastName;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private String resetToken;
    private LocalDateTime resetTokenExpires;

    // Constructores
    public User() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
    }

    public User(String email, String passwordHash, String role, String firstName, String lastName) {
        this();
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public LocalDateTime getResetTokenExpires() { return resetTokenExpires; }
    public void setResetTokenExpires(LocalDateTime resetTokenExpires) { this.resetTokenExpires = resetTokenExpires; }

    // Métodos utilitarios
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isAdmin() {
        return "admin".equals(role);
    }

    public boolean isTeacher() {
        return "teacher".equals(role);
    }

    public boolean isAssistant() {
        return "assistant".equals(role);
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}