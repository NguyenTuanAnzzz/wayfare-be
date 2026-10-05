package an.example.wayfare.controllers;

import an.example.wayfare.dtos.request.LoginRequest;
import an.example.wayfare.dtos.request.RegisterRequest;
import an.example.wayfare.dtos.request.VerifyEmailRequest;
import an.example.wayfare.dtos.response.LoginResponse;
import an.example.wayfare.dtos.response.RegisterResponse;
import an.example.wayfare.dtos.response.VerifyEmailResponse;
import an.example.wayfare.services.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request){
        return authService.register(request);
    }

    @PostMapping("/verify-email")
    public VerifyEmailResponse verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request
    ) {
        return authService.verifyEmail(request);
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request, HttpServletResponse response
    ) {
        return authService.login(request, response);
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(
            @CookieValue(value="refreshToken", required = false)
            String refreshToken
    ) {
        return authService.refresh(refreshToken);
    }

}
