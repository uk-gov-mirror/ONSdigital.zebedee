package com.github.onsdigital.zebedee.search.indexing;

import com.github.onsdigital.zebedee.reader.configuration.ReaderConfiguration;
import com.github.onsdigital.zebedee.util.URIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

import static com.github.onsdigital.zebedee.search.indexing.SearchBoostTermsResolver.getSearchTermResolver;
import static com.github.onsdigital.zebedee.util.PathUtils.toRelativeUri;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Searches the file system
 */
public class FileScanner {


    private Path root;

    public FileScanner() {
        root = Paths.get(ReaderConfiguration.getConfiguration()
                                            .getContentDir());
    }

    private static boolean isDataFile(String fileName) {
        return fileName.endsWith("data.json");
    }

    private static boolean isNotPreviousVersion(Path fileName) {
        return !"previous".equals(fileName.getFileName()
                                          .toString());
    }

    public List<Document> scan() throws IOException {
        return scan(null);
    }

    public List<Document> scan(String path) throws IOException {
        Path dir = root;
        List<Document> documents = new LinkedList<>();
        if (isEmpty(path) == false) {
            dir = root.resolve(URIUtils.removeLeadingSlash(path));
        }


        Scanner scanner = new Scanner(dir, null, root);
        List<ForkJoinTask<Document>> fork = scanner.getDocumentBuilders();
        fork.forEach(db -> documents.add(db.join()));
        return documents;
    }

    /**
     * Iterates through the file system from a specified root directory and
     * stores the file names
     */
    static class Scanner {

        private List<Document> fileNames = new ArrayList<>();
        private Path dir;
        private Path root;
        private Set<List<String>> searchTerms;


        Scanner(final Path dir, final Set<List<String>> searchTerms, final Path root) {

            this.dir = dir;
            this.root = root;
            if (searchTerms != null) {
                this.searchTerms = searchTerms;
            }
            else {
                this.searchTerms = new HashSet<>();
            }

        }


        private String toUri(Path path) {
            return path.toAbsolutePath()
                       .toString();
        }

        private List<ForkJoinTask<Document>> getDocumentBuilders()
                throws IOException {

            if (fileNames == null || dir == null) {
                throw new IllegalArgumentException(
                        "List of fileNames and Path dir cannot be null");
            }

            if (searchTerms == null) {
                searchTerms = new HashSet<>();
            }

            List<ForkJoinTask<Document>> subTasks = new ArrayList<>();
            // java 7 try-with-resources automatically closes streams after use
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {

                for (Path path : stream) {
                    String uri = toRelativeUri(root, path.getParent()).toString();
                    boolean directory = path.toFile()
                                            .isDirectory();
                    if (directory && isNotPreviousVersion(path
                                                         )) {
                        recurseSubDirectories(subTasks, path, uri);

                    }
                    else if (!directory) {
                        createDocument(searchTerms, subTasks, path, uri);
                    }
                }
            }

            return subTasks;
        }

        private void recurseSubDirectories(final List<ForkJoinTask<Document>> subTasks, final Path path,
                                           final String uri) throws IOException {
            List<String> termsForPrefix = getSearchTermResolver().getTermsForPrefix(uri);
            Set<List<String>> subTermsForPrefix = createChildTerms(searchTerms, termsForPrefix);
            addSubDocumentBuilders(subTermsForPrefix, subTasks, path);
        }

        private void createDocument(final Set<List<String>> searchTerms, final List<ForkJoinTask<Document>> subTasks,
                                    final Path path, final String uri) {
            String fullPath = toUri(path);
            if (isDataFile(fullPath)) {
                List<String> terms = getSearchTermResolver().getTerms(uri);
                Set<List<String>> subTermsForPrefix = createChildTerms(searchTerms, terms);
                subTasks.add(new DocumentBuilder(uri, subTermsForPrefix).fork());

            }
        }

        private Set<List<String>> createChildTerms(final Set<List<String>> searchTerms,
                                                   final List<String> termsForPrefix) {
            Set<List<String>> subTermsForPrefix = new HashSet<>();
            subTermsForPrefix.addAll(searchTerms);
            subTermsForPrefix.add(termsForPrefix);
            return subTermsForPrefix;
        }

        private void addSubDocumentBuilders(final Set<List<String>> searchTerms,
                                            final List<ForkJoinTask<Document>> subTasks,
                                            final Path path) throws IOException {
            subTasks.addAll(new Scanner(path, searchTerms, root).getDocumentBuilders());

        }
    }

    static class DocumentBuilder extends RecursiveTask<Document> {


        private final String uri;
        private final Set<List<String>> subTermsForPrefix;

        DocumentBuilder(final String uri, final Set<List<String>> subTermsForPrefix) {
            this.uri = uri;
            this.subTermsForPrefix = subTermsForPrefix;
        }

        @Override
        protected Document compute() {

            return new Document(uri, subTermsForPrefix);
        }
    }

}