package com.github.onsdigital.zebedee.model.publishing.client;

import com.github.onsdigital.zebedee.model.publishing.verify.GetContentHashEntity;

import java.io.IOException;
import java.net.URISyntaxException;

public interface TrainClient {

    GetContentHashEntity getContentHash(String host, String transactionId, String uri) throws IOException,
            URISyntaxException;
}
