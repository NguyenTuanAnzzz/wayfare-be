package an.example.wayfare.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tour_packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "duration_nights", nullable = false)
    private Integer durationNights;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private String location;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt;

    // Một tour có nhiều ảnh
    @OneToMany(
            mappedBy = "tourPackage",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    @Builder.Default
    private List<TourImage> images = new ArrayList<>();

    // Một tour có nhiều ngày lịch trình
    @OneToMany(
            mappedBy = "tourPackage",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    @Builder.Default
    private List<TourItinerary> itineraries = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}