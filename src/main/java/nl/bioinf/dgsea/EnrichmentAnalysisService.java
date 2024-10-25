package nl.bioinf.dgsea;

import nl.bioinf.dgsea.data_processing.*;
import nl.bioinf.dgsea.table_outputs.EnrichmentTable;
import nl.bioinf.dgsea.visualisations.EnrichmentBarChart;
import nl.bioinf.dgsea.visualisations.EnrichmentDotPlot;

import java.awt.*;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * EnrichmentAnalysisService is responsible for performing enrichment analysis
 * and generating the appropriate charts (bar-chart or dot-chart).
 * This service handles the calculation, filtering, and visualization of
 * pathway enrichment results based on gene expression data.
 */
public class EnrichmentAnalysisService {

    /**
     * Generates an enrichment chart (bar-chart or dot-chart) based on the provided data.
     *
     * @param degs            List of differentially expressed genes (DEGs).
     * @param pathways        List of pathways.
     * @param pathwayGenes    List of PathwayGene mappings.
     * @param maxNPathways    Maximum number of pathways to display.
     * @param outputFilePath  File path for the output image. Default based on chart type if null.
     * @param title           Title of the plot
     * @param colorManual     Array of colors for manual chart customization.
     * @param chartType       The type of chart to generate (BAR_CHART or DOT_CHART).
     * @param dotSize         Size of the dots (optional, relevant for dot-chart).
     * @param dotTransparency Transparency of the dots (optional, relevant for dot-chart).
     * @throws IOException If an error occurs during file writing.
     */
    public void generateEnrichmentChart(
            List<Deg> degs,
            List<Pathway> pathways,
            List<PathwayGene> pathwayGenes,
            int maxNPathways,
            String outputFilePath,
            String title,
            Color[] colorManual,
            ChartType chartType,
            Double dotSize,
            Float dotTransparency

    ) throws IOException {
        EnrichmentTable enrichmentTable = new EnrichmentTable(pathways, degs, pathwayGenes);
        enrichmentTable.calculateEnrichment("output.csv");

        List<EnrichmentResult> results = enrichmentTable.getEnrichmentResults();

        List<EnrichmentResult> significantResults = results.stream()
                .filter(result -> !Double.isNaN(result.adjustedPValue()) && result.adjustedPValue() < 0.05)
                .toList();

        List<EnrichmentResult> topResults = significantResults.stream()
                .sorted(Comparator.comparingDouble(EnrichmentResult::enrichmentScore).reversed())
                .limit(maxNPathways)
                .collect(Collectors.toList());

        String outputFile = (outputFilePath != null && !outputFilePath.isEmpty()) ? outputFilePath
                : (chartType == ChartType.BAR_CHART ? "pathway_enrichment_bar_chart.png" : "pathway_enrichment_dot_plot.png");

        switch (chartType) {
            case BAR_CHART -> {
                new EnrichmentBarChart(
                        title,
                        topResults,
                        pathways,
                        outputFile,
                        colorManual
                );
                System.out.println("Bar chart saved as PNG at: " + outputFile);
            }
            case DOT_CHART -> {
                new EnrichmentDotPlot(
                        title,
                        topResults,
                        pathways,
                        outputFile,
                        colorManual,
                        dotSize != null ? dotSize : 30.0,  // Default size
                        dotTransparency != null ? dotTransparency : 1.0f // Default transparency
                );
                System.out.println("Dot plot saved as PNG at: " + outputFile);
            }
        }
    }

    /**
     * Enum to represent the type of chart to generate.
     */
    public enum ChartType {
        BAR_CHART,
        DOT_CHART
    }
}
