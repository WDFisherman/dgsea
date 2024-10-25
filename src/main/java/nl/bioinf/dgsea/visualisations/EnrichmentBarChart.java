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
 * This class visualizes enrichment results in a bar chart format and provides options for custom colors.
 */
public class EnrichmentBarChart {
    private final String title;
    private final List<EnrichmentResult> enrichmentResults;
    private final Color[] colorManual; // User-defined colors

    /**
     * Constructor for EnrichmentBarChart.
     *
     * @param title            The title of the chart.
     * @param enrichmentResults The enrichment results to be displayed.
     * @param pathways         The pathways corresponding to the enrichment results.
     * @param outputFilePath   The path where the chart image will be saved.
     * @param colorManual      User-defined colors for the bars.
     * @throws IOException if an error occurs while saving the chart.
     */
    public EnrichmentBarChart(String title, List<EnrichmentResult> enrichmentResults, List<Pathway> pathways,
                              String outputFilePath, Color[] colorManual) throws IOException {
        this.title = title;
        this.enrichmentResults = enrichmentResults;
        this.colorManual = colorManual;

        DefaultCategoryDataset dataset = createDataset(enrichmentResults, pathways);
        JFreeChart barChart = createChart(dataset);
        applyColors(barChart);

        int width = 800;
        int height = 600;
        File file = new File(outputFilePath);
        ChartUtils.saveChartAsPNG(file, barChart, width, height);
    }

    /**
     * Creates the bar chart with the provided dataset.
     *
     * @param dataset Dataset containing enrichment scores.
     * @return Configured JFreeChart instance for the bar chart.
     */
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

    /**
     * Applies user-defined or default colors to the bar chart.
     *
     * @param barChart The chart to which colors will be applied.
     */
    private void applyColors(JFreeChart barChart) {
        CategoryPlot plot = barChart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        Color colorItem;

        // Apply colors to each series based on user input or default settings
        for (int i = 0; i < enrichmentResults.size(); i++) {
            if (colorManual == null || colorManual.length == 0) {
                colorItem = getDefaultColor(i);
            } else {
                colorItem = colorManual[i % colorManual.length];
            }
            renderer.setSeriesPaint(i, colorItem);
        }
    }

    /**
     * Converts a color name or hex code to a Color object.
     *
     * @param colorStr The name or hex code of the color.
     * @return The corresponding Color object, or gray if invalid.
     */
    Color getColorFromString(String colorStr) {
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

        if (colorNameMap.containsKey(colorStr.toLowerCase())) {
            return colorNameMap.get(colorStr.toLowerCase());
        }

        try {
            return Color.decode(colorStr);
        } catch (NumberFormatException e) {
            System.err.println("Invalid color format: " + colorStr);
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
        return switch (index % 5) {
            case 0 -> Color.RED;
            case 1 -> Color.BLUE;
            case 2 -> Color.GREEN;
            case 3 -> Color.ORANGE;
            default -> Color.MAGENTA; // Fallback color
        };
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
        Set<String> addedSeriesNames = new HashSet<>(); // Set to track unique series names

        for (EnrichmentResult result : enrichmentResults) {
            Pathway matchingPathway = pathways.stream()
                    .filter(pathway -> pathway.pathwayId().equals(result.pathwayId()))
                    .findFirst()
                    .orElse(null);

            if (matchingPathway != null) {
                String description = matchingPathway.description();

                // Check if the description has already been added
                if (!addedSeriesNames.contains(description)) {
                    dataset.addValue(result.enrichmentScore(), description, description);  // Use description as series and category name
                    addedSeriesNames.add(description); // Add description to the set
                } else {
                    System.out.println("Description '" + description + "' already exists. Skipping.");
                }
            }
        }

        return dataset;
    }
}
