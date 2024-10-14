package nl.bioinf.dgsea.visualisations;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.ChartUtils;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.List;
import nl.bioinf.dgsea.data_processing.Pathway;
import nl.bioinf.dgsea.data_processing.EnrichmentResult;

public class EnrichmentBarChart {

    // Constructor om de bar chart aan te maken en als PNG op te slaan
    public EnrichmentBarChart(String title, List<EnrichmentResult> enrichmentResults, List<Pathway> pathways, String outputFilePath) throws IOException {
        // Maak het dataset aan
        DefaultCategoryDataset dataset = createDataset(enrichmentResults, pathways);

        // Maak de bar chart aan
        JFreeChart barChart = ChartFactory.createBarChart(
                title,                    // Title of the chart
                "Pathway",               // X-Axis label
                "Enrichment Score",      // Y-Axis label
                dataset
        );

        // Customize de renderer voor het aanpassen van kleuren
        CategoryPlot plot = barChart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        // Pas de kleuren van de balken aan op basis van de adjusted p-value
        for (int i = 0; i < enrichmentResults.size(); i++) {
            EnrichmentResult result = enrichmentResults.get(i);
            double adjustedPValue = result.adjustedPValue();

            // Kies een kleur op basis van de p-adjust waarde
            Color color;
            if (adjustedPValue < 0.01) {
                color = Color.RED;
            } else if (adjustedPValue < 0.05) {
                color = Color.ORANGE;
            } else {
                color = Color.GREEN;
            }

            // Pas de kleur toe op de serie (balk)
            renderer.setSeriesPaint(i, color);
        }

        // Sla de chart op als een PNG bestand
        int width = 800;    // breedte van het plaatje
        int height = 600;   // hoogte van het plaatje
        File file = new File(outputFilePath);
        ChartUtils.saveChartAsPNG(file, barChart, width, height);
    }

    // Methode om het dataset aan te maken, waarbij de beschrijving van de pathway wordt toegevoegd
    private DefaultCategoryDataset createDataset(List<EnrichmentResult> enrichmentResults, List<Pathway> pathways) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Loop door de enrichmentResults en match ze met de juiste Pathway om de beschrijving te krijgen
        for (EnrichmentResult result : enrichmentResults) {
            Pathway matchingPathway = pathways.stream()
                    .filter(pathway -> pathway.pathwayId().equals(result.pathwayId()))
                    .findFirst()
                    .orElse(null);  // Voeg error handling toe voor niet-gevonden pathways als dat nodig is

            if (matchingPathway != null) {
                String description = matchingPathway.description();  // Haal de beschrijving van de pathway op
                dataset.addValue(result.enrichmentScore(), description, "Enrichment Score");
            }
        }

        return dataset;
    }
}
