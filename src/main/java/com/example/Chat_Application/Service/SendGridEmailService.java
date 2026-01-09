package com.example.Chat_Application.Service;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SendGridEmailService {
    @Value("${sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from-email}")
    private String fromEmail;

    @Value("${sendgrid.from-name}")
    private String fromName;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.base-url}")  // ✅ Backend URL add kiya
    private String baseUrl;

    public void sendVerificationEmail(String toEmail, String verificationToken) {
        Email from = new Email(fromEmail, fromName);
        Email to = new Email(toEmail);
        String subject = "Verify Your Email - Chat Application";

        // ✅ FIXED: Backend verification endpoint use karo (not frontend!)
        String verificationLink = baseUrl + "/auth/verify?token=" + verificationToken;

        String htmlContent = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
                ".content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }" +
                ".button { display: inline-block; padding: 15px 30px; background: #667eea; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }" +
                ".footer { text-align: center; margin-top: 20px; color: #999; font-size: 12px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>Welcome to Chat Application! 🎉</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Hi there!</p>" +
                "<p>Thank you for signing up! Please verify your email address to start chatting with your friends.</p>" +
                "<div style='text-align: center;'>" +
                "<a href='" + verificationLink + "' class='button'>Verify Email Address</a>" +
                "</div>" +
                "<p>Or copy and paste this link in your browser:</p>" +
                "<p style='word-break: break-all; background: #fff; padding: 10px; border-radius: 5px;'>" + verificationLink + "</p>" +
                "<p><strong>This link will expire in 24 hours.</strong></p>" +
                "<p>If you didn't create this account, please ignore this email.</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>© 2026 Chat Application. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";

        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("✅ Verification email sent successfully to: " + toEmail);
            } else {
                System.err.println("❌ SendGrid error: " + response.getStatusCode() + " - " + response.getBody());
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }
}
