package com.example.jwt_demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.from}")
    private String fromEmail;

    public void sendVerificationEmail(String toEmail, String verificationCode) {
        String subject = "Confirm Account Identity";
        String body = """
                Hi there,

                Thank you for signing up to TestApp! To complete your registration, please verify your email address using the code below.

                Your verification  code is: %s

                This code will expire in 10 minutes. If you did not request this, you can safely ignore this email.

                Thank you,  
                TestApp
                
                
                **This is an auto-generated email. Please do not reply to this message.**
                """.formatted(verificationCode);
        
        sendEmail(toEmail, subject, body);
    }
    
    public void sendVerificationReminderEmail(String toEmail, Long daysLeft) {
    	String subject = "Account Verification Reminder";
        String body = """
	            Hi there,

	            This is a reminder that your account is not yet verified.
	            
	            You have %d day(s) left to verify your account before it is deleted.
	            Please verify your email to keep your account active.

	            Thank you,
	            TestApp
	            
	            
	            **This is an auto-generated email. Please do not reply to this message.**
	            """.formatted(daysLeft);
        sendEmail(toEmail, subject, body);
    }
    
    public void sendPasswordResetEmail(String toEmail, String verificationCode) {
        String subject = "Password Reset Request";
        String body = """
                Hi there,

                We received a request to reset your password. 
                If you made this request, please enter the code below in TestApp to authorize the reset.

                Your verification code is:  %s

                This code will expire in 10 minutes.

                If you did not request a password reset, please ignore this email or contact support if you have concerns.

                Best regards,  
                TestApp
                
                
                **This is an auto-generated email. Please do not reply to this message.**
                """.formatted(verificationCode);
        sendEmail(toEmail, subject, body);
    }
    
    @Async
    public void sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setFrom(fromEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}

