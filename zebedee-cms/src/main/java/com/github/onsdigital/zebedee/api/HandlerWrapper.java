package com.github.onsdigital.zebedee.api;

import com.github.onsdigital.zebedee.api.wrapper.HandlerFunc;
import com.github.onsdigital.zebedee.exceptions.ZebedeeException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.info;

public class HandlerWrapper<T, R> {

    private HandlerFunc<T, R> handlerFunc;

    public HandlerWrapper(HandlerFunc<T, R> handlerFunc) {
        this.handlerFunc = handlerFunc;
    }

    public R handle(HttpServletRequest req, HttpServletResponse resp, T t) throws IOException, ZebedeeException {
        info().beginHTTP(req).log("request received");
        try {
            return handlerFunc.doHandle(req, resp, t);
        } catch (Exception e) {
            // TODO - do something nicer here.
            throw e;
        } finally {
            info().endHTTP(req, resp).log("request compelete");
        }
    }
}
