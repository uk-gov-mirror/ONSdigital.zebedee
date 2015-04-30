package com.github.onsdigital.zebedee.model;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class Collections extends ArrayList<Collection> {

    /**
     * Retrieves a collection with the given id.
     *
     * @param id The name to look for.
     * @return If a {@link Collection} matching the given name exists,
     * (according to {@link PathUtils#toFilename(String)}) the collection.
     * Otherwise null.
     */
    public Collection getCollection(String id) {
        Collection result = null;

        if (StringUtils.isNotBlank(id)) {
            for (Collection collection : this) {
                if (StringUtils.equalsIgnoreCase(collection.description.id, id)) {
                    result = collection;
                    break;
                }
            }
        }

        return result;
    }

    public boolean transfer(String email,String uri, Collection source, Collection destination) throws IOException {
        boolean result = false;

            // Move the file
            Path sourcePath = source.find(email, uri);
            Path destinationPath = destination.getInProgressPath(uri);

        PathUtils.moveFilesInDirectory(sourcePath, destinationPath);
            result = true;


        return result;
    }



    /**
     * Determines whether a collection with the given name exists.
     *
     * @param name The name to check for.
     * @return If {@link #getCollection(String)} returns non-null, true.
     */
    public boolean hasCollection(String name) {
        return getCollection(name) != null;
    }
}
