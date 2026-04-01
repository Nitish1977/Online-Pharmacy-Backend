package com.pharmacy.notification.service;

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

    public void sendWelcomeEmail(String toEmail, String name) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("nitish1977022@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("Welcome to PharmEasy! 🎉");
            helper.setText(buildWelcomeHtml(name), true);

            mailSender.send(message);
            log.info("Welcome email sent successfully to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage());
        }
    }

    public void sendLowStockAlert(String toEmail, String medicineName, int remainingStock) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("nitish1977022@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("🚨 Low Stock Alert: " + medicineName);

            String html = """
                    <!DOCTYPE html>
                    <html>
                    <body>
                        <h2>Low Stock Alert</h2>
                        <p>The stock for <strong>%s</strong> has dropped below the threshold.</p>
                        <p>Remaining Stock: <strong style="color:red">%d</strong></p>
                        <p>Please restock immediately.</p>
                    </body>
                    </html>
                    """.formatted(medicineName, remainingStock);

            helper.setText(html, true);

            mailSender.send(message);
            log.info("Low stock alert email sent successfully to {} for {}", toEmail, medicineName);
        } catch (Exception e) {
            log.error("Failed to send low stock alert email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildWelcomeHtml(String name) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 0; }
                        .container { max-width: 600px; margin: 40px auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.08); }
                        .header { background: linear-gradient(135deg, #1a73e8, #0d47a1); padding: 40px 30px; text-align: center; }
                        .header h1 { color: #ffffff; margin: 0; font-size: 26px; }
                        .header p { color: #bbdefb; margin: 8px 0 0; font-size: 14px; }
                        .body { padding: 30px; color: #333333; line-height: 1.7; }
                        .body h2 { color: #1a73e8; margin-top: 0; }
                        .features { background: #f0f4ff; border-radius: 8px; padding: 20px; margin: 20px 0; }
                        .features ul { list-style: none; padding: 0; margin: 0; }
                        .features li { padding: 8px 0; border-bottom: 1px solid #e0e7ff; }
                        .features li:last-child { border-bottom: none; }
                        .features li::before { content: '✅ '; }
                        .cta { text-align: center; margin: 30px 0; }
                        .cta a { background: #1a73e8; color: #ffffff; padding: 14px 36px; border-radius: 8px; text-decoration: none; font-weight: 600; display: inline-block; }
                        .footer { background: #f4f6f9; padding: 20px 30px; text-align: center; color: #888888; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>💊 PharmEasy</h1>
                            <p>Your Trusted Online Pharmacy</p>
                        </div>
                        <div class="body">
                            <h2>Welcome, %s! 👋</h2>
                            <p>Thank you for creating your account with <strong>PharmEasy</strong>. We're thrilled to have you on board!</p>
                            <div class="features">
                                <ul>
                                    <li>Browse a wide range of medicines & health products</li>
                                    <li>Place orders quickly and securely</li>
                                    <li>Track your orders in real-time</li>
                                    <li>Get exclusive offers & discounts</li>
                                </ul>
                            </div>
                            <p>If you have any questions, feel free to reach out to our support team. We're here to help!</p>
                            <div class="cta">
                                <a href="#">Start Shopping Now</a>
                            </div>
                        </div>
                        <div class="footer">
                            <p>© 2026 PharmEasy. All rights reserved.</p>
                            <p>This is an automated message. Please do not reply.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(name);
    }
}
