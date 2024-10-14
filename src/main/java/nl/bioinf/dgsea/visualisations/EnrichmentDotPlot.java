package nl.bioinf.dgsea.visualisations;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.labels.ItemLabelAnchor;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import nl.bioinf.dgsea.data_processing.Pathway;
import nl.bioinf.dgsea.data_processing.EnrichmentResult;

public class EnrichmentDotPlot {

    // Constructor to create the dot plot and save it as a PNG
    public EnrichmentDotPlot(String title, List<EnrichmentResult> enrichmentResults, List<Pathway> pathways, String outputFilePath) throws IOException {
        // Create the dataset
        XYSeriesCollection dataset = createDataset(enrichmentResults, pathways);

        // Create the dot plot
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

        // Customize the plot renderer
        XYPlot plot = dotPlot.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true); // Draw only shapes (dots), no lines
        plot.setRenderer(renderer);

        // Customize the domain axis (X-axis)
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setVerticalTickLabels(false);  // Don't rotate tick labels
        domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));
        domainAxis.setTickLabelPaint(Color.BLACK);
        domainAxis.setTickLabelInsets(new RectangleInsets(5, 5, 5, 5));

        // Add gridlines
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // Set colors for points based on adjusted p-value
        for (int i = 0; i < enrichmentResults.size(); i++) {
            EnrichmentResult result = enrichmentResults.get(i);
            double adjustedPValue = result.adjustedPValue();

            // Choose a color based on the p-adjust value
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

        // Add labels with pathway names to the points, avoiding overlap
        XYItemLabelGenerator labelGenerator = new XYItemLabelGenerator() {
            @Override
            public String generateLabel(XYDataset dataset, int series, int item) {
                // Retrieve the pathway name corresponding to this point
                Pathway pathway = pathways.get(item);
                return pathway.description();
            }
        };

        // Set the item label generator and make labels visible
        renderer.setDefaultItemLabelGenerator(labelGenerator);
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(new Font("SansSerif", Font.PLAIN, 9));  // Adjusted label font size
        renderer.setDefaultItemLabelPaint(Color.BLACK); // set label colour

        // Set the item label position
        renderer.setDefaultPositiveItemLabelPosition(new org.jfree.chart.labels.ItemLabelPosition(
                ItemLabelAnchor.OUTSIDE12,  // Positioning doesnt work
                org.jfree.chart.ui.TextAnchor.BOTTOM_CENTER));  // Align to bottom center of the point

        // Save the chart as a PNG file
        int width = 1000;    // Increase width for better spacing
        int height = 700;    // Adjust height for better visibility
        File file = new File(outputFilePath);
        ChartUtils.saveChartAsPNG(file, dotPlot, width, height);
    }

    // Method to create the dataset, adding enrichment score and adjusted p-value
    private XYSeriesCollection createDataset(List<EnrichmentResult> enrichmentResults, List<Pathway> pathways) {
        XYSeries series = new XYSeries("Enrichment Results");

        // Loop through enrichmentResults and add them to the XYSeries
        for (EnrichmentResult result : enrichmentResults) {
            double adjustedPValue = result.adjustedPValue();
            double enrichmentScore = result.enrichmentScore();

            // Add results only if the adjusted p-value is not NaN
            if (!Double.isNaN(adjustedPValue)) {
                series.add(adjustedPValue, enrichmentScore);
            }
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        return dataset;
    }
}
