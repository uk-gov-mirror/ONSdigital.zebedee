package com.github.onsdigital.zebedee.util;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by James Fawke on 26/01/2017.
 */
public class PathUtilsTest {


    @Test
    public void testSizeInMBNonExistant() {
        Path path = Paths.get("blah..");
        assertEquals(0, PathUtils.sizeInMB(path),0);
    }

    @Test
    public void testSizeInMBNullPath() {
        assertEquals(0, PathUtils.sizeInMB(null), 0);
    }
    @Test
    public void testSizeInMB1Mb() {
        Path path = Paths.get("src/test/resources/com/github/onsdigital/zebedee/util/1MB.txt");
        assertEquals(1, PathUtils.sizeInMB(path),0.0001);
    }

    @Test
    public void testSizeInMB1Kb() {
        Path path = Paths.get("src/test/resources/com/github/onsdigital/zebedee/util/1KB.txt");
        assertEquals(1024, PathUtils.sizeInMB(path)*1024*1024,0.0001);
    }

    @Test
    public void testSize() {
        Path path = Paths.get("src/test/resources/com/github/onsdigital/zebedee/util/1KB.txt");
        assertEquals(1024, PathUtils.size(path));
    }

    @Test
    public void readFileToString() throws Exception {
        assertNotNull(PathUtils.readFileToString(Paths.get("src/test/resources/countries.json")));
    }
    @Test
    public void testSize1MB() {
        Path path = Paths.get("src/test/resources/com/github/onsdigital/zebedee/util/1MB.txt");
        assertEquals(1024*1024, PathUtils.size(path));
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
    public void testIsJsonCsvFileTrue() throws Exception {
        assertFalse(PathUtils.isJsonFile(Paths.get("src/test/resources/testData.csv")));

    }

    @Test
    public void testIsJsonCsvFileNull() throws Exception {
        assertFalse(PathUtils.isJsonFile(Paths.get("src/test/resources/blah.csv")));
    }

}