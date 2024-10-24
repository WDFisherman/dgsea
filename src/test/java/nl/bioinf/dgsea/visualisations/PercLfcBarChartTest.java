package nl.bioinf.dgsea.visualisations;

import nl.bioinf.dgsea.data_processing.*;
import org.jfree.data.time.DateRange;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PercLfcBarChartTest {
    static List<Deg> degs;
    static List<Pathway> pathways;
    static List<PathwayGene> pathwayGenes;
    static final String testResourcesFolder = "src/test/resources/";

    @BeforeAll
    public static void setData() throws Exception {
        File dataFolder = new File(testResourcesFolder);
        File pathwayFile = new File(dataFolder, "hsa_pathways.csv");
        File pathwayGenesFile = new File(dataFolder, "pathways.csv");
        File degsFile = new File(dataFolder, "degs.csv");
        FileParseUtils fileParseUtils = new FileParseUtils();
        degs = fileParseUtils.parseDegsFile(degsFile);
        pathwayGenes = fileParseUtils.parsePathwayGeneFile(pathwayGenesFile);
        pathways = fileParseUtils.parsePathwayFile(pathwayFile);
    }

    private PercLfcBarChart.Builder getBuilderRequiredWorking(String testName) {
        return new PercLfcBarChart.Builder(
                "Promising pathway combinations - test: " + testName,
                "pathways",
                "perc share differential expression",
                degs,
                pathways,
                pathwayGenes,
                new File(testResourcesFolder + "test_lfc_"+testName+".png")
        ).maxNPathways(20);
    }

    @Test
    void saveChart_ideal() {
        String testName = "ideal";
        PercLfcBarChart.Builder chartGeneratorsBuilder = getBuilderRequiredWorking(testName);
        assertFileWasMade(chartGeneratorsBuilder, testName, "png");
    }

    /**
     * Does set chart have the chosen pathwayIds (only)
     */
    @Test
    void saveChart_somePathwaysOfInterest() {
        String testName = "some_pathways";
        PercLfcBarChart.Builder chartGeneratorsBuilder = getBuilderRequiredWorking(testName).pathwayIds(new String[]{"hsa00190", "hsa00430"});
        assertFileWasMade(chartGeneratorsBuilder, testName, "png");

    }

    @Test
    void saveChart_setColorPink() {
        String testName = "color_pink";
        PercLfcBarChart.Builder chartGeneratorsBuilder = getBuilderRequiredWorking(testName).colorManual(new String[]{"pink"});
        assertFileWasMade(chartGeneratorsBuilder, testName, "png");
    }

    /**
     * Does set chart have color default colors, because gloo is not a recognized color?
     */
    @Test
    void saveChart_setInvalidColorGloo() {
        String testName = "invalid_color_gloo";
        PercLfcBarChart.Builder chartGeneratorsBuilder = getBuilderRequiredWorking(testName).colorManual(new String[]{"gloo"});
        assertFileWasMade(chartGeneratorsBuilder, testName, "png");
    }

    /**
     * Does set chart use default colors, because color-manual has no items?
     */
    @Test
    void saveChart_setNoColor() {
        String testName = "no_color";
        PercLfcBarChart.Builder chartGeneratorsBuilder = getBuilderRequiredWorking(testName).colorManual(new String[]{});
        assertFileWasMade(chartGeneratorsBuilder, testName, "png");
    }

    /**
     * Does set chart alternate colors, because color-manual does not have enough items?
     */
    @Test
    void saveChart_setFewColors() {
        String testName = "few_colors";
        PercLfcBarChart.Builder chartGeneratorsBuilder = getBuilderRequiredWorking(testName).colorManual(new String[]{"yellow", "red", "black"});
        assertFileWasMade(chartGeneratorsBuilder, testName, "png");
    }

    /**
     * Does set chart save as a proper jpeg?
     */
    @Test
    void saveChart_jpegOutputFile() {
        String testName = "jpeg_output_file";
        PercLfcBarChart.Builder chartGeneratorsBuilder = new PercLfcBarChart.Builder(
                "Promising pathway combinations",
                "pathways",
                "perc share differential expression",
                PercLfcBarChartTest.degs,
                PercLfcBarChartTest.pathways,
                PercLfcBarChartTest.pathwayGenes,
                new File(testResourcesFolder + "test_lfc_"+testName+".jpg")
        ).imageFormat("jpeg");
        assertFileWasMade(chartGeneratorsBuilder, testName, "jpg");
        try {
            assertTrue(isJPEG(new File(testResourcesFolder + "test_lfc_"+testName+".jpg")));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Does set chart throw error if output-file path contains non-existent folder?
     */
    @Test
    void saveChart_wrongOutputFile() {
        PercLfcBarChart.Builder chartGeneratorsBuilder = new PercLfcBarChart.Builder(
                "Promising pathway combinations",
                "pathways",
                "perc share differential expression",
                PercLfcBarChartTest.degs,
                PercLfcBarChartTest.pathways,
                PercLfcBarChartTest.pathwayGenes,
                new File("lala-folder/output.png")
        );
        PercLfcBarChart percLfcBarChart = new PercLfcBarChart(chartGeneratorsBuilder);
        assertThrows(RuntimeException.class, percLfcBarChart::saveChart);
    }

    /**
     * Is set file an actual jpeg. (checks by content)
     * @param filename file to check
     * @return true, if jpeg by file content, false otherwise
     * @throws Exception if reading file fails
     */
    private static Boolean isJPEG(File filename) throws Exception {
        try (DataInputStream ins = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)))) {
            return ins.readInt() == 0xffd8ffe0;
        }
    }

    /**
     * Checks if file was made with assertTrue and if the file was recently modified.
     * @param chartGeneratorsBuilder builder to make chartGenerator with (all chart properties can be set this way)
     * @param testName name of the test, making the file unique for every test-case
     * @param fileExtensionName extension to use after filename
     */
    private static void assertFileWasMade(PercLfcBarChart.Builder chartGeneratorsBuilder, String testName, String fileExtensionName) {
        PercLfcBarChart percLfcBarChart = new PercLfcBarChart(chartGeneratorsBuilder);
        percLfcBarChart.saveChart();
        assertTrue(new DateRange(
                System.currentTimeMillis() - (100 * 100), System.currentTimeMillis())
                .contains(new File(testResourcesFolder + "test_lfc_" + testName + "." + fileExtensionName).lastModified()));
    }

}