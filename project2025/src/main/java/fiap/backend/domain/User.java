package fiap.backend.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @NotBlank
    @Size(max = 150)
    @Column(name = "first_name")
    private String firstName;

    @NotBlank
    @Size(max = 150)
    @Column(name = "last_name")
    private String lastName;

    @Email
    @NotBlank
    @Size(max = 100)
    private String email;

    @NotBlank
    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "phone_e164")
    private String phoneE164;

    @Column(name = "address_street")
    private String addressStreet;

    @Column(name = "address_number")
    private String addressNumber;

    @Column(name = "address_complement")
    private String addressComplement;

    @Column(name = "address_district")
    private String addressDistrict;

    @Column(name = "address_city")
    private String addressCity;

    @Column(name = "address_state")
    private String addressState;

    @Column(name = "address_postal_code")
    private String addressPostalCode;

    @Column(name = "address_country")
    private String addressCountry = "BR";

    @Column(name = "profile_photo_url")
    private String profilePhotoUrl;

    @Column(name = "is_active")
    private Boolean isActive = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Relacionamento ManyToMany com Role
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    public User() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public User(UUID id, String firstName, String lastName, String email, String passwordHash, String phoneE164,
            String addressStreet, String addressNumber, String addressComplement, String addressDistrict,
            String addressCity, String addressState, String addressPostalCode, String addressCountry,
            String profilePhotoUrl, Boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt,
            Set<Role> roles) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phoneE164 = phoneE164;
        this.addressStreet = addressStreet;
        this.addressNumber = addressNumber;
        this.addressComplement = addressComplement;
        this.addressDistrict = addressDistrict;
        this.addressCity = addressCity;
        this.addressState = addressState;
        this.addressPostalCode = addressPostalCode;
        this.addressCountry = addressCountry;
        this.profilePhotoUrl = profilePhotoUrl;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.roles = roles;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPhoneE164() {
        return phoneE164;
    }

    public void setPhoneE164(String phoneE164) {
        this.phoneE164 = phoneE164;
    }

    public String getAddressStreet() {
        return addressStreet;
    }

    public void setAddressStreet(String addressStreet) {
        this.addressStreet = addressStreet;
    }

    public String getAddressNumber() {
        return addressNumber;
    }

    public void setAddressNumber(String addressNumber) {
        this.addressNumber = addressNumber;
    }

    public String getAddressComplement() {
        return addressComplement;
    }

    public void setAddressComplement(String addressComplement) {
        this.addressComplement = addressComplement;
    }

    public String getAddressDistrict() {
        return addressDistrict;
    }

    public void setAddressDistrict(String addressDistrict) {
        this.addressDistrict = addressDistrict;
    }

    public String getAddressCity() {
        return addressCity;
    }

    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity;
    }

    public String getAddressState() {
        return addressState;
    }

    public void setAddressState(String addressState) {
        this.addressState = addressState;
    }

    public String getAddressPostalCode() {
        return addressPostalCode;
    }

    public void setAddressPostalCode(String addressPostalCode) {
        this.addressPostalCode = addressPostalCode;
    }

    public String getAddressCountry() {
        return addressCountry;
    }

    public void setAddressCountry(String addressCountry) {
        this.addressCountry = addressCountry;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    // Métodos auxiliares para compatibilidade com código existente
    public Set<String> getRoleNames() {
        if (roles == null)
            return null;
        return roles.stream().map(Role::getName).collect(java.util.stream.Collectors.toSet());
    }

    public String getName() {
        return firstName + " " + lastName;
    }

    public String getFullName() {
        return getName();
    }

    public void setRolesByNames(Set<String> roleNames) {
        if (roleNames == null) {
            this.roles = null;
            return;
        }
        this.roles = roleNames.stream()
                .map(name -> {
                    Role role = new Role();
                    role.setName(name);
                    return role;
                })
                .collect(java.util.stream.Collectors.toSet());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof User))
            return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phoneE164='" + phoneE164 + '\'' +
                ", addressStreet='" + addressStreet + '\'' +
                ", addressNumber='" + addressNumber + '\'' +
                ", addressComplement='" + addressComplement + '\'' +
                ", addressDistrict='" + addressDistrict + '\'' +
                ", addressCity='" + addressCity + '\'' +
                ", addressState='" + addressState + '\'' +
                ", addressPostalCode='" + addressPostalCode + '\'' +
                ", addressCountry='" + addressCountry + '\'' +
                ", profilePhotoUrl='" + profilePhotoUrl + '\'' +
                ", isActive=" + isActive +
                ", roles=" + roles +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
