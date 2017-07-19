package com.github.onsdigital.zebedee.email.service;

import com.github.onsdigital.zebedee.user.model.User;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by iankent on 10/07/2017.
 */
public class EmailServiceImpl implements EmailService {

    protected String hostname;
    protected Integer port;
    protected String username;
    protected String password;
    protected String senderEmail;
    protected String senderName;
    protected String florenceURL;

    protected static String verificationEmailSubject = "Please verify your email address";
    protected static String createUserVerificationEmailTemplate = "Dear %s\n\nA Florence account has been created for %s.\n\nPlease use the following link to verify your email address:\n\n%s\n\nThank you,\nFlorence Admin";

    protected static String passwordResetEmailSubject = "Reset your password";
    protected static String passwordResetEmailTemplate = "Dear %s\n\nA password reset has been requested for %s.\n\nPlease use the following link to create a new password:\n\n%s\n\nThank you,\nFlorence Admin";

    public EmailServiceImpl(String florenceURL, String hostname, Integer port, String username, String password, String senderEmail, String senderName) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
        this.florenceURL = florenceURL;
    }

    @Override
    public void SendCreateUserVerificationEmail(User user, String verificationCode) throws EmailException {
        String message = String.format(createUserVerificationEmailTemplate , user.getName(), user.getEmail(), florenceURL + String.format("/verify?email=%s&code=%s", user.getEmail(), verificationCode));
        sendEmail(user.getVerificationEmail(), user.getName(), verificationEmailSubject, message);
    }

    @Override
    public void SendPasswordResetEmail(User user, String verificationCode) throws EmailException {
        String message = String.format(passwordResetEmailTemplate , user.getName(), user.getEmail(), florenceURL + String.format("/verify?email=%s&code=%s", user.getEmail(), verificationCode));
        sendEmail(user.getVerificationEmail(), user.getName(), passwordResetEmailSubject, message);
    }

    protected void sendEmail(String recipientEmail, String recipientName, String subject, String message) throws EmailException {
        Email email = new SimpleEmail();
        email.setHostName(hostname);
        email.setSmtpPort(port);
        email.setSSLOnConnect(false);
        email.setStartTLSEnabled(true);
        // TODO need to setStartTLSRequired (configurable for local testing)

        if(username.length() > 0 || password.length() > 0) {
            email.setAuthenticator(new DefaultAuthenticator(username, password));
        }

        email.setFrom(senderEmail);
        email.setSubject(subject);

        Map<String, String> headers = new HashMap<>();
        headers.put("From", String.format("%s <%s>", senderName, senderEmail));
        headers.put("To", String.format("%s <%s>", recipientName, recipientEmail));
        email.setHeaders(headers);

        email.setMsg(message);
        email.addTo(recipientEmail);

        email.send();
    }
}
