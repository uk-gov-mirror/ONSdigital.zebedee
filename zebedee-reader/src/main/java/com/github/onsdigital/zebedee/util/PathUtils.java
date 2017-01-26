package com.github.onsdigital.zebedee.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by bren on 10/09/15.
 */
public class PathUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(PathUtils.class);
    public static final String JSON = "json";

    public static URI toRelativeUri(Path root, Path child) {

        // Handle spaces in filenames. An exception is thrown if the URI is created with spaces, so we encode them.
        if (child.toString()
                 .contains(" ")) {
            child = Paths.get(child.toString()
                                   .replace(" ", "%20"));
        }

        return URI.create("/" + URIUtils.removeTrailingSlash(root.toUri()
                                                                 .relativize(child.toUri())
                                                                 .getPath()));
    }

    public static String readFileToString(final Path path) throws IOException {
        return FileUtils.readFileToString(path.toFile());
    }

    public static boolean isJsonFile(final Path downloadPath) {

        String name = null;

        if (null != downloadPath && downloadPath.toFile()
                                                .exists()) {
            name = downloadPath.toFile()
                               .getName();
        }
        return StringUtils.endsWithIgnoreCase(name, JSON);

    }
}
