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

/**
 * Unit tests for the EnrichmentBarChart class.
 */
public class EnrichmentBarChartTest {
    private static final String OUTPUT_FILE_PATH = "test_chart.png";
    private static final String[] COLOR_MANUAL = new String[]{"red", "green", "blue"};

    private List<EnrichmentResult> enrichmentResults;
    private List<Pathway> pathways;

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
    }

    @Test
    public void testCreateBarChart() {
        assertDoesNotThrow(() -> {
            EnrichmentBarChart chart = new EnrichmentBarChart(
                    "Enrichment Bar Chart",
                    enrichmentResults,
                    pathways,
                    OUTPUT_FILE_PATH,
                    COLOR_MANUAL,
                    null
            );

            // Check if the chart was created successfully
            File outputFile = new File(OUTPUT_FILE_PATH);
            assertTrue(outputFile.exists(), "Output file should be created.");
        });
    }

    @Test
    public void testCreateDataset() throws IOException {
        EnrichmentBarChart chart = new EnrichmentBarChart(
                "Enrichment Bar Chart",
                enrichmentResults,
                pathways,
                OUTPUT_FILE_PATH,
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
            assertEquals(result.enrichmentScore(), dataset.getValue(pathway.description(), pathway.description()),
                    "Enrichment score does not match for pathway: " + pathway.description());
        }
    }

    @Test
    public void testGetColorFromString() throws IOException {
        EnrichmentBarChart chart = new EnrichmentBarChart(
                "Enrichment Bar Chart",
                enrichmentResults,
                pathways,
                OUTPUT_FILE_PATH,
                null,
                null
        );

        // Test color name to Color mapping
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
                OUTPUT_FILE_PATH,
                null,
                null
        );

        // Test default color retrieval
        assertEquals(Color.RED, chart.getDefaultColor(0), "Index 0 should return Color.RED.");
        assertEquals(Color.BLUE, chart.getDefaultColor(1), "Index 1 should return Color.BLUE.");
        assertEquals(Color.GREEN, chart.getDefaultColor(2), "Index 2 should return Color.GREEN.");
        assertEquals(Color.ORANGE, chart.getDefaultColor(3), "Index 3 should return Color.ORANGE.");
        assertEquals(Color.MAGENTA, chart.getDefaultColor(4), "Index 4 should return Color.MAGENTA.");
    }
}
