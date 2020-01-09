package com.github.onsdigital.zebedee.model.publishing.verify;

import com.github.onsdigital.zebedee.model.Collection;
import com.github.onsdigital.zebedee.reader.CollectionReader;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface HashVerifier {

    void verifyPublishingTransactionHashes(List<String> uris, Collection collectionn,
                                           CollectionReader collectionReader) throws ExecutionException,
            InterruptedException, IOException;
}
