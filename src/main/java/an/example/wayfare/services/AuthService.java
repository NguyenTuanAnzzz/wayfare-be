package an.example.wayfare.services;


import an.example.wayfare.dtos.request.RegisterRequest;
import an.example.wayfare.dtos.request.VerifyEmailRequest;
import an.example.wayfare.dtos.response.RegisterResponse;
import an.example.wayfare.dtos.response.VerifyEmailResponse;
import an.example.wayfare.enums.OtpType;
import an.example.wayfare.enums.UserStatus;
import an.example.wayfare.exceptions.AppException;
import an.example.wayfare.models.Otp;
import an.example.wayfare.models.User;
import an.example.wayfare.repositories.OtpRepo;
import an.example.wayfare.repositories.UserRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
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
}
