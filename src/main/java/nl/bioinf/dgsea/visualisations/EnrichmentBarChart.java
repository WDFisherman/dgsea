package nl.bioinf.dgsea.visualisations;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.ChartUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.bioinf.dgsea.data_processing.Pathway;
import nl.bioinf.dgsea.data_processing.EnrichmentResult;

/**
 * Class for creating and saving an enrichment bar chart using JFreeChart.
 */
public class EnrichmentBarChart {
    private String title;
    private List<EnrichmentResult> enrichmentResults;
    private List<Pathway> pathways;
    private String outputFilePath;
    private String[] colorManual; // User-defined colors
    private String colorScheme; // Color scheme if no manual colors are given

    /**
     * Constructor for EnrichmentBarChart.
     *
     * @param title            The title of the chart.
     * @param enrichmentResults The enrichment results to be displayed.
     * @param pathways         The pathways corresponding to the enrichment results.
     * @param outputFilePath   The path where the chart image will be saved.
     * @param colorManual      User-defined colors for the bars.
     * @param colorScheme      Default color scheme if no manual colors are provided.
     * @throws IOException if an error occurs while saving the chart.
     */
    public EnrichmentBarChart(String title, List<EnrichmentResult> enrichmentResults, List<Pathway> pathways,
                              String outputFilePath, String[] colorManual, String colorScheme) throws IOException {
        this.title = title;
        this.enrichmentResults = enrichmentResults;
        this.pathways = pathways;
        this.outputFilePath = outputFilePath;
        this.colorManual = colorManual;
        this.colorScheme = colorScheme;

        DefaultCategoryDataset dataset = createDataset(enrichmentResults, pathways);
        JFreeChart barChart = createChart(dataset);
        applyColors(barChart);

        int width = 800;
        int height = 600;
        File file = new File(outputFilePath);
        ChartUtils.saveChartAsPNG(file, barChart, width, height);
    }

    private JFreeChart createChart(DefaultCategoryDataset dataset) {
        JFreeChart barChart = ChartFactory.createBarChart(
                title,                    // Title of the chart
                "",                       // X-Axis label (empty to remove labels)
                "Enrichment Score",      // Y-Axis label
                dataset
        );

        CategoryPlot plot = barChart.getCategoryPlot();
        CategoryAxis domainAxis = plot.getDomainAxis();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        // Rotate labels 90 degrees if needed (set to NONE if you don't want any labels)
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        domainAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 12)); // Reduce font size or remove

        // Increase the thickness of the bars
        renderer.setMaximumBarWidth(0.4);  // Adjust the value for thicker bars

        return barChart;
    }

    private void applyColors(JFreeChart barChart) {
        CategoryPlot plot = barChart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        // Kleurtoewijzing met validatie
        for (int i = 0; i < enrichmentResults.size(); i++) {
            Color color;
            if (colorManual != null && colorManual.length > 0) {
                color = getColorFromString(colorManual[i % colorManual.length]);
            } else {
                color = getDefaultColor(i);
            }
            renderer.setSeriesPaint(i, color);
        }
    }

    /**
     * Converts a color name or hex code to a Color object.
     *
     * @param colorStr The name or hex code of the color.
     * @return The corresponding Color object, or gray if invalid.
     */
    Color getColorFromString(String colorStr) {
        // A mapping of common color names to their corresponding Color objects
        Map<String, Color> colorNameMap = new HashMap<>();
        colorNameMap.put("red", Color.RED);
        colorNameMap.put("blue", Color.BLUE);
        colorNameMap.put("green", Color.GREEN);
        colorNameMap.put("orange", Color.ORANGE);
        colorNameMap.put("yellow", Color.YELLOW);
        colorNameMap.put("pink", Color.PINK);
        colorNameMap.put("magenta", Color.MAGENTA);
        colorNameMap.put("cyan", Color.CYAN);
        colorNameMap.put("gray", Color.GRAY);
        colorNameMap.put("black", Color.BLACK);
        colorNameMap.put("white", Color.WHITE);

        // First, check if the colorStr is a named color
        if (colorNameMap.containsKey(colorStr.toLowerCase())) {
            return colorNameMap.get(colorStr.toLowerCase());
        }

        // Otherwise, try interpreting it as a hex color code
        try {
            return Color.decode(colorStr);
        } catch (NumberFormatException e) {
            System.err.println("Invalid color format: " + colorStr); // Log the error
            return Color.GRAY; // Fallback color
        }
    }

    /**
     * Provides a default color based on the index.
     *
     * @param index The index of the series.
     * @return The default Color object.
     */
    Color getDefaultColor(int index) {
        // Geef een standaardkleur terug op basis van de index
        switch (index % 5) {
            case 0: return Color.RED;
            case 1: return Color.BLUE;
            case 2: return Color.GREEN;
            case 3: return Color.ORANGE;
            case 4: return Color.MAGENTA;
            default: return Color.BLACK; // Fallback color
        }
    }

    /**
     * Creates a dataset for the bar chart from the enrichment results and pathways.
     *
     * @param enrichmentResults The enrichment results.
     * @param pathways         The pathways corresponding to the enrichment results.
     * @return The dataset for the bar chart.
     */
    DefaultCategoryDataset createDataset(List<EnrichmentResult> enrichmentResults, List<Pathway> pathways) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Set<String> addedSeriesNames = new HashSet<>(); // Set voor unieke series

        for (int i = 0; i < enrichmentResults.size(); i++) {
            EnrichmentResult result = enrichmentResults.get(i);
            Pathway matchingPathway = pathways.stream()
                    .filter(pathway -> pathway.pathwayId().equals(result.pathwayId()))
                    .findFirst()
                    .orElse(null);

            if (matchingPathway != null) {
                String description = matchingPathway.description();

                // Controleer of de beschrijving al is toegevoegd
                if (!addedSeriesNames.contains(description)) {
                    dataset.addValue(result.enrichmentScore(), description, description);  // Gebruik description als serie- en categorie naam
                    addedSeriesNames.add(description); // Voeg beschrijving toe aan de set
                } else {
                    // Optioneel: logica om te reageren op een duplicaat
                    System.out.println("Beschrijving '" + description + "' bestaat al. Overslaan.");
                }
            }
        }

        return dataset;
    }
}
