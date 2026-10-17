package an.example.wayfare.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourPackageRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    private String description;

    @NotNull
    @Min(1)
    private Integer durationDays;

    @NotNull
    @Min(0)
    private Integer durationNights;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal price;

    @NotBlank
    @Size(max = 255)
    private String location;

    @NotEmpty
    private List<MultipartFile> images;

    @NotEmpty
    @Valid
    private List<TourItineraryRequest> itineraries;
}