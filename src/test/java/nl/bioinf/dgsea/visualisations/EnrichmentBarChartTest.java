package nl.bioinf.dgsea.visualisations;

import nl.bioinf.dgsea.data_processing.EnrichmentResult;
import nl.bioinf.dgsea.data_processing.Pathway;
import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EnrichmentBarChartTest {
    private List<EnrichmentResult> enrichmentResults;
    private List<Pathway> pathways;
    private String outputFilePath;
    private String[] colorManual;

    @BeforeEach
    public void setUp() {
        // Setup mock data for testing
        enrichmentResults = Arrays.asList(
                new EnrichmentResult("pathway1", 2.5, 0.01, 0.001),
                new EnrichmentResult("pathway2", 1.8, 0.03, 0.002),
                new EnrichmentResult("pathway3", 3.0, 0.05, 0.003)
        );

        pathways = Arrays.asList(
                new Pathway("pathway1", "Glycolysis"),
                new Pathway("pathway2", "Citrate Cycle"),
                new Pathway("pathway3", "Fatty Acid Biosynthesis")
        );

        outputFilePath = "test_chart.png";
        colorManual = new String[]{"red", "green", "blue"};
    }

    @Test
    public void testCreateBarChart() {
        assertDoesNotThrow(() -> {
            EnrichmentBarChart chart = new EnrichmentBarChart(
                    "Enrichment Bar Chart",
                    enrichmentResults,
                    pathways,
                    outputFilePath,
                    colorManual,
                    null
            );

            // Check if the chart was created successfully
            File outputFile = new File(outputFilePath);
            assertTrue(outputFile.exists(), "Output file should be created.");
        });
    }


    @Test
    public void testCreateDataset() throws IOException {
        EnrichmentBarChart chart = new EnrichmentBarChart(
                "Enrichment Bar Chart",
                enrichmentResults,
                pathways,
                outputFilePath,
                null,
                null
        );

        DefaultCategoryDataset dataset = chart.createDataset(enrichmentResults, pathways);

        // Check if the dataset has the correct number of entries
        assertEquals(enrichmentResults.size(), dataset.getRowCount(), "Dataset should have correct number of rows.");

        // Validate the values in the dataset
        for (int i = 0; i < enrichmentResults.size(); i++) {
            EnrichmentResult result = enrichmentResults.get(i);
            Pathway pathway = pathways.get(i);
            assertEquals(result.enrichmentScore(), dataset.getValue(pathway.description(), pathway.description()));
        }
    }

    @Test
    public void testGetColorFromString() throws IOException {
        EnrichmentBarChart chart = new EnrichmentBarChart(
                "Enrichment Bar Chart",
                enrichmentResults,
                pathways,
                outputFilePath,
                null,
                null
        );

        Color redColor = chart.getColorFromString("red");
        assertEquals(Color.RED, redColor, "Should return Color.RED for 'red' string.");

        Color hexColor = chart.getColorFromString("#00FF00");
        assertEquals(Color.GREEN, hexColor, "Should return Color.GREEN for '#00FF00' hex code.");

        Color invalidColor = chart.getColorFromString("invalidColorName");
        assertEquals(Color.GRAY, invalidColor, "Should return Color.GRAY for an invalid color name.");
    }

    @Test
    public void testGetDefaultColor() throws IOException {
        EnrichmentBarChart chart = new EnrichmentBarChart(
                "Enrichment Bar Chart",
                enrichmentResults,
                pathways,
                outputFilePath,
                null,
                null
        );

        assertEquals(Color.RED, chart.getDefaultColor(0), "Index 0 should return Color.RED.");
        assertEquals(Color.BLUE, chart.getDefaultColor(1), "Index 1 should return Color.BLUE.");
        assertEquals(Color.GREEN, chart.getDefaultColor(2), "Index 2 should return Color.GREEN.");
        assertEquals(Color.ORANGE, chart.getDefaultColor(3), "Index 3 should return Color.ORANGE.");
        assertEquals(Color.MAGENTA, chart.getDefaultColor(4), "Index 4 should return Color.MAGENTA.");
    }
}
