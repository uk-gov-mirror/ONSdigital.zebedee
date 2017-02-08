package com.github.onsdigital.zebedee.reader.util;

import com.beust.jcommander.internal.Lists;
import com.github.onsdigital.zebedee.content.page.statistics.dataset.Dataset;
import com.github.onsdigital.zebedee.content.page.statistics.dataset.DownloadSection;
import com.github.onsdigital.zebedee.reader.configuration.ReaderConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by fawks on 08/02/2017.
 */
public class ContentExtractorTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentExtractorTest.class);

    @Before
    public void init() {
        String contentDir = ReaderConfiguration.getConfiguration()
                                               .getContentDir();
        LOGGER.info("init([]) : contentDir='{}'", contentDir);
        ContentExtractorFactory.rootFolder = new File("src/test/resources").getAbsolutePath();
        LOGGER.info("init([]) : rootFolder='{}'", ContentExtractorFactory.rootFolder);
    }

    @Test
    public void getInstance() throws Exception {


        Dataset dataset = new Dataset();
        dataset.setUri(new URI("ContentExtractorTest/dataset"));
        DownloadSection ds = new DownloadSection();
        ds.setFile("ContentExtractorTest.downloadFile.zip");
        dataset.setDownloads(Lists.newArrayList(ds));
        ContentExtractor instance = ContentExtractorFactory.getInstance(dataset,
                                                                        "ContentExtractorTest.downloadFile.zip");
        String fileName = "tobeornottobe.txt";
        StringBuilder bldr = new StringBuilder(fileName).append("\n");
        bldr.append(FileUtils.readFileToString(new File("src/test/resources/ContentExtractorTest/dataset/" + fileName)));
        List<String> actual = instance.extract();
        LOGGER.info("getInstance([]) : {}", actual);

        //Check the contents
        assertEquals(bldr.toString()
                         .trim(),
                     actual.get(0)
                           .trim());

    }

}