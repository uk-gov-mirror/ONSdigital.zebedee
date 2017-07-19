package com.github.onsdigital.zebedee.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.zebedee.audit.Audit;
import com.github.onsdigital.zebedee.exceptions.BadRequestException;
import com.github.onsdigital.zebedee.exceptions.NotFoundException;
import com.github.onsdigital.zebedee.exceptions.UnauthorizedException;
import com.github.onsdigital.zebedee.json.Credentials;
import com.github.onsdigital.zebedee.service.ServiceSupplier;
import com.github.onsdigital.zebedee.session.model.Session;
import com.github.onsdigital.zebedee.user.model.User;
import com.github.onsdigital.zebedee.user.service.UsersService;
import org.apache.commons.mail.EmailException;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import java.io.IOException;

/**
 * API for resetting or changing a password.
 */
@Api
public class PasswordReset {

    /**
     * Wrap static method calls to obtain service in function makes testing easier - class member can be
     * replaced with a mocked giving control of desired behaviour.
     */
    private ServiceSupplier<UsersService> usersServiceSupplier = () -> Root.zebedee.getUsersService();

    /**
     * Reset password
     *
     * Will reset the users password and send a verification email
     *
     * @param request     - a session with appropriate permissions
     * @param response
     * @param credentials - credentials specifiying the email address to reset
     * @return
     * @throws IOException
     * @throws UnauthorizedException
     * @throws BadRequestException
     */
    @POST
    public void resetPassword(HttpServletRequest request, HttpServletResponse response, Credentials credentials) throws IOException, BadRequestException, NotFoundException, EmailException {
        User user = usersServiceSupplier.getService().getUserByEmail(credentials.email);
        usersServiceSupplier.getService().resetPassword(user);
    }
}
