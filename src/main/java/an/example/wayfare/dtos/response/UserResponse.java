package an.example.wayfare.dtos.response;

import an.example.wayfare.enums.Role;
import an.example.wayfare.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;

    private String email;

    private String name;

    private String phone;

    private LocalDate dob;

    private String avatarUrl;

    private Role role;

    private UserStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


}
