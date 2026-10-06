package an.example.wayfare.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ForgotPasswordResponse {

    private String email;
    private String message;
    private LocalDateTime expiresAt;
}