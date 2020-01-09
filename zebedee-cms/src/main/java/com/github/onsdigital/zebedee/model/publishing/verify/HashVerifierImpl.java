package com.github.onsdigital.zebedee.model.publishing.verify;

import com.github.onsdigital.zebedee.json.CollectionDescription;
import com.github.onsdigital.zebedee.model.Collection;
import com.github.onsdigital.zebedee.model.publishing.client.TrainClient;
import com.github.onsdigital.zebedee.reader.CollectionReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class HashVerifierImpl implements HashVerifier {

    private static final ExecutorService pool = Executors.newFixedThreadPool(20);

    private TrainClient trainClient;

    public HashVerifierImpl(final TrainClient trainClient) {
        this.trainClient = trainClient;
    }

    public void verifyPublishingTransactionHashes(List<String> uris, Collection collection,
                                                  CollectionReader collectionReader)
            throws ExecutionException, InterruptedException {

        List<Callable<Boolean>> tasks = createVerifyTasks(collection, collectionReader, uris);
        List<Future<Boolean>> results = executeTasks(tasks);
        checkResults(results);
    }

    private List<Callable<Boolean>> createVerifyTasks(Collection collection, CollectionReader collectionReader,
                                                      List<String> uris) {
        List<Callable<Boolean>> tasks = new ArrayList<>();

        for (Map.Entry<String, String> entry : getTransactionIdMap(collection)) {
            String trainHost = entry.getKey();
            String transactionId = entry.getValue();

            for (String uri : uris) {

                tasks.add(new VerifyHashTask.Builder()
                        .collectionID(collection.getId())
                        .collectionReader(collectionReader)
                        .trainHost(trainHost)
                        .transactionId(transactionId)
                        .contentURI(uri)
                        .trainClient(trainClient)
                        .build());
            }
        }
        return tasks;
    }

    private Set<Map.Entry<String, String>> getTransactionIdMap(Collection collection) {
        if (collection == null) {
            throw new IllegalArgumentException("TODO");
        }

        CollectionDescription description = collection.getDescription();
        if (description == null) {
            throw new IllegalArgumentException("TODO");
        }

        Map<String, String> transactionIdsMap = description.getPublishTransactionIds();
        if (transactionIdsMap == null || transactionIdsMap.isEmpty()) {
            // TODO
        }
        return transactionIdsMap.entrySet();
    }

    private List<Future<Boolean>> executeTasks(List<Callable<Boolean>> tasks) throws InterruptedException {
        return pool.invokeAll(tasks);
    }

    private void checkResults(List<Future<Boolean>> verifyResults) throws ExecutionException, InterruptedException {
        for (Future<Boolean> result : verifyResults) {
            result.get();
        }
    }
}
