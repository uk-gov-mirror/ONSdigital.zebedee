package com.github.onsdigital.zebedee.model.publishing.verify;

import com.github.onsdigital.zebedee.model.publishing.client.TrainClient;
import com.github.onsdigital.zebedee.reader.CollectionReader;
import com.github.onsdigital.zebedee.reader.Resource;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

import static com.github.onsdigital.zebedee.logging.CMSLogEvent.error;
import static java.util.Objects.requireNonNull;

public class VerifyHashTask implements Callable<Boolean> {

    private static final String HASH_INCORRECT_ERR
            = "file hash from remote server did not match the value calculated locally";

    private String collectionID;
    private CollectionReader collectionReader;
    private String host;
    private String transactionId;
    private String uri;
    private TrainClient trainClient;

    private VerifyHashTask(Builder builder) {
        this.collectionID = requireNonNull(builder.getCollectionID());
        this.collectionReader = requireNonNull(builder.getReader());
        this.host = requireNonNull(builder.getTrainHost());
        this.transactionId = requireNonNull(builder.getTransactionId());
        this.uri = requireNonNull(builder.getUri());
        this.trainClient = requireNonNull(builder.getTrainClient());
    }

    @Override
    public Boolean call() throws Exception {
        GetContentHashEntity entity = trainClient.getContentHash(host, transactionId, uri);
        String expected = getExpectedHashValue();

        if (StringUtils.equals(entity.getHash(), expected)) {
            return true;
        } else {
            HashVerificationException ex = new HashVerificationException(HASH_INCORRECT_ERR, collectionID, host,
                    transactionId, uri);
            error().exceptionAll(ex)
                    .data("train_host", host)
                    .data("transaction_id", transactionId)
                    .collectionID(collectionID)
                    .uri(uri)
                    .log(HASH_INCORRECT_ERR);
            throw ex;
        }
    }

    private String getExpectedHashValue() throws IOException {
        try (
                Resource resource = collectionReader.getResource(uri);
                InputStream in = resource.getData();
                BufferedInputStream buf = new BufferedInputStream(in)
        ) {
            return DigestUtils.sha1Hex(buf);
        } catch (Exception ex) {
            throw new HashVerificationException("error calculating content hash", collectionID, host, transactionId,
                    uri);
        }
    }

    public static class Builder {
        private String collectionID;
        private CollectionReader reader;
        private String trainHost;
        private String transactionId;
        private String uri;
        private TrainClient trainClient;


        public Builder collectionID(String collectionID) {
            this.collectionID = collectionID;
            return this;
        }

        public Builder collectionReader(CollectionReader reader) {
            this.reader = reader;
            return this;
        }

        public Builder trainHost(String host) {
            this.trainHost = host;
            return this;
        }

        public Builder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public Builder contentURI(String uri) {
            this.uri = uri;
            return this;
        }

        public Builder trainClient(TrainClient trainClient) {
            this.trainClient = trainClient;
            return this;
        }

        public VerifyHashTask build() {
            return new VerifyHashTask(this);
        }

        public String getCollectionID() {
            return this.collectionID;
        }

        public CollectionReader getReader() {
            return this.reader;
        }

        public String getTrainHost() {
            return this.trainHost;
        }

        public String getTransactionId() {
            return this.transactionId;
        }

        public String getUri() {
            return this.uri;
        }

        public TrainClient getTrainClient() {
            return this.trainClient;
        }
    }
}
