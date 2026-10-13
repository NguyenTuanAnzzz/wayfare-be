package an.example.wayfare.controllers;


import an.example.wayfare.dtos.response.UserResponse;
import an.example.wayfare.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public UserResponse getCurrentUser(Authentication authentication) {

        return userService.getCurrentUser(authentication);
    }
}
