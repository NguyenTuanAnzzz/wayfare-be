package an.example.wayfare.controllers;

import an.example.wayfare.dtos.request.*;
import an.example.wayfare.dtos.response.*;
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

    @PostMapping("/resend-otp")
    public ResendOtpResponse resendOtp(
            @Valid @RequestBody ResendOtpRequest request
    ) {
        return authService.resendOtp(request);
    }

    @PostMapping("/forgot-password")
    public ForgotPasswordResponse forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        return authService.forgotPassword(request);
    }

    @PostMapping("/resend-reset-password-otp")
    public ResendOtpResponse resendResetPasswordOtp(
            @Valid @RequestBody ResendOtpRequest request
    ) {
        return authService.resendResetPasswordOtp(request);
    }

    @PostMapping("/reset-password")
    public ResetPasswordResponse resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        return authService.resetPassword(request);
    }

}
