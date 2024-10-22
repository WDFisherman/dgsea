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
import java.util.List;
import nl.bioinf.dgsea.data_processing.Pathway;
import nl.bioinf.dgsea.data_processing.EnrichmentResult;

public class EnrichmentBarChart {
    private String title;
    private List<EnrichmentResult> enrichmentResults;
    private List<Pathway> pathways;
    private String outputFilePath;
    private String[] colorManual; // User-defined colors
    private String colorScheme; // Color scheme if no manual colors are given

    public EnrichmentBarChart(String title, List<EnrichmentResult> enrichmentResults, List<Pathway> pathways, String outputFilePath, String[] colorManual, String colorScheme) throws IOException {
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
        renderer.setMaximumBarWidth(0.1);  // Adjust the value for thicker bars

        return barChart;
    }

    private void applyColors(JFreeChart barChart) {
        CategoryPlot plot = barChart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        // Apply user-defined colors
        if (colorManual != null && colorManual.length > 0) {
            for (int i = 0; i < enrichmentResults.size(); i++) {
                String color = colorManual[i % colorManual.length];
                renderer.setSeriesPaint(i, Color.decode(color)); // Decode hex color
            }
        } else {
            // Apply a default color scheme if no manual colors are provided
            for (int i = 0; i < enrichmentResults.size(); i++) {
                // In deze sectie kun je een standaard kleurenschema toepassen op basis van de `colorScheme` parameter
                // Dit kan verder worden uitgebreid om verschillende kleurenschema's te ondersteunen
                renderer.setSeriesPaint(i, getDefaultColor(i)); // Gebruik een standaardkleur
            }
        }
    }

    private Color getDefaultColor(int index) {
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

    private DefaultCategoryDataset createDataset(List<EnrichmentResult> enrichmentResults, List<Pathway> pathways) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (EnrichmentResult result : enrichmentResults) {
            Pathway matchingPathway = pathways.stream()
                    .filter(pathway -> pathway.pathwayId().equals(result.pathwayId()))
                    .findFirst()
                    .orElse(null);

            if (matchingPathway != null) {
                String description = matchingPathway.description();
                dataset.addValue(result.enrichmentScore(), "", matchingPathway.description());  // "" for no label on X-axis
            }
        }

        return dataset;
    }
}
