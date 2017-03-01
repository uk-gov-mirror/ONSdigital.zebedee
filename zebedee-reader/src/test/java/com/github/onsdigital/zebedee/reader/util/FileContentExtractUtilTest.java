package com.github.onsdigital.zebedee.reader.util;

import org.apache.tika.metadata.Metadata;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.github.onsdigital.zebedee.reader.util.FileContentExtractUtil.isCompressed;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by James Fawke on 24/01/2017.
 */
public class FileContentExtractUtilTest {
    @Test
    public void isTabularFileNameTRUECsv() {
        List<Metadata> metadataList = new ArrayList<>();
        Metadata md1 = new Metadata();
        md1.set(Metadata.RESOURCE_NAME_KEY, "blob.csv");
        Metadata md2 = new Metadata();
        md2.set(Metadata.RESOURCE_NAME_KEY, "blob.pdf");

        metadataList.add(md1);
        metadataList.add(md2);

        assertTrue(FileContentExtractUtil.isTabularFileName(metadataList));
    }

    @Test
    public void isTabularFileNameTRUEXls() {
        List<Metadata> metadataList = new ArrayList<>();
        Metadata md1 = new Metadata();
        md1.set(Metadata.RESOURCE_NAME_KEY, "blob.xls");
        Metadata md2 = new Metadata();
        md2.set(Metadata.RESOURCE_NAME_KEY, "blob.pdf");

        metadataList.add(md1);
        metadataList.add(md2);

        assertTrue(FileContentExtractUtil.isTabularFileName(metadataList));
    }

    @Test
    public void isTabularFileNameTRUEXlsx() {
        List<Metadata> metadataList = new ArrayList<>();
        Metadata md1 = new Metadata();
        md1.set(Metadata.RESOURCE_NAME_KEY, "blob.xlsx");
        Metadata md2 = new Metadata();
        md2.set(Metadata.RESOURCE_NAME_KEY, "blob.pdf");
        metadataList.add(md1);
        metadataList.add(md2);

        assertTrue(FileContentExtractUtil.isTabularFileName(metadataList));
    }

    @Test
    public void isTabularFileNameFalse() {
        List<Metadata> metadataList = new ArrayList<>();
        Metadata md1 = new Metadata();
        md1.set(Metadata.RESOURCE_NAME_KEY, "blob.docx");
        Metadata md2 = new Metadata();
        md2.set(Metadata.RESOURCE_NAME_KEY, "blob.pdf");

        metadataList.add(md1);
        metadataList.add(md2);

        assertFalse(FileContentExtractUtil.isTabularFileName(metadataList));
    }

    @Test
    public void isTabularContentTypeFalse() {
        List<Metadata> metadataList = new ArrayList<>();
        Metadata md1 = new Metadata();
        md1.set(Metadata.CONTENT_TYPE, "application/pdf");
        Metadata md2 = new Metadata();
        md2.set(Metadata.CONTENT_TYPE, "application/docx");

        metadataList.add(md1);
        metadataList.add(md2);

        assertFalse(FileContentExtractUtil.isTabularContentType(metadataList));
    }


    @Test
    public void isTabularContentTypeTrueExcel() {
        List<Metadata> metadataList = new ArrayList<>();
        Metadata md1 = new Metadata();
        md1.set(Metadata.CONTENT_TYPE, "application/vnd-excel");
        Metadata md2 = new Metadata();
        md2.set(Metadata.CONTENT_TYPE, "application/msword");

        metadataList.add(md1);
        metadataList.add(md2);

        assertTrue(FileContentExtractUtil.isTabularContentType(metadataList));
    }

    @Test
    public void isTabularContentTypeTrueOpenXMLSheet() {
        List<Metadata> metadataList = new ArrayList<>();
        Metadata md1 = new Metadata();
        md1.set(Metadata.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        metadataList.add(md1);
        assertTrue(FileContentExtractUtil.isTabularContentType(metadataList));

    }

    @Test
    public void extractCSVContentFromFile() {

        Path testDataFile = Paths.get("src/test/resources/testData.csv");
        assertTrue("File not available", testDataFile.toFile()
                                                     .exists());
        List<String> csvContent = FileContentExtractUtil.extractText(testDataFile);

        assertEquals(newArrayList("bamboo_shoots arugula avocado basil beets beans artichoke asparagus"),
                     csvContent);
    }

    @Test
    public void extractTxtContentFromFile() {

        Path testDataFile = Paths.get("src/test/resources/testData.txt");
        assertTrue("File not available", testDataFile.toFile()
                                                     .exists());
        List<String> txtContent = FileContentExtractUtil.extractText(testDataFile);

        //Should be raw as it's a text file
        assertEquals(newArrayList(
                "artichoke,arugula     asparagus artichoke avocado      bamboo_shoots basil    artichoke beans beets\n"),
                     txtContent);
    }

    @Test
    public void extractContentFromNonExistantFile() {

        Path testDataFile = Paths.get("blah!!");
        List<String> csvContent = FileContentExtractUtil.extractText(testDataFile);
        assertNull(csvContent);

    }

    @Test
    public void isPathCompressedTar() {
        Path tarFile = Paths.get("/local/blahs/bloo.tAr");
        assertTrue(isCompressed(tarFile));
    }

    @Test
    public void isPathCompressedZip() {
        Path fle = Paths.get("/local/blahs/bloo.ziP");
        assertTrue(isCompressed(fle));
    }

    @Test
    public void isPathCompressedGz() {
        Path fle = Paths.get("/local/blahs/bloo.gz");
        assertTrue(isCompressed(fle));
    }


    @Test
    public void isPathCompressedXls() {
        Path file = Paths.get("/local/blahs/bloo.xlsx");
        assertFalse(isCompressed(file));
    }

    @Test
    public void isPathCompressedLogx() {
        Path file = Paths.get("/local/blahs/bloo.logx");
        assertFalse(isCompressed(file));
    }


    @Test
    public void isPathCompressedLog() {
        Path fle = Paths.get("/local/blahs/bloo.log");
        assertFalse(isCompressed(fle));
    }


    @Test
    public void isPathCompressedRar() {
        Path fle = Paths.get("/local/blahs/bloo.rar");
        assertTrue(isCompressed(fle));
    }


    @Test
    public void isFilenameCompressedTar() {

        assertTrue(isCompressed("/local/blahs/bloo.tAr"));
    }

    @Test
    public void isFilenameCompressedZip() {

        assertTrue(isCompressed("/local/blahs/bloo.ziP"));
    }

    @Test
    public void isFilenameCompressedGz() {
        assertTrue(isCompressed("/local/blahs/bloo.gz"));
    }


    @Test
    public void isFilenameCompressedXls() {
        assertFalse(isCompressed("/local/blahs/bloo.xlsx"));
    }

    @Test
    public void isFilenameCompressedLogx() {
        assertFalse(isCompressed("/local/blahs/bloo.logx"));
    }


    @Test
    public void isFilenameCompressedLog() {
        assertFalse(isCompressed("/local/blahs/bloo.log"));
    }


    @Test
    public void isFilenameCompressedRar() {
        assertTrue(isCompressed("/local/blahs/bloo.rar"));
    }
}