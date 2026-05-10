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

import java.time.LocalDateTime;

@Entity
@Table(name = "categories")
@SQLRestriction("is_active = true")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Full Category entity including audit fields")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Category ID", example = "1")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Category's name", example = "Music")
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Schema(description = "Category's description", example = "Musical group playing songs")
    private String description;

    @Column(name = "identifying_icon", nullable = false, columnDefinition = "TEXT")
    @Schema(description = "URL of category's identifying Icon", example = "https://cdn-icons-png.flaticon.com/512/2418/2418779.png")
    private String identifyingIcon;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Schema(description = "Category's create timestamp for audit purposes", example = "2007-12-03T10:15:30")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @Schema(description = "Category's update timestamp for audit purposes", example = "2008-12-03T10:15:30")
    private LocalDateTime updatedAt;

    @Builder.Default
    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Schema(description = "Flag for soft delete. Only records that has this flag as true are shown", example = "true")
    private Boolean isActive = true;

    @Column(name = "modified_by_user_id")
    @Schema(description = "ID of the user who has modified the category record", example = "73")
    private Long modifiedByUserId;
}
