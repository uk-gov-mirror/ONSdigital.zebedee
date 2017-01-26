package com.github.onsdigital.zebedee.util;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by James Fawke on 26/01/2017.
 */
public class PathUtilsTest {
    @Test
    public void toRelativeUri() throws Exception {

    }

    @Test
    public void readFileToString() throws Exception {
        assertNotNull(PathUtils.readFileToString(Paths.get("src/test/resources/countries.json")));
    }

    @Test
    public void testIsJsonTrue() throws Exception {
        assertTrue(PathUtils.isJsonFile(Paths.get("src/test/resources/countries.json")));
    }

    @Test(expected = FileNotFoundException.class)
    public void testReadFileToStringNoFile() throws Exception {
        assertNotNull(PathUtils.readFileToString(Paths.get("src/test/resources/blah.json")));
    }

    @Test
    public void testIsJSONTrue() throws Exception {
        assertTrue(PathUtils.isJsonFile(Paths.get("src/test/resources/countries.JSON")));
    }

    @Test
    public void testIsJsonCsvFileTrue() throws Exception {
        assertFalse(PathUtils.isJsonFile(Paths.get("src/test/resources/testData.csv")));

    }

    @Test
    public void testIsJsonCsvFileNull() throws Exception {
        assertFalse(PathUtils.isJsonFile(Paths.get("src/test/resources/blah.csv")));

    }

}