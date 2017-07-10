package com.github.onsdigital.zebedee.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.zebedee.audit.Audit;
import com.github.onsdigital.zebedee.configuration.Configuration;
import com.github.onsdigital.zebedee.exceptions.BadRequestException;
import com.github.onsdigital.zebedee.exceptions.NotFoundException;
import com.github.onsdigital.zebedee.json.Credentials;
import com.github.onsdigital.zebedee.json.Verification;
import com.github.onsdigital.zebedee.logging.ZebedeeLogBuilder;
import com.github.onsdigital.zebedee.service.ServiceSupplier;
import com.github.onsdigital.zebedee.session.service.SessionsService;
import com.github.onsdigital.zebedee.user.model.User;
import com.github.onsdigital.zebedee.user.service.UsersService;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import java.io.IOException;

/**
 * API for processing login requests.
 */
@Api
public class Verify {

    private static final String VERIFY_SUCCESS_MSG = "Florence verify success";
    private static final String VERIFY_CODE_FAILURE_MSG = "Verification failure";

    /**
     * Wrap static method calls to obtain service in function makes testing easier - class member can be
     * replaced with a mocked giving control of desired behaviour.
     */
    private ServiceSupplier<UsersService> usersServiceSupplier = () -> Root.zebedee.getUsersService();

    /**
     * Validates verification code with Zebedee.
     *
     * @param request     This should contain a {@link Verify} Json object.
     * @param response    <ul>
     *                    <li>If authentication succeeds: a new or existing session ID.</li>
     *                    <li>If verify are not provided:  {@link HttpStatus#BAD_REQUEST_400}</li>
     *                    <li>If authentication fails:  {@link HttpStatus#UNAUTHORIZED_401}</li>
     *                    </ul>
     * @param verify The user email and password.
     * @throws IOException
     */
    @POST
    public String verify(HttpServletRequest request, HttpServletResponse response, Verification verify) throws IOException, NotFoundException, BadRequestException {

        if (verify == null || StringUtils.isBlank(verify.email)) {
            response.setStatus(HttpStatus.BAD_REQUEST_400);
            return "Email is required";
        }

        User user = usersServiceSupplier.getService().getUserByEmail(verify.email);
        if(user == null) {
            response.setStatus(HttpStatus.NOT_FOUND_404);
            return "User not found";
        }

        if(user.getVerifiedEmail()) {
            response.setStatus(HttpStatus.BAD_REQUEST_400);
            return "Email already verified";
        }

        boolean result = user.verify(verify.code);

        if (!result) {
            response.setStatus(HttpStatus.UNAUTHORIZED_401);
            Audit.Event.VERIFY_CODE_INVALID.parameters().host(request).user(verify.email).log();
            ZebedeeLogBuilder.logInfo(VERIFY_CODE_FAILURE_MSG).user(verify.email).log();
            return "Verification failed.";
        }

        response.setStatus(HttpStatus.EXPECTATION_FAILED_417);
        Audit.Event.LOGIN_EMAIL_VERIFICATION_REQUIRED.parameters().host(request).user(verify.email).log();
        ZebedeeLogBuilder.logInfo(VERIFY_SUCCESS_MSG).user(verify.email).log();
        return "Verification OK";
    }

}
