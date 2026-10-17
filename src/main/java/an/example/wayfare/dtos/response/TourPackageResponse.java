package an.example.wayfare.dtos.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourPackageResponse {

    private Long id;
    private String name;
    private String description;

    private Integer durationDays;
    private Integer durationNights;

    private BigDecimal price;
    private String location;

    private Boolean isActive;
    private LocalDateTime deactivatedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<TourImageResponse> images;
    private List<TourItineraryResponse> itineraries;
}