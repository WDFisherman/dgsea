package nl.bioinf.dgsea.visualisations;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.labels.ItemLabelAnchor;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.util.List;
import nl.bioinf.dgsea.data_processing.Pathway;
import nl.bioinf.dgsea.data_processing.EnrichmentResult;

public class EnrichmentDotPlot {
    private String title;
    private List<EnrichmentResult> enrichmentResults;
    private List<Pathway> pathways;
    private String outputFilePath;
    private String[] colorManual; // User-defined colors
    private String colorScheme; // Color scheme if no manual colors are given

    // Constructor to create the dot plot and save it as a PNG
    public EnrichmentDotPlot(String title, List<EnrichmentResult> enrichmentResults, List<Pathway> pathways, String outputFilePath, String[] colorManual, String colorScheme) throws IOException {
        this.title = title;
        this.enrichmentResults = enrichmentResults;
        this.pathways = pathways;
        this.outputFilePath = outputFilePath;
        this.colorManual = colorManual;
        this.colorScheme = colorScheme;

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

        // Set the shape for the dots (circle) and specify the size
        Shape dotShape = new Ellipse2D.Double(-5, -5, 12, 12); // Size of the dots (12x12)
        renderer.setSeriesShape(0, dotShape); // Apply the shape to the first series
        renderer.setSeriesShapesFilled(0, true); // Fill the shapes
        renderer.setSeriesShapesVisible(0, true); // Make sure the shapes are visible

        // Set plot background color for better visibility
        plot.setBackgroundPaint(Color.WHITE);

        // Customize the domain axis (X-axis)
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setVerticalTickLabels(false);  // Don't rotate tick labels
        domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        domainAxis.setTickLabelPaint(Color.BLACK);
        domainAxis.setTickLabelInsets(new RectangleInsets(5, 5, 5, 5));

        // Customize the range axis (Y-axis)
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        rangeAxis.setTickLabelPaint(Color.BLACK);

        // Add gridlines with more contrast
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);

        // Set colors for points based on adjusted p-value
        applyColors(renderer);

        // Add labels with pathway names to the points, positioned to the right of the dots
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
        renderer.setDefaultItemLabelFont(new Font("SansSerif", Font.PLAIN, 14));  // Adjusted label font size for better readability
        renderer.setDefaultItemLabelPaint(Color.BLACK); // Set label color

        // Adjust label positions to avoid overlap
        renderer.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(
                ItemLabelAnchor.OUTSIDE12,  // Position label outside to the right of the point
                TextAnchor.TOP_LEFT // Align to the top right of the label box
        ));

        // Improve label handling to avoid collisions by rotating if necessary
        renderer.setSeriesItemLabelsVisible(0, true); // Enable item labels for the first series
        renderer.setDefaultItemLabelsVisible(true); // Show all labels by default

        // Save the chart as a PNG file
        int width = 1200;    // Increase width for better spacing
        int height = 800;    // Adjust height for better visibility
        File file = new File(outputFilePath);
        ChartUtils.saveChartAsPNG(file, dotPlot, width, height);
    }

    private void applyColors(XYLineAndShapeRenderer renderer) {
        // Apply user-defined colors
        if (colorManual != null && colorManual.length > 0) {
            for (int i = 0; i < enrichmentResults.size(); i++) {
                String color = colorManual[i % colorManual.length];
                renderer.setSeriesPaint(i, Color.decode(color)); // Decode hex color
            }
        } else {
            // Apply a default color scheme if no manual colors are provided
            for (int i = 0; i < enrichmentResults.size(); i++) {
                renderer.setSeriesPaint(i, getDefaultColor(i)); // Use a default color
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

    // Method to create the dataset, adding enrichment score and adjusted p-value
    private XYSeriesCollection createDataset(List<EnrichmentResult> enrichmentResults, List<Pathway> pathways) {
        XYSeries series = new XYSeries("Enrichment Results");

        // Loop through enrichmentResults and add them to the XYSeries
        for (EnrichmentResult result : enrichmentResults) {
            double adjustedPValue = result.adjustedPValue();
            double enrichmentScore = result.enrichmentScore();

            // Add results only if the adjusted p-value is not NaN and below a threshold (e.g., 0.05)
            if (!Double.isNaN(adjustedPValue) && adjustedPValue < 0.05) {
                series.add(adjustedPValue, enrichmentScore);
            }
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        return dataset;
    }
}
