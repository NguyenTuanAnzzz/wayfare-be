package an.example.wayfare.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);

            // Giao diện HTML template bọc ngoài nội dung email
            String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #E6EAE8; margin: 0; padding: 30px; color: #1B2428; }
                        .container { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
                        .header { background-color: #14505C; padding: 30px 20px; text-align: center; }
                        .header h1 { color: #ffffff; margin: 0; font-size: 26px; font-weight: 700; letter-spacing: 1px; }
                        .header p { color: #D9A21B; margin: 5px 0 0 0; font-size: 14px; text-transform: uppercase; letter-spacing: 2px; }
                        .content { padding: 40px 30px; font-size: 15px; line-height: 1.6; color: #1B2428; }
                        .footer { background-color: #f8f9fa; padding: 20px; text-align: center; font-size: 13px; color: #666; border-top: 1px solid #eeeeee; }
                        .btn { display: inline-block; background-color: #14505C; color: #ffffff !important; text-decoration: none; padding: 12px 25px; border-radius: 6px; font-weight: bold; margin-top: 20px; }
                        p { margin-top: 0; margin-bottom: 15px; }
                        .content p:last-child { margin-bottom: 0; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>WAYFARE</h1>
                            <p>Journeys done right.</p>
                        </div>
                        <div class="content">
                            %s
                        </div>
                        <div class="footer">
                            <p>© 2026 Wayfare Vietnam. All rights reserved.</p>
                            <p>Email này được gửi tự động, vui lòng không trả lời.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(content.replace("\n", "<br>"));

            // Thiết lập tham số thứ 2 là true để báo cho JavaMail biết đây là email dạng HTML
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Email đã được gửi thành công đến {}", to);
            
        } catch (MessagingException e) {
            log.error("Lỗi khi gửi email HTML đến {}", to, e);
            throw new RuntimeException("Không thể gửi email, vui lòng thử lại sau.", e);
        }
    }
}