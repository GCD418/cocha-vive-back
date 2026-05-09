package cocha.vive.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private Long unitPrice;

    @Transient
    public Long totalPrice() {
        return quantity * unitPrice;
    }

    @Transient
    public Boolean isExpired() {
        return LocalDateTime.now().isAfter(this.event.getDateEnd());
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_user_id", nullable = false)
    private User buyerUserId;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean used = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event", nullable = false)
    private Event event;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

}
