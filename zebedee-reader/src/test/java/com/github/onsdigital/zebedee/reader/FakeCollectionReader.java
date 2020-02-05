package com.github.onsdigital.zebedee.reader;

import com.github.onsdigital.zebedee.content.collection.Collection;
import com.github.onsdigital.zebedee.content.util.ContentUtil;
import com.github.onsdigital.zebedee.exceptions.CollectionNotFoundException;
import com.github.onsdigital.zebedee.exceptions.NotFoundException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.onsdigital.zebedee.reader.configuration.ReaderConfiguration.get;

public class FakeCollectionReader extends CollectionReader {

    private Path collections;

    /**
     * @param collectionsFolderPath path of the collections folder
     */
    public FakeCollectionReader(String collectionsFolderPath, String collectionId) throws NotFoundException, IOException {
        if (collectionsFolderPath == null) {
            throw new NullPointerException("Collections folder can not be null");
        }
        this.collections = Paths.get(collectionsFolderPath);
        Path collectionsPath = findCollectionPath(collectionId);
        inProgress = getContentReader(collectionsPath, get().getInProgressFolderName());
        complete = getContentReader(collectionsPath, get().getCompleteFolderName());
        reviewed = getContentReader(collectionsPath, get().getReviewedFolderName());
    }

    //TODO: If collection folder names were ids or we saved cookie as collection's name we would not need to search collection, but just read the path
    //Finds collection name with given id
    private Path findCollectionPath(String collectionId) throws IOException, NotFoundException, CollectionNotFoundException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(collections, "*.{json}")) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    continue;
                } else {
                    try (InputStream fileStream = Files.newInputStream(path)) {
                        Collection collection = ContentUtil.deserialise(fileStream, Collection.class);
                        if (StringUtils.equalsIgnoreCase(collection.getId(), collectionId)) {
                            return collections.resolve(FilenameUtils.removeExtension(path.getFileName().toString())); //get directory with same name

                        }
                    }
                }
            }
            throw new CollectionNotFoundException("Collection with given id not found, id:" + collectionId);
        }
    }

    private ContentReader getContentReader(Path collectionPath, String folderName) {
        return new FileSystemContentReader(collectionPath.resolve(folderName));
    }
}
