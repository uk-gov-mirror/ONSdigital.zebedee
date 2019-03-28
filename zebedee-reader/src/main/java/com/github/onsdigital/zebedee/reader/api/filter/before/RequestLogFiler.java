package com.github.onsdigital.zebedee.reader.api.filter.before;

import com.github.davidcarboni.restolino.framework.PreFilter;
import com.github.davidcarboni.restolino.framework.Priority;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.github.onsdigital.zebedee.logging.ReaderLogger.info;

@Priority(1)
public class RequestLogFiler implements PreFilter {

    @Override
    public boolean filter(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        info().beginHTTP(httpServletRequest).log("request received");
        return true;
    }
}
