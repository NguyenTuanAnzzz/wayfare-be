package an.example.wayfare.dtos.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourImageResponse {

    private Long id;
    private String imageUrl;
    private Boolean isThumbnail;
    private Boolean isActive;
}