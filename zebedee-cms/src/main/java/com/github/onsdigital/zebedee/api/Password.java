package com.github.onsdigital.zebedee.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.zebedee.Zebedee;
import com.github.onsdigital.zebedee.audit.Audit;
import com.github.onsdigital.zebedee.exceptions.BadRequestException;
import com.github.onsdigital.zebedee.exceptions.NotFoundException;
import com.github.onsdigital.zebedee.exceptions.UnauthorizedException;
import com.github.onsdigital.zebedee.json.Credentials;
import com.github.onsdigital.zebedee.session.model.Session;
import com.github.onsdigital.zebedee.user.model.User;
import com.github.onsdigital.zebedee.service.ServiceSupplier;
import com.github.onsdigital.zebedee.user.service.UsersService;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import java.io.IOException;

/**
 * API for resetting or changing a password.
 */
@Api
public class Password {

    /**
     * Wrap static method calls to obtain service in function makes testing easier - class member can be
     * replaced with a mocked giving control of desired behaviour.
     */
    private ServiceSupplier<UsersService> usersServiceSupplier = () -> Root.zebedee.getUsersService();

    /**
     * Update password
     *
     * Will set password as permanent if it is the user updating
     *
     * @param request     - a session with appropriate permissions
     * @param response
     * @param credentials - new credentials
     * @return
     * @throws IOException
     * @throws UnauthorizedException
     * @throws BadRequestException
     */
    @POST
    public String setPassword(HttpServletRequest request, HttpServletResponse response, Credentials credentials) throws IOException, UnauthorizedException, BadRequestException, NotFoundException {
        // If verifying, ignore whether the user is logged in
        if(credentials.getVerify().length() > 0) {
            User user = usersServiceSupplier.getService().getUserByEmail(credentials.email);
            Session session = Root.zebedee.openSession(credentials);

            if(user.getVerifiedEmail()) {
                response.setStatus(HttpStatus.BAD_REQUEST_400);
                return "User already verified";
            }

            if(!user.verify(credentials.getVerify())) {
                response.setStatus(HttpStatus.BAD_REQUEST_400);
                return "Invalid verification code";
            }

            if(!usersServiceSupplier.getService().setPassword(session, credentials)) {
                response.setStatus(HttpStatus.BAD_REQUEST_400);

                Audit.Event.PASSWORD_CHANGED_FAILURE
                        .parameters()
                        .host(request)
                        .user(session.getEmail())
                        .log();

                return "Password not updated for " + credentials.email + " (there may be an issue with the user's keyring password).";
            }

            Audit.Event.PASSWORD_CHANGED_SUCCESS
                    .parameters()
                    .host(request)
                    .user(session.getEmail())
                    .log();
            return "Password updated for " + credentials.email;
        }

        // Get the user session
        Session session = Root.zebedee.getSessionsService().get(request);

        // If the user is not logged in, but they are attempting to change their password, authenticate using the old password
        if (session == null && credentials != null) {
            User user = usersServiceSupplier.getService().getUserByEmail(credentials.email);

            if (user.authenticate(credentials.oldPassword)) {
                Credentials oldPasswordCredentials = new Credentials();
                oldPasswordCredentials.email = credentials.email;
                oldPasswordCredentials.password = credentials.oldPassword;
                session = Root.zebedee.openSession(oldPasswordCredentials);
            }
        }

        // Attempt to change or reset the password:
        if (usersServiceSupplier.getService().setPassword(session, credentials)) {
            Audit.Event.PASSWORD_CHANGED_SUCCESS
                    .parameters()
                    .host(request)
                    .user(session.getEmail())
                    .log();
            return "Password updated for " + credentials.email;
        } else {
            Audit.Event.PASSWORD_CHANGED_FAILURE
                    .parameters()
                    .host(request)
                    .user(session.getEmail())
                    .log();
            return "Password not updated for " + credentials.email + " (there may be an issue with the user's keyring password).";
        }
    }
}
