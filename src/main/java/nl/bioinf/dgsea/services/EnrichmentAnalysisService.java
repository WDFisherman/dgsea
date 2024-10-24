package nl.bioinf.dgsea.services;

import nl.bioinf.dgsea.data_processing.*;
import nl.bioinf.dgsea.table_outputs.EnrichmentTable;
import nl.bioinf.dgsea.visualisations.EnrichmentBarChart;
import nl.bioinf.dgsea.visualisations.EnrichmentDotPlot;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EnrichmentAnalysisService {

    // Methode voor het genereren van de bar-chart
    public void generateEnrichmentBarChart(List<Deg> degs, List<Pathway> pathways, List<PathwayGene> pathwayGenes, int maxNPathways, String outputFilePath, String[] colorManual, String colorScheme) throws IOException {
        EnrichmentTable enrichmentTable = new EnrichmentTable(pathways, degs, pathwayGenes);
        enrichmentTable.calculateEnrichment();
        List<EnrichmentResult> results = enrichmentTable.getEnrichmentResults();

        List<EnrichmentResult> significantResults = results.stream()
                .filter(result -> !Double.isNaN(result.adjustedPValue()) && result.adjustedPValue() < 0.05)
                .toList();

        List<EnrichmentResult> topResults = significantResults.stream()
                .sorted(Comparator.comparingDouble(EnrichmentResult::enrichmentScore).reversed())
                .limit(maxNPathways)
                .collect(Collectors.toList());

        String outputFile = (outputFilePath != null && !outputFilePath.isEmpty()) ? outputFilePath : "pathway_enrichment_bar_chart.png";

        EnrichmentBarChart barChart = new EnrichmentBarChart(
                "Top " + maxNPathways + " Pathway Enrichment",
                topResults,
                pathways,
                outputFile,
                colorManual,
                colorScheme
        );

        System.out.println("Bar chart saved as PNG at: " + outputFile);
    }

    // Nieuwe methode voor het genereren van de dot-chart
    public void generateEnrichmentDotChart(List<Deg> degs, List<Pathway> pathways, List<PathwayGene> pathwayGenes, int maxNPathways, double dotSize, float dotTransparency, String outputFilePath, String[] colorManual, String colorScheme) throws IOException {
        EnrichmentTable enrichmentTable = new EnrichmentTable(pathways, degs, pathwayGenes);
        enrichmentTable.calculateEnrichment();
        List<EnrichmentResult> results = enrichmentTable.getEnrichmentResults();

        List<EnrichmentResult> significantResults = results.stream()
                .filter(result -> !Double.isNaN(result.adjustedPValue()) && result.adjustedPValue() < 0.05)
                .toList();

        List<EnrichmentResult> topResults = significantResults.stream()
                .sorted(Comparator.comparingDouble(EnrichmentResult::enrichmentScore).reversed())
                .limit(maxNPathways)
                .collect(Collectors.toList());

        String dotPlotOutputFile = (outputFilePath != null && !outputFilePath.isEmpty()) ? outputFilePath : "pathway_enrichment_dot_plot.png";

        EnrichmentDotPlot dotPlot = new EnrichmentDotPlot(
                "Pathway Enrichment Dot Plot (Top " + maxNPathways + ")",
                topResults,
                pathways,
                dotPlotOutputFile,
                colorManual,
                colorScheme,
                dotSize,
                dotTransparency
        );

        System.out.println("Dot plot saved as PNG at: " + dotPlotOutputFile);
    }
}
