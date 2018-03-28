package com.github.onsdigital.zebedee.util;

import com.github.davidcarboni.httpino.Serialiser;
import com.github.onsdigital.zebedee.json.JSONable;
import com.google.gson.JsonSyntaxException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.github.onsdigital.zebedee.logging.ZebedeeLogBuilder.logError;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class JsonUtils {

    public static boolean isValidJson(InputStream inputStream) {
        try {
            Serialiser.deserialise(inputStream, Object.class);
            return true;
        } catch (IOException | JsonSyntaxException e) {
            return false;
        }
    }

    public static void writeResponse(HttpServletResponse response, JSONable body, int status) throws IOException {
        try {
            response.setStatus(status);
            response.setContentType(APPLICATION_JSON);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(body.toJSON());
        } catch (IOException e) {
            logError(e, "error while attempting to write userIdentity to HTTP response").log();
            throw e;
        }
    }
}
