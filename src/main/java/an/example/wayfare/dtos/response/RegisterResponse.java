package an.example.wayfare.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {

    private String message;

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String status;
}