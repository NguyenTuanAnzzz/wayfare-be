package an.example.wayfare.services;

import an.example.wayfare.dtos.request.UpdateUserRequest;
import an.example.wayfare.dtos.response.UserResponse;
import an.example.wayfare.exceptions.AppException;
import an.example.wayfare.models.User;
import an.example.wayfare.repositories.UserRepo;
import an.example.wayfare.securites.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

@Service
public class UserService {


    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CloudinaryService cloudinaryService;

    public UserResponse getCurrentUser(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userPrincipal.getUser();
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .dob(user.getDob())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Transactional
    public UserResponse updateCurrentUser(Authentication authentication, @Valid UpdateUserRequest request) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userPrincipal.getUser();

        if (!Objects.equals(user.getPhone(), request.getPhone())
                && request.getPhone() != null
                && userRepo.existsByPhone(request.getPhone())) {
            throw new AppException("Số điện thoại đã được sử dụng", 409);
        }

        int age = Period.between(
                request.getDob(),
                LocalDate.now()
        ).getYears();

        if (age < 18) {
            throw new AppException("Bạn phải đủ 18 tuổi", 400);
        }
        if (request.getAvatar() != null
                && !request.getAvatar().isEmpty()) {

            String avatarUrl = cloudinaryService.uploadAvatar(
                    request.getAvatar()
            );

            user.setAvatarUrl(avatarUrl);
        }

        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setDob(request.getDob());
        User updatedUser = userRepo.save(user);

        return UserResponse.builder()
                .id(updatedUser.getId())
                .name(updatedUser.getName())
                .email(updatedUser.getEmail())
                .phone(updatedUser.getPhone())
                .dob(updatedUser.getDob())
                .avatarUrl(updatedUser.getAvatarUrl())
                .role(updatedUser.getRole())
                .status(updatedUser.getStatus())
                .createdAt(updatedUser.getCreatedAt())
                .updatedAt(updatedUser.getUpdatedAt())
                .build();
    }
}
