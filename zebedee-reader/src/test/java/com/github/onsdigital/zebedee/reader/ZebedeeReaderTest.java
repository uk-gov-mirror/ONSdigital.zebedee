package com.github.onsdigital.zebedee.reader;

import com.github.onsdigital.zebedee.content.base.Content;
import com.github.onsdigital.zebedee.content.base.ContentType;
import com.github.onsdigital.zebedee.content.staticpage.StaticPage;
import com.github.onsdigital.zebedee.content.statistics.document.figure.table.Table;
import com.github.onsdigital.zebedee.exceptions.NotFoundException;
import com.github.onsdigital.zebedee.exceptions.ZebedeeException;
import com.github.onsdigital.zebedee.reader.configuration.TestConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by bren on 31/07/15.
 */

public class ZebedeeReaderTest {

    @Before
    public void initializeTestConfig() {
        TestConfiguration.initializeTestConfiguration();
    }

    @Test
    public void testReadPublishedContent() throws ZebedeeException, IOException {
        Content content = readAccessibilityData("/about/accessibility/data.json///");
    }

    @Test
    public void testReadPublishedContentWithAbsoluteUri() throws ZebedeeException, IOException {
        Content content = readAccessibilityData("about/accessibility/data.json///");
    }

    @Test(expected = NotFoundException.class)
    public void testNonExistingContentRead() throws ZebedeeException, IOException {
        ZebedeeReader.getInstance().getPublishedContent("non/existing/path/");
    }

    @Test
    public void testReadPublishedResource() throws ZebedeeException, IOException {
        try (Resource resource = ZebedeeReader.getInstance().getPublishedResource("/economy/environmentalaccounts/articles/uknaturalcapitallandcoverintheuk/2015-03-17/4f5b14cb.xls")) {
            assertNotNull(resource != null);
            assertEquals("application/vnd.ms-excel", resource.getMimeType());
            assertTrue(resource.isNotEmpty());
        }
    }

    @Test
    public void testReadCollectionContent() throws ZebedeeException, IOException {
        Content content = ZebedeeReader.getInstance().getCollectionContent("testcollection", "employmentandlabourmarket/peopleinwork/workplacedisputesandworkingconditions/articles/labourdisputes/2015-07-16/0c908062.json");
        assertNotNull(content);
        assertEquals(content.getType(), ContentType.table);
        assertTrue(content instanceof Table);
    }

    @Test(expected = NotFoundException.class)
    public void testNonExistingCollectionRead() throws ZebedeeException, IOException {
        Content content = ZebedeeReader.getInstance().getCollectionContent("nonexistingcollection", "/employmentandlabourmarket/peopleinwork/workplacedisputesandworkingconditions/articles/labourdisputes/2015-07-16/0c908062.json");
    }

    @Test
    public void testXlsResource() throws ZebedeeException, IOException {
        try (Resource resource = ZebedeeReader.getInstance().getCollectionResource("testcollection", "employmentandlabourmarket/peopleinwork/workplacedisputesandworkingconditions/datasets/labourdisputesbysectorlabd02/labd02jul2015_tcm77-408195.xls")) {
            assertNotNull(resource != null);
            assertEquals("application/vnd.ms-excel", resource.getMimeType());
            assertTrue(resource.isNotEmpty());
        }
    }

    private Content readAccessibilityData(String path) throws ZebedeeException, IOException {
        Content content = ZebedeeReader.getInstance().getPublishedContent(path);
        assertNotNull(content);
        assertEquals(content.getType(), ContentType.static_page);
        assertEquals("Accessibility", content.getDescription().getTitle());
        assertTrue(content instanceof StaticPage);
        StaticPage staticPage = (StaticPage) content;
        assertNotNull(staticPage.getMarkdown());
        return content;
    }

}
