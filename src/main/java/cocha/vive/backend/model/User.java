package cocha.vive.backend.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@SQLRestriction("is_active = true")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Full User entity including authentication and audit fields")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "User ID", example = "1")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "User first names", example = "Gabriel")
    private String names;

    @Column(name = "first_last_name", nullable = false)
    @Schema(description = "User first last name", example = "Perez")
    private String firstLastName;

    @Column(name = "second_last_name")
    @Schema(description = "User second last name", example = "Quispe")
    private String secondLastName;

    @Column(nullable = false, unique = true)
    @Schema(description = "Unique user email", example = "gabriel.perez@example.com")
    private String email;

    @Column(name = "photo_url", columnDefinition = "TEXT")
    @Schema(description = "Public user photo URL", example = "https://example.com/photos/user-1.jpg")
    private String photoUrl;

    @Column(name = "google_provider_id")
    @Schema(description = "Google provider unique identifier", example = "110012341234123412341")
    private String googleProviderId;

    @Column(name = "facebook_provider_id", unique = true)
    @Schema(description = "Facebook provider unique identifier", example = "123456789012345678901234567890123456")
    private String facebookProviderId;

    @Column(name = "facebook_page_id", unique = true)
    @Schema(description = "Facebook page ID associated with the user", example = "123456789012345")
    private String facebookPageId;

    @Column(name = "document_number")
    @Schema(description = "User document number", example = "8349271")
    private String documentNumber;

    @Column(name = "document_extension", length = 2)
    @Schema(description = "Document extension", example = "1H")
    private String documentExtension;

    @Column(nullable = false)
    @Schema(description = "Assigned application role", example = "ROLE_USER")
    private String role;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Schema(description = "Creation timestamp", example = "2026-03-22T10:15:30")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @Schema(description = "Last update timestamp", example = "2026-03-22T11:05:19")
    private LocalDateTime updatedAt;

    @Builder.Default
    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Schema(description = "Indicates whether the user is active", example = "true")
    private Boolean isActive = true;

    @Column(name = "modified_by_user_id")
    @Schema(description = "ID of the user that last modified this record", example = "73")
    private Long modifiedByUserId;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(this.role));
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.isActive;
    }
}
