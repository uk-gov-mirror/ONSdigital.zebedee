package com.github.onsdigital.zebedee.reader.util;

import com.github.onsdigital.zebedee.content.page.base.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.github.onsdigital.zebedee.util.PathUtils.concatenate;
import static com.github.onsdigital.zebedee.util.PathUtils.substituteFileName;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.notExists;

public class ContentExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentExtractor.class);

    private final String rootPath;
    private final Page page;
    private final String filePath;

    public ContentExtractor(final String rootPath, final Page page, final String filePath) {

        this.rootPath = rootPath;
        this.page = page;
        this.filePath = filePath;
    }

    public List<String> extract() {
        List<String> content = null;
        try {

            content = extractText(resolveDownloadPath(page, filePath));

        }
        catch (IOException e) {
            LOGGER.error("extractContent([pageURI, filePath]) : failed to parser file '{}' with error {} for page {}",
                         filePath,
                         e.getMessage(),
                         page);
        }
        return content;
    }

    /**
     * Use tika to extract File contents
     *
     * @param downloadPath
     * @return
     */

    private List<String> extractText(final Path downloadPath) {
        LOGGER.debug("extractText([downloadPath]) : extracting {}", downloadPath);
        List<String> strings = FileContentExtractUtil.extractText(downloadPath);
        LOGGER.debug("extractText([downloadPath]) : extracted {}", downloadPath);
        return strings;
    }

    /**
     * Work out the path of the download document
     * <p>
     * <i>Sometimes the document contains the actual location and sometimes it just contains the name</i>
     *
     * @param page
     * @param filePath
     * @return
     * @throws BadRequestException
     */
    private Path resolveDownloadPath(final Page page,
                                     final String filePath) throws IOException {


        Path downloadPath = null;

        Path pagePath = Paths.get(concatenate(rootPath, page.getUri()
                                                            .getRawPath()));
        if (exists(pagePath)) {
            //Get the just the filename as the the file should be in the same directory as the dataJson
            String concat = substituteFileName(pagePath, filePath);

            downloadPath = Paths.get(concat);

        }
        //Check that the path supplied is valid, some of section paths include the path and some do not.
        //AND its not the root data.json file :)
        if (notExists(downloadPath) && "data.json".equals(filePath)) {

            String concat = concatenate(rootPath, filePath);
            downloadPath = Paths.get(concat);


            if (notExists(downloadPath)) {
                LOGGER.info("resolveDownloadPath([Resource, String]) : not a accessible {}", downloadPath);
                downloadPath = null;

            }
        }

        return downloadPath;
    }

}