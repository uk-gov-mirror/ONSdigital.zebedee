package com.github.onsdigital.zebedee.model.publishing.client;

import com.github.onsdigital.zebedee.model.publishing.verify.GetContentHashEntity;
import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

public class TrainClientImpl implements TrainClient {

    @Override
    public GetContentHashEntity getContentHash(String host, String transactionId, String uri) throws IOException,
            URISyntaxException {
        HttpGet httpGet = createHttpGet(host, transactionId, uri);

        try (
                CloseableHttpClient client = HttpClients.createDefault();
                CloseableHttpResponse response = client.execute(httpGet)
        ) {
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new TrainClientException(host, transactionId, uri, response.getStatusLine().getStatusCode());
            }
            return getResponseEntity(response.getEntity(), GetContentHashEntity.class);
        }
    }

    private HttpGet createHttpGet(String host, String transactionId, String uri) throws URISyntaxException {
        HttpGet httpGet = new HttpGet(host + "/contentHash");

        httpGet.setURI(new URIBuilder(httpGet.getURI())
                .setParameter("transactionId", transactionId)
                .addParameter("uri", uri)
                .build());

        return httpGet;
    }

    private <T> T getResponseEntity(HttpEntity entity, Class<T> tClass) throws IOException {
        try (
                InputStream inputStream = entity.getContent();
                InputStreamReader reader = new InputStreamReader(inputStream)
        ) {
            return new Gson().fromJson(reader, tClass);
        }
    }
}
