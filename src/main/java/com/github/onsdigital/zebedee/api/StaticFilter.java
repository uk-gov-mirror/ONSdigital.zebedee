package com.github.onsdigital.zebedee.api;

import com.github.davidcarboni.restolino.framework.Filter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Captures requests with a file extension as they do not get routed to the API.
 */
public class StaticFilter implements Filter
{

    @Override
    public boolean filter(HttpServletRequest req, HttpServletResponse res) {
        if (isStaticContentRequest(req)) {
            try {
                new Browse().browse(req, res);
            } catch (IOException e) {
                return true;
            }

            return false;
        }

        return true;
    }

    /**
     * A request is considered to be a static content request if there is a file
     * extension present.
     *
     * @param req
     *            The request.
     * @return If the result of {@link org.apache.commons.io.FilenameUtils#getExtension(String)} is
     *         not blank, true.
     */
    private boolean isStaticContentRequest(HttpServletRequest req) {
        String requestURI = req.getRequestURI();
        String extension = FilenameUtils.getExtension(requestURI);
        return StringUtils.isNotBlank(extension);
    }
}
