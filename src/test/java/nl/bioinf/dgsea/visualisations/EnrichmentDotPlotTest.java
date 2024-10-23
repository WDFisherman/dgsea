package nl.bioinf.dgsea.visualisations;

import nl.bioinf.dgsea.data_processing.EnrichmentResult;
import nl.bioinf.dgsea.data_processing.Pathway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EnrichmentDotPlotTest {
    private List<EnrichmentResult> enrichmentResults;
    private List<Pathway> pathways;
    private String outputFilePath;
    private String[] colorManual;
    private double dotSize;
    private float dotTransparency;

    @BeforeEach
    public void setUp() {
        // Setup mock data for testing
        enrichmentResults = Arrays.asList(
                new EnrichmentResult("pathway1", 2.5, 0.01, 0.001),
                new EnrichmentResult("pathway2", 1.8, 0.03, 0.002),
                new EnrichmentResult("pathway3", 3.0, 0.05, 0.003),
                new EnrichmentResult("pathway4", 1.2, Double.NaN, 0.004) // Include NaN case
        );

        pathways = Arrays.asList(
                new Pathway("pathway1", "Glycolysis"),
                new Pathway("pathway2", "Citrate Cycle"),
                new Pathway("pathway3", "Fatty Acid Biosynthesis"),
                new Pathway("pathway4", "Pathway with NaN") // To test the NaN case
        );

        outputFilePath = "test_dot_plot.png";
        colorManual = new String[]{"red", "green", "blue"};
        dotSize = 10.0;
        dotTransparency = 0.5f;
    }

    @Test
    public void testCreateDotPlot() {
        assertDoesNotThrow(() -> {
            EnrichmentDotPlot dotPlot = new EnrichmentDotPlot(
                    "Enrichment Dot Plot",
                    enrichmentResults,
                    pathways,
                    outputFilePath,
                    colorManual,
                    null,
                    dotSize,
                    dotTransparency
            );

            // Check if the dot plot was created successfully
            File outputFile = new File(outputFilePath);
            assertTrue(outputFile.exists(), "Output file should be created.");
        });
    }

    @Test
    public void testCreateDataset() throws IOException {
        EnrichmentDotPlot dotPlot = new EnrichmentDotPlot(
                "Enrichment Dot Plot",
                enrichmentResults,
                pathways,
                outputFilePath,
                null,
                null,
                dotSize,
                dotTransparency
        );

        XYSeriesCollection dataset = dotPlot.createDataset(enrichmentResults, pathways);

        // Check that the dataset has the correct number of series
        assertEquals(4, dataset.getSeriesCount(), "Dataset should contain four series.");

        // Validate the values in the dataset
        for (int i = 0; i < enrichmentResults.size(); i++) {
            if (!Double.isNaN(enrichmentResults.get(i).adjustedPValue()) && enrichmentResults.get(i).adjustedPValue() < 0.05) {
                assertEquals(enrichmentResults.get(i).adjustedPValue(), dataset.getSeries(pathways.get(i).description()).getX(0));
                assertEquals(enrichmentResults.get(i).enrichmentScore(), dataset.getSeries(pathways.get(i).description()).getY(0));
            }
        }
    }


    @Test
    public void testGetColorFromString() throws IOException {
        EnrichmentDotPlot dotPlot = new EnrichmentDotPlot(
                "Enrichment Dot Plot",
                enrichmentResults,
                pathways,
                outputFilePath,
                null,
                null,
                dotSize,
                dotTransparency
        );

        Color redColor = dotPlot.getColorFromString("red");
        assertEquals(Color.RED, redColor, "Should return Color.RED for 'red' string.");

        Color hexColor = dotPlot.getColorFromString("#00FF00");
        assertEquals(Color.GREEN, hexColor, "Should return Color.GREEN for '#00FF00' hex code.");

        Color invalidColor = dotPlot.getColorFromString("invalidColorName");
        assertEquals(Color.GRAY, invalidColor, "Should return Color.GRAY for an invalid color name.");
    }

    @Test
    public void testGetDefaultColor() throws IOException {
        EnrichmentDotPlot dotPlot = new EnrichmentDotPlot(
                "Enrichment Dot Plot",
                enrichmentResults,
                pathways,
                outputFilePath,
                null,
                null,
                dotSize,
                dotTransparency
        );

        assertEquals(Color.RED, dotPlot.getDefaultColor(0), "Index 0 should return Color.RED.");
        assertEquals(Color.BLUE, dotPlot.getDefaultColor(1), "Index 1 should return Color.BLUE.");
        assertEquals(Color.GREEN, dotPlot.getDefaultColor(2), "Index 2 should return Color.GREEN.");
        assertEquals(Color.ORANGE, dotPlot.getDefaultColor(3), "Index 3 should return Color.ORANGE.");
        assertEquals(Color.MAGENTA, dotPlot.getDefaultColor(4), "Index 4 should return Color.MAGENTA.");
    }
}
