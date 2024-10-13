package nl.bioinf.dgsea.visualisations;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.ChartUtils;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.List;
import nl.bioinf.dgsea.data_processing.Pathway;
import nl.bioinf.dgsea.data_processing.EnrichmentResult;

public class EnrichmentDotPlot {

    // Constructor om de dot plot aan te maken en als PNG op te slaan
    public EnrichmentDotPlot(String title, List<EnrichmentResult> enrichmentResults, List<Pathway> pathways, String outputFilePath) throws IOException {
        // Maak het dataset aan
        XYSeriesCollection dataset = createDataset(enrichmentResults, pathways);

        // Maak de dot plot aan
        JFreeChart dotPlot = ChartFactory.createScatterPlot(
                title,                          // Title of the chart
                "Adjusted P-Value",            // X-Axis label
                "Enrichment Score",            // Y-Axis label
                dataset,                        // Dataset
                PlotOrientation.VERTICAL,       // Orientation
                true,                           // Show legend
                true,                           // Tooltips
                false                           // URLs
        );

        // Pas de renderer aan om punten te tekenen
        XYPlot plot = dotPlot.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true);
        plot.setRenderer(renderer);

        // Pas kleuren van de punten aan op basis van de adjusted p-value
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

            // Set color for each point
            renderer.setSeriesPaint(i, color);
        }

        // Sla de chart op als een PNG bestand
        int width = 800;    // breedte van het plaatje
        int height = 600;   // hoogte van het plaatje
        File file = new File(outputFilePath);
        ChartUtils.saveChartAsPNG(file, dotPlot, width, height);
    }

    // Methode om het dataset aan te maken, waarbij de enrichment score en adjusted p-value worden toegevoegd
    private XYSeriesCollection createDataset(List<EnrichmentResult> enrichmentResults, List<Pathway> pathways) {
        XYSeries series = new XYSeries("Enrichment Results");

        // Loop door de enrichmentResults en voeg ze toe aan de XYSeries
        for (EnrichmentResult result : enrichmentResults) {
            double adjustedPValue = result.adjustedPValue();
            double enrichmentScore = result.enrichmentScore();

            // Voeg alleen resultaten toe als de adjusted p-value niet NaN is
            if (!Double.isNaN(adjustedPValue)) {
                series.add(adjustedPValue, enrichmentScore);
            }
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        return dataset;
    }
}
