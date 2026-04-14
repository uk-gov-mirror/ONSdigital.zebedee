package com.github.onsdigital.zebedee.model.publishing.legacycacheapi;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class PayloadPathUpdaterTest {

    @RunWith(Parameterized.class)
    public static class PayloadPathUpdaterParameterisedTestVisualisation {
        private final String inputURL;
        private final String expectedResult;

        public PayloadPathUpdaterParameterisedTestVisualisation(String input, String expected) {
            this.inputURL = input;
            this.expectedResult = expected;
        }

        @Parameterized.Parameters
        public static List<Object[]> checkVisualisations() {
            return Arrays.asList(new Object[][]{
                    // case 1
                    {"/ons/seg1", "/ons/seg1"}, {"/ons/seg1/seg2/", "/ons/seg1/seg2"},
                    // case 2: visualisation => keep 1 after type
                    {"/visualisations/dvc1945/seasonalflu/", "/visualisations/dvc1945"}, {"/visualisations/dvc1945/seasonalflu/index.html", "/visualisations/dvc1945"},
                    // case 2 + trailing slash
                    {"/visualisations/dvc1945/seasonalflu/index.html/", "/visualisations/dvc1945"},});
        }

        @Test
        public void testVisualisation() {
            String result = PayloadPathUpdater.getCanonicalPagePath(inputURL, "cake-1abcdef345");
            assertEquals(expectedResult, result);
        }
    }

    @RunWith(Parameterized.class)
    public static class PayloadPathUpdaterParameterisedTestResources {
        private final String inputURL;
        private final String expectedResult;

        public PayloadPathUpdaterParameterisedTestResources(String input, String expected) {
            this.inputURL = input;
            this.expectedResult = expected;
        }

        @Parameterized.Parameters
        public static List<Object[]> checkResourcesPath() {
            // case 3: resource types => extract uri
            return Arrays.asList(new Object[][]{{"/file?uri=/economy/inflationandpriceindices/adhocs/009581cpiinflationbetween2010and2018/cpiinflationbetween2010and2018.xls", "/economy/inflationandpriceindices/adhocs/009581cpiinflationbetween2010and2018"}, {"/generator?uri=/economy/inflationandpriceindices/bulletins/producerpriceinflation/october2022/30d7d6c2&format=csv", "/economy/inflationandpriceindices/bulletins/producerpriceinflation/october2022"}, {"/chartimage?uri=/economy/inflationandpriceindices/bulletins/producerpriceinflation/october2022/previous/v1/30d7d6c2", "/economy/inflationandpriceindices/bulletins/producerpriceinflation/october2022"}, {"/chartconfig?format=csv&uri=/economy/inflationandpriceindices/timeseries/l55o/mm23", "/economy/inflationandpriceindices/timeseries/l55o/mm23"}, {"/chart?format=csv&uri=/economy/inflationandpriceindices/timeseries/l55o/mm23", "/economy/inflationandpriceindices/timeseries/l55o/mm23"}, {"/embed?format=csv&uri=/economy/inflationandpriceindices/timeseries/l55o/mm23", "/economy/inflationandpriceindices/timeseries/l55o/mm23"}, {"/export?uri=/economy/grossdomesticproductgdp/timeseries/abmi/pn2/data&format=csv", "/economy/grossdomesticproductgdp/timeseries/abmi/pn2"}, {"/resource?format=csv&uri=/economy/inflationandpriceindices/timeseries/l55o/mm23", "/economy/inflationandpriceindices/timeseries/l55o/mm23"}, {"/export?format=csv&uri=/economy/inflationandpriceindices/timeseries/l55o/mm23", "/economy/inflationandpriceindices/timeseries/l55o/mm23"}, {"/file?uri=/economy/inflationandpriceindices/adhocs/009581cpiinflationbetween2010and2018/cpiinflationbetween2010and2018.xls/", "/economy/inflationandpriceindices/adhocs/009581cpiinflationbetween2010and2018"}, {"/generator?uri=/economy/inflationandpriceindices/bulletins/producerpriceinflation/october2022/30d7d6c2/&format=csv", "/economy/inflationandpriceindices/bulletins/producerpriceinflation/october2022"}, {"/chartimage?uri=/economy/inflationandpriceindices/bulletins/producerpriceinflation/october2022/previous/v1/30d7d6c2/", "/economy/inflationandpriceindices/bulletins/producerpriceinflation/october2022"}, {"/chartconfig?format=csv&uri=/economy/inflationandpriceindices/timeseries/l55o/mm23/", "/economy/inflationandpriceindices/timeseries/l55o/mm23"}, {"/chart?format=csv&uri=/economy/inflationandpriceindices/timeseries/l55o/mm23/", "/economy/inflationandpriceindices/timeseries/l55o/mm23"}, {"/embed?format=csv&uri=/economy/inflationandpriceindices/timeseries/l55o/mm23/", "/economy/inflationandpriceindices/timeseries/l55o/mm23"}, {"/resource?format=csv&uri=/economy/inflationandpriceindices/timeseries/l55o/mm23/", "/economy/inflationandpriceindices/timeseries/l55o/mm23"}, {"/export?format=csv&uri=/economy/inflationandpriceindices/timeseries/l55o/mm23/", "/economy/inflationandpriceindices/timeseries/l55o/mm23"}, {"/export?uri=/economy/grossdomesticproductgdp/timeseries/abmi/pn2/data/&format=csv", "/economy/grossdomesticproductgdp/timeseries/abmi/pn2"}});
        }

        @Test
        public void testResources() {
            String result = PayloadPathUpdater.getCanonicalPagePath(inputURL, "cake-1abcdef345");
            assertEquals(expectedResult, result);
        }
    }

    @RunWith(Parameterized.class)
    public static class PayloadPathUpdaterParameterisedTestBulletin {
        private final String inputURL;
        private final String expectedResult;

        public PayloadPathUpdaterParameterisedTestBulletin(String input, String expected) {
            this.inputURL = input;
            this.expectedResult = expected;
        }

        @Parameterized.Parameters
        public static List<Object[]> checkBulletinsPath() {
            // case 3: resource types => extract uri
            return Arrays.asList(new Object[][]{
                    // bulletins & articles => keep 2 after type
                    {"/economy/inflationandpriceindices/bulletins/producerpriceinflation/latest", "/economy/inflationandpriceindices/bulletins/producerpriceinflation/latest"}, {"/economy/inflationandpriceindices/articles/researchanddevelopmentsinthetransformationofukconsumerpricestatistics/december2023", "/economy/inflationandpriceindices/articles/researchanddevelopmentsinthetransformationofukconsumerpricestatistics/december2023"},
                    // trailing slash
                    {"/economy/inflationandpriceindices/bulletins/producerpriceinflation/latest/", "/economy/inflationandpriceindices/bulletins/producerpriceinflation/latest"}, {"/economy/inflationandpriceindices/articles/researchanddevelopmentsinthetransformationofukconsumerpricestatistics/december2023/", "/economy/inflationandpriceindices/articles/researchanddevelopmentsinthetransformationofukconsumerpricestatistics/december2023"},});
        }

        @Test
        public void testBulletinPath() {
            String result = PayloadPathUpdater.getCanonicalPagePath(inputURL, "cake-1abcdef345");
            assertEquals(expectedResult, result);
        }
    }

    @RunWith(Parameterized.class)
    public static class PayloadPathUpdaterParameterisedTestQMisAndAdhocAndMethods {
        private final String inputURL;
        private final String expectedResult;

        public PayloadPathUpdaterParameterisedTestQMisAndAdhocAndMethods(String input, String expected) {
            this.inputURL = input;
            this.expectedResult = expected;
        }

        @Parameterized.Parameters
        public static List<Object[]> checkQMisAndAdhocAndMethodsPath() {
            return Arrays.asList(new Object[][]{
                    // qmis & adhoc => keep 1 after type
                    {"/economy/grossdomesticproductgdp/qmis/annualacquisitionsanddisposalsofcapitalassetssurveyqmi", "/economy/grossdomesticproductgdp/qmis/annualacquisitionsanddisposalsofcapitalassetssurveyqmi"},
                    {"/economy/inflationandpriceindices/adhocs/009581cpiinflationbetween2010and2018", "/economy/inflationandpriceindices/adhocs/009581cpiinflationbetween2010and2018"},
                    // trailing slash
                    {"/economy/grossdomesticproductgdp/qmis/annualacquisitionsanddisposalsofcapitalassetssurveyqmi/", "/economy/grossdomesticproductgdp/qmis/annualacquisitionsanddisposalsofcapitalassetssurveyqmi"},
                    {"/economy/inflationandpriceindices/adhocs/009581cpiinflationbetween2010and2018/", "/economy/inflationandpriceindices/adhocs/009581cpiinflationbetween2010and2018"}, {"/economy/inflationandpriceindices/methodologies/consumerpriceinflationincludesall3indicescpihcpiandrpiqmi/anything", "/economy/inflationandpriceindices/methodologies/consumerpriceinflationincludesall3indicescpihcpiandrpiqmi"},});
        }

        @Test
        public void testQMISAndAdhocPath() {
            String result = PayloadPathUpdater.getCanonicalPagePath(inputURL, "cake-1abcdef345");
            assertEquals(expectedResult, result);
        }
    }

    @RunWith(Parameterized.class)
    public static class PayloadPathUpdaterParameterisedTestData {
        private final String inputURL;
        private final String expectedResult;

        public PayloadPathUpdaterParameterisedTestData(String input, String expected) {
            this.inputURL = input;
            this.expectedResult = expected;
        }

        @Parameterized.Parameters
        public static List<Object[]> checkDataPath() {
            // resource types => extract uri
            return Arrays.asList(new Object[][]{
                    // remove /data
                    {"/economy/grossdomesticproductgdp/timeseries/abmi/pn2/data", "/economy/grossdomesticproductgdp/timeseries/abmi/pn2"},
                    {"/economy/grossdomesticproductgdp/something/data", "/economy/grossdomesticproductgdp/something"},
                    {"/economy/inflationandpriceindices/datasets/consumerpriceindicescpiandretailpricesindexrpiitemindicesandpricequotes/data", "/economy/inflationandpriceindices/datasets/consumerpriceindicescpiandretailpricesindexrpiitemindicesandpricequotes"},
                    {"/economy/inflationandpriceindices/bulletins/producerpriceinflations/latest/data", "/economy/inflationandpriceindices/bulletins/producerpriceinflations/latest"},
                    {"/economy/inflationandpriceindices/methodologies/consumerpriceinflationincludesall3indicescpihcpiandrpiqmi/data", "/economy/inflationandpriceindices/methodologies/consumerpriceinflationincludesall3indicescpihcpiandrpiqmi"},
                    // remove data + trailing slash
                    {"/economy/inflationandpriceindices/datasets/consumerpriceindicescpiandretailpricesindexrpiitemindicesandpricequotes/pricequotesseptember2023/upload-pricequotes202309.csv/", "/economy/inflationandpriceindices/datasets/consumerpriceindicescpiandretailpricesindexrpiitemindicesandpricequotes/pricequotesseptember2023"},
                    {"/economy/grossdomesticproductgdp/timeseries/abmi/pn2/data/", "/economy/grossdomesticproductgdp/timeseries/abmi/pn2"},
                    {"/economy/grossdomesticproductgdp/something/data/", "/economy/grossdomesticproductgdp/something"},
                    {"/economy/inflationandpriceindices/datasets/consumerpriceindicescpiandretailpricesindexrpiitemindicesandpricequotes/data/", "/economy/inflationandpriceindices/datasets/consumerpriceindicescpiandretailpricesindexrpiitemindicesandpricequotes"},
                    {"/economy/inflationandpriceindices/bulletins/producerpriceinflations/latest/data/", "/economy/inflationandpriceindices/bulletins/producerpriceinflations/latest"},
                    {"/economy/inflationandpriceindices/datasets/consumerpriceindicescpiandretailpricesindexrpiitemindicesandpricequotes/pricequotesseptember2023/upload-pricequotes202309.csv/", "/economy/inflationandpriceindices/datasets/consumerpriceindicescpiandretailpricesindexrpiitemindicesandpricequotes/pricequotesseptember2023"},
                    {"/economy/inflationandpriceindices/methodologies/consumerpriceinflationincludesall3indicescpihcpiandrpiqmi/anything", "/economy/inflationandpriceindices/methodologies/consumerpriceinflationincludesall3indicescpihcpiandrpiqmi"}});
        }

        @Test
        public void testDataPath() {
            String result = PayloadPathUpdater.getCanonicalPagePath(inputURL, "cake-1abcdef345");
            assertEquals(expectedResult, result);
        }
    }

    @RunWith(Parameterized.class)
    public static class PayloadPathUpdaterParameterisedTestTimeseries {
        private final String inputURL;
        private final String expectedResult;

        public PayloadPathUpdaterParameterisedTestTimeseries(String input, String expected) {
            this.inputURL = input;
            this.expectedResult = expected;
        }

        @Parameterized.Parameters
        public static List<Object[]> checkTimeSeriesPath() {
            return Arrays.asList(new Object[][]{
                    // timeseries => keep up to 2 after type
                    {"/economy/grossdomesticproductgdp/timeseries/ihyq", "/economy/grossdomesticproductgdp/timeseries/ihyq"},
                    {"/economy/grossdomesticproductgdp/timeseries/abmi/pn2", "/economy/grossdomesticproductgdp/timeseries/abmi/pn2"},
                    // timeseries => remove linechartconfig
                    {"/economy/grossdomesticproductgdp/timeseries/abmi/pn2/linechartconfig", "/economy/grossdomesticproductgdp/timeseries/abmi/pn2"},
                    {"/economy/grossdomesticproductgdp/timeseries/linechartconfig", "/economy/grossdomesticproductgdp/timeseries"},
                    // timeseries + trailing slash
                    {"/economy/grossdomesticproductgdp/timeseries/ihyq/", "/economy/grossdomesticproductgdp/timeseries/ihyq"},
                    {"/economy/grossdomesticproductgdp/timeseries/abmi/pn2/", "/economy/grossdomesticproductgdp/timeseries/abmi/pn2"},
                    {"/economy/grossdomesticproductgdp/timeseries/abmi/pn2/linechartconfig/", "/economy/grossdomesticproductgdp/timeseries/abmi/pn2"},
                    {"/economy/grossdomesticproductgdp/timeseries/linechartconfig/", "/economy/grossdomesticproductgdp/timeseries"}});
        }

        @Test
        public void testTimeseries() {
            String result = PayloadPathUpdater.getCanonicalPagePath(inputURL, "cake-1abcdef345");
            assertEquals(expectedResult, result);
        }
    }

    @RunWith(Parameterized.class)
    public static class PayloadPathUpdaterParameterisedTestDataset {
        private final String inputURL;
        private final String expectedResult;

        public PayloadPathUpdaterParameterisedTestDataset(String input, String expected) {
            this.inputURL = input;
            this.expectedResult = expected;
        }

        @Parameterized.Parameters
        public static List<Object[]> checkDatasetPath() {
            // case 3: resource types => extract uri
            return Arrays.asList(new Object[][]{
                    // datasets => keep up to 2 after type
                    {"/economy/inflationandpriceindices/datasets/consumerpriceindicescpiandretailpricesindexrpiitemindicesandpricequotes", "/economy/inflationandpriceindices/datasets/consumerpriceindicescpiandretailpricesindexrpiitemindicesandpricequotes"},
                    {"/economy/inflationandpriceindices/datasets/consumerpriceindicescpiandretailpricesindexrpiitemindicesandpricequotes/pricequotesseptember2023", "/economy/inflationandpriceindices/datasets/consumerpriceindicescpiandretailpricesindexrpiitemindicesandpricequotes/pricequotesseptember2023"},
                    // trailing slash
                    {"/economy/inflationandpriceindices/datasets/consumerpriceindicescpiandretailpricesindexrpiitemindicesandpricequotes/", "/economy/inflationandpriceindices/datasets/consumerpriceindicescpiandretailpricesindexrpiitemindicesandpricequotes"},
                    {"/economy/inflationandpriceindices/datasets/consumerpriceindicescpiandretailpricesindexrpiitemindicesandpricequotes/pricequotesseptember2023/", "/economy/inflationandpriceindices/datasets/consumerpriceindicescpiandretailpricesindexrpiitemindicesandpricequotes/pricequotesseptember2023"},
                    // Topic pages
                    {"/economy", "/economy"},
                    {"/economy/inflationandpriceindices", "/economy/inflationandpriceindices"},
                    {"/economy/inflationandpriceindices/", "/economy/inflationandpriceindices"},
                    {"/employmentandlabourmarket/peopleinwork/earningsandworkinghours", "/employmentandlabourmarket/peopleinwork/earningsandworkinghours"},
                    {"/employmentandlabourmarket/peopleinwork/earningsandworkinghours/", "/employmentandlabourmarket/peopleinwork/earningsandworkinghours"},
                    // Static pages
                    {"/aboutus/transparencyandgovernance/dataprotection", "/aboutus/transparencyandgovernance/dataprotection"},
                    {"/aboutus/transparencyandgovernance/dataprotection/", "/aboutus/transparencyandgovernance/dataprotection"}, 
                    {"/file?uri=/aboutus/whatwedo/programmesandprojects/sustainabledevelopmentgoals/c3ac8759.png", "/aboutus/whatwedo/programmesandprojects/sustainabledevelopmentgoals"}
                }
            );
        }

        @Test
        public void testDataset() {
            String result = PayloadPathUpdater.getCanonicalPagePath(inputURL, "cake-1abcdef345");
            assertEquals(expectedResult, result);
        }
    }

    @RunWith(Parameterized.class)
    public static class PayloadPathUpdaterParameterisedTestEncode {
        private final String inputURL;
        private final String expectedResult;

        public PayloadPathUpdaterParameterisedTestEncode(String input, String expected) {
            this.inputURL = input;
            this.expectedResult = expected;
        }

        @Parameterized.Parameters
        public static List<Object[]> checkEncodedPath() {
            // case 3: resource types => extract uri
            return Arrays.asList(new Object[][]{
                    // Encoded endpoints
                    {"/file?uri=%2Feconomy%2Fseg1", "/economy/seg1"},
                    {"/file?uri=%2Feconomy%2Fseg1%2Fseg2", "/economy/seg1/seg2"}
                }
            );
        }

        @Test
        public void testEncoded() {
            String result = PayloadPathUpdater.getCanonicalPagePath(inputURL, "cake-1abcdef345");
            assertEquals(expectedResult, result);
        }
    }

    public static class PayloadPathUpdaterLatestTest {
        @Test
        public void testGetPathForBulletinLatest() {
            String result = PayloadPathUpdater.getPathForLatest("/economy/inflationandpriceindices/bulletins/producerpriceinflation/october2022/30d7d6c2/");
            assertEquals("/economy/inflationandpriceindices/bulletins/producerpriceinflation/latest", result);
        }

        @Test
        public void testIsPathBulletinLatest() {
            boolean result = PayloadPathUpdater.isPayloadPathBulletinLatest("/economy/inflationandpriceindices/bulletins/producerpriceinflation/october2022/30d7d6c2/");
            assertTrue(result);
        }

        @Test
        public void testIsPathBulletinLatestFalseForArticlesPath() {
            boolean result = PayloadPathUpdater.isPayloadPathBulletinLatest("/economy/inflationandpriceindices/articles/producerpriceinflation/october2022/30d7d6c2/");
            assertFalse(result);
        }

        @Test
        public void testIsPathBulletinLatestFalse() {
            boolean result = PayloadPathUpdater.isPayloadPathBulletinLatest("/economy/inflationandpriceindices/bulletins/producerpriceinflation/latest");
            assertFalse(result);
        }
        
        @Test
        public void testGetPathForArticleLatest() {
            String result = PayloadPathUpdater.getPathForLatest("/economy/inflationandpriceindices/articles/consumerpricesdevelopmentplan/updatedaugust2024/");
            assertEquals("/economy/inflationandpriceindices/articles/consumerpricesdevelopmentplan/latest", result);
        }

        @Test
        public void testIsPathArticleLatest() {
            boolean result = PayloadPathUpdater.isPayloadPathArticleLatest("/economy/inflationandpriceindices/articles/consumerpricesdevelopmentplan/updatedaugust2024/");
            assertTrue(result);
        }

        @Test
        public void testIsPathArticleLatestFalseForBulletinsPath() {
            boolean result = PayloadPathUpdater.isPayloadPathArticleLatest("/economy/inflationandpriceindices/bulletins/producerpriceinflation/october2022/30d7d6c2/");
            assertFalse(result);
        }

        @Test
        public void testIsPathArticleLatestFalse() {
            boolean result = PayloadPathUpdater.isPayloadPathArticleLatest("/economy/inflationandpriceindices/articles/consumerpricesdevelopmentplan/latest");
            assertFalse(result);
        }
        
        @Test
        public void testGetPathForCompendiaLatest() {
            String result = PayloadPathUpdater.getPathForLatest("/economy/grossdomesticproductgdp/compendium/unitedkingdomnationalaccountsthebluebook/2024/");
            assertEquals("/economy/grossdomesticproductgdp/compendium/unitedkingdomnationalaccountsthebluebook/latest", result);
        }

        @Test
        public void testIsPathCompendiaLatest() {
            boolean result = PayloadPathUpdater.isPayloadPathCompendiaLatest("/economy/grossdomesticproductgdp/compendium/unitedkingdomnationalaccountsthebluebook/2024/");
            assertTrue(result);
        }

        @Test
        public void testIsPathCompendiaLatestFalseForBulletinsPath() {
            boolean result = PayloadPathUpdater.isPayloadPathCompendiaLatest("/economy/inflationandpriceindices/bulletins/producerpriceinflation/october2022/30d7d6c2/");
            assertFalse(result);
        }

        @Test
        public void testIsPathCompendiaLatestFalse() {
            boolean result = PayloadPathUpdater.isPayloadPathCompendiaLatest("/economy/grossdomesticproductgdp/compendium/unitedkingdomnationalaccountsthebluebook/latest");
            assertFalse(result);
        }
    }

    public static class PayloadPathUpdaterFileTest {
        @Test
        public void testGetPathForFile() {
            String result = PayloadPathUpdater.getPathForFile("/economy/inflationandpriceindices/adhocs/009581cpiinflationbetween2010and2018/cpiinflationbetween2010and2018.xls");
            assertEquals("/file?uri=/economy/inflationandpriceindices/adhocs/009581cpiinflationbetween2010and2018/cpiinflationbetween2010and2018.xls", result);
        }
    }
}
