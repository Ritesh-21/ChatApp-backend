package com.example.Chat_Application.Service;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class BrevoEmailService {

    @Value("${brevo.api-key}")
    private String brevoApiKey;

    @Value("${brevo.from-email}")
    private String fromEmail;

    @Value("${brevo.from-name}")
    private String fromName;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.base-url}")
    private String baseUrl;

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    public void sendVerificationEmail(String toEmail, String verificationToken) {
        // ✅ Backend verification endpoint (redirect karega frontend pe)
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

        String jsonPayload = "{"
                + "\"sender\":{\"name\":\"" + fromName + "\",\"email\":\"" + fromEmail + "\"},"
                + "\"to\":[{\"email\":\"" + toEmail + "\"}],"
                + "\"subject\":\"Verify Your Email - Chat Application\","
                + "\"htmlContent\":\"" + escapeJson(htmlContent) + "\""
                + "}";

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(jsonPayload, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(BREVO_API_URL)
                .addHeader("accept", "application/json")
                .addHeader("api-key", brevoApiKey)
                .addHeader("content-type", "application/json")
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                System.out.println("✅ Brevo: Verification email sent successfully to: " + toEmail);
                System.out.println("🔗 Verification link: " + verificationLink);
            } else {
                System.err.println("❌ Brevo error: " + response.code() + " - " + response.body().string());
                throw new RuntimeException("Brevo email sending failed with code: " + response.code());
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to send email via Brevo: " + e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }

    private String escapeJson(String input) {
        return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
