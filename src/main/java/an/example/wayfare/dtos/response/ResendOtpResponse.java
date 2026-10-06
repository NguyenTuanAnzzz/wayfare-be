package an.example.wayfare.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResendOtpResponse {

    private String email;
    private String message;
    private LocalDateTime expiresAt;
}