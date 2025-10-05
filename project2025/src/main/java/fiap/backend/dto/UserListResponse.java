package fiap.backend.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * DTO para retornar informações resumidas de usuários em listagens
 */
public class UserListResponse {
    
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private Set<String> roles;
    private LocalDateTime createdAt;
    private Boolean isActive;

    // Constructors
    public UserListResponse() {
    }

    public UserListResponse(UUID userId, String email, String firstName, String lastName, 
                           Set<String> roles, LocalDateTime createdAt) {
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = firstName + " " + lastName;
        this.roles = roles;
        this.createdAt = createdAt;
        this.isActive = true;
    }

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
