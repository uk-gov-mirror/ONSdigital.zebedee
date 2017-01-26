package com.github.onsdigital.zebedee.reader.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.github.onsdigital.zebedee.util.PathUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author James Fawke on 26/01/2017.
 */
public class JsonToStringConverterTest {


    @Test
    public void extractText() throws Exception {
        String s = PathUtils.readFileToString(Paths.get("src/test/resources/countries.json"));
        List<String> countriesList = new JsonToStringConverter(s).extractText();

        assertTrue(countriesList.get(0).contains("376"));
        assertTrue(countriesList.get(0).contains("GBP"));
    }

    @Test(expected = IOException.class)
    public void extractTextNullFile() throws Exception {
        String s = PathUtils.readFileToString(Paths.get("blah.json"));

    }

    @Test(expected = JsonParseException.class)
    public void extractTextCsvFile() throws Exception {
        String s = PathUtils.readFileToString(Paths.get("src/test/resources/testData.csv"));
        new JsonToStringConverter(s).extractText();
        fail("Should not reach this part");

    }
}