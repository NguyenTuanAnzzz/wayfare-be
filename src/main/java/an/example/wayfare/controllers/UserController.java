package an.example.wayfare.controllers;


import an.example.wayfare.dtos.request.UpdateUserRequest;
import an.example.wayfare.dtos.response.UserResponse;
import an.example.wayfare.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public UserResponse getCurrentUser(Authentication authentication) {

        return userService.getCurrentUser(authentication);
    }

    @PutMapping(
            value = "/me",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public UserResponse updateCurrentUser(
            Authentication authentication,
            @Valid @ModelAttribute UpdateUserRequest request) {

        return userService.updateCurrentUser(authentication, request);
    }
}
