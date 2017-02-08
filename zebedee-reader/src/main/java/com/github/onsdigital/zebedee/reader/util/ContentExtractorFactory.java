package com.github.onsdigital.zebedee.reader.util;

import com.github.onsdigital.zebedee.content.page.base.Page;
import com.github.onsdigital.zebedee.reader.configuration.ReaderConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by fawks on 03/02/2017.
 */

public class ContentExtractorFactory {
    static String rootFolder = ReaderConfiguration.getConfiguration()
                                                                .getContentDir();
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentExtractorFactory.class);

    static {
        LOGGER.info("static initializer() : Root Dir {} ", rootFolder);
    }

    public static ContentExtractor getInstance(final Page page, final String filePath) {
        return new ContentExtractor(rootFolder, page, filePath);
    }


}
