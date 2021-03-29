package com.github.onsdigital.zebedee.model;

import com.github.onsdigital.zebedee.Zebedee;
import com.github.onsdigital.zebedee.exceptions.BadRequestException;
import com.github.onsdigital.zebedee.exceptions.NotFoundException;
import com.github.onsdigital.zebedee.exceptions.UnauthorizedException;
import com.github.onsdigital.zebedee.keyring.KeyringException;
import com.github.onsdigital.zebedee.reader.CollectionReader;
import com.github.onsdigital.zebedee.reader.ContentReader;
import com.github.onsdigital.zebedee.session.model.Session;
import com.github.onsdigital.zebedee.user.model.User;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.Path;

import static com.github.onsdigital.zebedee.reader.configuration.ReaderConfiguration.get;

public class ZebedeeCollectionReader extends CollectionReader {

    static final String ZEBEDEE_NULL_ERR =
            "error constructing ZebedeeCollectionReader zebedee instance required but was null";
    static final String PERMISSIONS_SERVICE_NULL_ERR =
            "error constructing ZebedeeCollectionReader perissions service required but was null";
    static final String USERS_SERVICE_NULL_ERR =
            "error constructing ZebedeeCollectionReader users service required but was null";
    static final String KEYRING_NULL_ERR =
            "error constructing ZebedeeCollectionReader keyring requred but was null";
    static final String COLLECTION_NULL_ERR =
            "error constructing ZebedeeCollectionReader collection required but was null";
    static final String SESSION_NULL_ERR =
            "error constructing ZebedeeCollectionReader session required but was null";
    static final String GET_USER_ERR =
            "error constructing ZebedeeCollectionReader error getting user by email";
    static final String PERMISSIONS_CHECK_ERR =
            "error constructing ZebedeeCollectionReader error checking user collection permissions";
    static final String PERMISSION_DENIED_ERR =
            "error constructing ZebedeeCollectionReader user does not have view permission for collection";
    static final String COLLECTION_KEY_NULL_ERR =
            "error constructing ZebedeeCollectionReader key required but keyring returned null";

    /**
     *
     * @param zebedee
     * @param collection
     * @param session
     * @throws IOException
     * @throws NotFoundException
     * @throws UnauthorizedException
     */
    public ZebedeeCollectionReader(Zebedee zebedee, Collection collection, Session session)
            throws IOException, NotFoundException, UnauthorizedException {
        validate(zebedee, collection, session);

        checkUserAuthorisedToAccessCollection(zebedee, collection, session);

        User user = getUser(zebedee, session);

        SecretKey key = getCollectionKey(zebedee, collection, user);

        init(collection, key);
    }

    private void validate(Zebedee zebedee, Collection collection, Session session)
            throws IOException, NotFoundException, UnauthorizedException {
        if (zebedee == null) {
            throw new IOException(ZEBEDEE_NULL_ERR);
        }

        if (zebedee.getPermissionsService() == null) {
            throw new IOException(PERMISSIONS_SERVICE_NULL_ERR);
        }

        if (zebedee.getCollectionKeyring() == null) {
            throw new IOException(KEYRING_NULL_ERR);
        }

        if (zebedee.getUsersService() == null) {
            throw new IOException(USERS_SERVICE_NULL_ERR);
        }

        if (collection == null) {
            throw new NotFoundException(COLLECTION_NULL_ERR);
        }

        if (session == null) {
            throw new UnauthorizedException(SESSION_NULL_ERR);
        }
    }

    private User getUser(Zebedee zebedee, Session session) throws IOException {
        try {
            return zebedee.getUsersService().getUserByEmail(session.getEmail());
        } catch (Exception ex) {
            throw new IOException(GET_USER_ERR, ex);
        }
    }

    private void checkUserAuthorisedToAccessCollection(Zebedee zebedee, Collection collection, Session session)
            throws IOException, UnauthorizedException {
        boolean isAuthorised = false;
        try {
            isAuthorised = zebedee.getPermissionsService().canView(session, collection.getDescription());
        } catch (Exception ex) {
            throw new IOException(PERMISSIONS_CHECK_ERR, ex);
        }
        if (!isAuthorised) {
            throw new UnauthorizedException(PERMISSION_DENIED_ERR);
        }
    }

    private SecretKey getCollectionKey(Zebedee zebedee, Collection collection, User user)
            throws KeyringException, UnauthorizedException {
        SecretKey key = zebedee.getCollectionKeyring().get(user, collection);

        if (key == null) {
            throw new UnauthorizedException(COLLECTION_KEY_NULL_ERR);
        }

        return key;
    }

    public ZebedeeCollectionReader(Collection collection, SecretKey key) throws BadRequestException, IOException, UnauthorizedException, NotFoundException {
        init(collection, key);
    }

    private void init(Collection collection, SecretKey key) throws NotFoundException, UnauthorizedException, IOException {

        if (collection == null) {
            throw new NotFoundException("Collection not found");
        }

        inProgress = getContentReader(collection, key, collection.getPath(), get().getInProgressFolderName());
        complete = getContentReader(collection, key, collection.getPath(), get().getCompleteFolderName());
        reviewed = getContentReader(collection, key, collection.getPath(), get().getReviewedFolderName());
        root = new CollectionContentReader(collection, key, collection.getPath());
    }

    private ContentReader getContentReader(Collection collection, SecretKey key, Path collectionPath, String folderName) throws UnauthorizedException, IOException {
        return new CollectionContentReader(collection, key, collectionPath.resolve(folderName));
    }
}
