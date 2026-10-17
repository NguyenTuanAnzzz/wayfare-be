package an.example.wayfare.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourItineraryRequest {

    @NotNull(message = "Ngày lịch trình không được để trống")
    @Min(value = 1, message = "Ngày lịch trình phải bắt đầu từ ngày 1")
    private Integer dayNumber;

    @NotBlank(message = "Tiêu đề lịch trình không được để trống")
    @Size(max = 255, message = "Tiêu đề không được vượt quá 255 ký tự")
    private String title;

    private String description;
}