package com.github.onsdigital.zebedee.email.service;

import com.github.onsdigital.zebedee.user.model.User;
import org.apache.commons.mail.EmailException;

/**
 * Created by iankent on 10/07/2017.
 */
public interface EmailService {

    // Sends a verification email to a user
    void SendCreateUserVerificationEmail(User user, String verificationCode) throws EmailException;

}
