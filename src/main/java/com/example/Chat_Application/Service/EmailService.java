//package com.example.Chat_Application.Service;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.stereotype.Service;
//
//@Service
//public class EmailService {
//
//    @Autowired
//    private JavaMailSender mailSender;
//
//    @Value("${app.base-url}")
//    private String baseUrl;
//
//    public void sendVerificationEmail(String toEmail, String token) {
//        String verificationLink = baseUrl + "/auth/verify?token=" + token;  // ✅ Fixed: /auth/verify
//
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(toEmail);
//        message.setSubject("Verify Your Email - Chat App");
//        message.setText(
//                "Welcome to Chat App!\n\n" +
//                        "Please click the link below to verify your email address:\n\n" +
//                        verificationLink + "\n\n" +
//                        "This link will expire in 24 hours.\n\n" +
//                        "If you didn't create this account, please ignore this email.\n\n" +
//                        "Thanks,\nChat App Team"
//        );
//
//        mailSender.send(message);
//        System.out.println("✅ Verification email sent to: " + toEmail);
//    }
//}