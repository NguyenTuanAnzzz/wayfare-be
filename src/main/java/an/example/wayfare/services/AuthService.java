package an.example.wayfare.services;


import an.example.wayfare.dtos.request.LoginRequest;
import an.example.wayfare.dtos.request.RegisterRequest;
import an.example.wayfare.dtos.request.ResendOtpRequest;
import an.example.wayfare.dtos.request.VerifyEmailRequest;
import an.example.wayfare.dtos.response.LoginResponse;
import an.example.wayfare.dtos.response.RegisterResponse;
import an.example.wayfare.dtos.response.ResendOtpResponse;
import an.example.wayfare.dtos.response.VerifyEmailResponse;
import an.example.wayfare.enums.OtpType;
import an.example.wayfare.enums.Role;
import an.example.wayfare.enums.UserStatus;
import an.example.wayfare.exceptions.AppException;
import an.example.wayfare.models.Otp;
import an.example.wayfare.models.User;
import an.example.wayfare.repositories.OtpRepo;
import an.example.wayfare.repositories.UserRepo;
import an.example.wayfare.securites.UserPrincipal;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Service
public class AuthService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private OtpRepo otpRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    private final SecureRandom random = new SecureRandom();

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtService jwtService;

    @Transactional
    public RegisterResponse register(@Valid RegisterRequest request) {
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new AppException("Email đã được sử dụng", 409);
        }

        if (userRepo.existsByPhone(request.getPhone())) {
            throw new AppException("Số điện thoại đã được sử dụng", 409);
        }

        int age = Period.between(
                request.getDob(),
                LocalDate.now()
        ).getYears();

        if (age < 18) {
            throw new AppException(
                    "Bạn phải đủ 18 tuổi để đăng ký",
                    400
            );
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .dob(request.getDob())
                .build();

        userRepo.save(user);

        String randomOtp = generateOtp();

        Otp otp = Otp.builder()
                .email(request.getEmail())
                .code(passwordEncoder.encode(randomOtp))
                .type(OtpType.REGISTER)
                .build();

        otpRepo.save(otp);

        emailService.sendEmail(
                request.getEmail(),
                "Xác thực tài khoản Wayfare",
                "Mã OTP của bạn là: " + randomOtp
                        + "\n\nMã OTP có hiệu lực trong 1 phút."
        );

        return RegisterResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .message("Đăng ký thành công. Vui lòng kiểm tra email để xác thực tài khoản.")
                .expiresAt(otp.getExpiresAt())
                .build();
    }

    public String generateOtp() {

        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        StringBuilder otp = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(characters.length());
            otp.append(characters.charAt(index));
        }

        return otp.toString();
    }


    public VerifyEmailResponse verifyEmail(@Valid VerifyEmailRequest request) {

        Otp otp = otpRepo
                .findTopByEmailAndTypeOrderByCreatedAtDesc(
                        request.getEmail(),
                        OtpType.REGISTER
                )
                .orElseThrow(() ->
                        new AppException("OTP không tồn tại", 400)
                );

        if (LocalDateTime.now().isAfter(otp.getExpiresAt())) {
            throw new AppException("OTP đã hết hạn", 400);
        }

        if (!passwordEncoder.matches(
                request.getOtp(),
                otp.getCode()
        )) {
            throw new AppException("OTP không chính xác", 400);
        }

        User user = userRepo
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new AppException("Không tìm thấy người dùng", 404)
                );

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new AppException("Email đã được xác thực", 400);
        }

        user.setStatus(UserStatus.ACTIVE);
        userRepo.save(user);

        return VerifyEmailResponse.builder()
                .message("Xác thực email thành công")
                .build();
    }

    public LoginResponse login(
            @Valid LoginRequest request,
            HttpServletResponse response
    ) {
        User user = userRepo
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new AppException(
                                "Email hoặc mật khẩu không chính xác",
                                401
                        )
                );

        if (user.getStatus() == UserStatus.PENDING) {
            throw new AppException(
                    "Tài khoản chưa xác thực email",
                    403
            );
        }

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new AppException(
                    "Tài khoản đã bị khóa",
                    403
            );
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new AppException(
                    "Email hoặc mật khẩu không chính xác",
                    401
            );
        }

        UserPrincipal userPrincipal =
                (UserPrincipal) customUserDetailsService
                        .loadUserByUsername(request.getEmail());

        String accessToken =
                jwtService.generateAccessToken(userPrincipal);

        String refreshToken =
                jwtService.generateRefreshToken(userPrincipal);

        ResponseCookie.ResponseCookieBuilder cookieBuilder =
                ResponseCookie
                        .from("refreshToken", refreshToken)
                        .httpOnly(true)
                        .secure(false)
                        .path("/api/auth")
                        .sameSite("Lax");

        if (request.isRememberMe()) {
            cookieBuilder.maxAge(Duration.ofDays(7));
        }

        ResponseCookie cookie = cookieBuilder.build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );

        return LoginResponse.builder()
                .accessToken(accessToken)
                .build();
    }

    public LoginResponse refresh(String refreshToken) {

        System.out.println("refreshToken = " + refreshToken);

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AppException("Không tìm thấy refresh token", 401);
        }

        try {
            String email = jwtService.extractUsername(refreshToken);
            System.out.println("email = " + email);

            String tokenType = jwtService.extractTokenType(refreshToken);
            System.out.println("tokenType = " + tokenType);

            if (!"REFRESH".equals(tokenType)) {
                throw new AppException("Refresh token không hợp lệ", 401);
            }

            UserPrincipal userPrincipal =
                    (UserPrincipal) customUserDetailsService
                            .loadUserByUsername(email);

            boolean valid =
                    jwtService.isTokenValid(refreshToken, userPrincipal);

            System.out.println("token valid = " + valid);

            if (!valid) {
                throw new AppException(
                        "Refresh token đã hết hạn hoặc không hợp lệ",
                        401
                );
            }

            String accessToken =
                    jwtService.generateAccessToken(userPrincipal);

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .build();

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace(); // QUAN TRỌNG
            throw new AppException(
                    "Refresh token không hợp lệ",
                    401
            );
        }
    }


    @Transactional
    public ResendOtpResponse resendOtp(@Valid ResendOtpRequest request) {

        User user = userRepo
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new AppException(
                                "Không tìm thấy người dùng",
                                404
                        )
                );

        // Nếu email đã xác thực thì không cần gửi OTP nữa
        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new AppException(
                    "Email đã được xác thực",
                    400
            );
        }

        // Tạo OTP mới
        String randomOtp = generateOtp();

        // Lưu OTP mới
        Otp otp = Otp.builder()
                .email(user.getEmail())
                .code(passwordEncoder.encode(randomOtp))
                .type(OtpType.REGISTER)
                .build();

        otpRepo.save(otp);

        // Gửi OTP qua email
        emailService.sendEmail(
                user.getEmail(),
                "Mã xác thực Wayfare",
                "Mã OTP của bạn là: " + randomOtp
                        + "\n\nMã OTP có hiệu lực trong 1 phút."
        );

        return ResendOtpResponse.builder()
                .email(user.getEmail())
                .message("Đã gửi lại mã OTP. Vui lòng kiểm tra email.")
                .expiresAt(otp.getExpiresAt())
                .build();
    }

    public LoginResponse loginWithGoogle(
            String email,
            String name,
            String avatarUrl,
            HttpServletResponse response
    ) {
        User user = userRepo
                .findByEmail(email)
                .orElseGet(() -> {

                    User newUser = User.builder()
                            .email(email)
                            .name(name)
                            .avatarUrl(avatarUrl)
                            .role(Role.CUSTOMER)
                            .status(UserStatus.ACTIVE)
                            .password(null)
                            .build();

                    return userRepo.save(newUser);
                });

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new AppException(
                    "Tài khoản đã bị khóa",
                    403
            );
        }

        UserPrincipal userPrincipal =
                (UserPrincipal) customUserDetailsService
                        .loadUserByUsername(email);

        String accessToken =
                jwtService.generateAccessToken(userPrincipal);

        String refreshToken =
                jwtService.generateRefreshToken(userPrincipal);

        ResponseCookie cookie =
                ResponseCookie
                        .from("refreshToken", refreshToken)
                        .httpOnly(true)
                        .secure(false)
                        .path("/api/auth")
                        .maxAge(Duration.ofDays(7))
                        .sameSite("Lax")
                        .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );

        return LoginResponse.builder()
                .accessToken(accessToken)
                .build();
    }
}
