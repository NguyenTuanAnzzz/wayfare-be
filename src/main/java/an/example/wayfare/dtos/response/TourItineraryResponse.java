package an.example.wayfare.dtos.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourItineraryResponse {

    private Long id;
    private Integer dayNumber;
    private String title;
    private String description;
    private Boolean isActive;
}