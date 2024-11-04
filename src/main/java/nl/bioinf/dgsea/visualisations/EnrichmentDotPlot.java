package nl.bioinf.dgsea.visualisations;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.labels.ItemLabelAnchor;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import nl.bioinf.dgsea.data_processing.Pathway;
import nl.bioinf.dgsea.data_processing.EnrichmentResult;

/**
 * Class to create a dot plot for enrichment results using JFreeChart.
 * This plot visually represents the relationship between adjusted p-values and enrichment scores
 * for a set of enrichment results and their corresponding pathways.
 */
public class EnrichmentDotPlot {
    private final String title;
    private final List<EnrichmentResult> enrichmentResults;
    private final List<Pathway> pathways;
    private final Color[] colorManual; // User-defined colors\
    private double dotSize;       // Size of the dots
    private float dotTransparency; // Transparency of the dots
    private final Logger logger = LogManager.getLogger(EnrichmentDotPlot.class);

    /**
     * Constructor for EnrichmentDotPlot.
     *
     * @param title              The title of the plot.
     * @param enrichmentResults  The list of enrichment results.
     * @param pathways           The list of pathways corresponding to the enrichment results.
     * @param outputFilePath     The file path to save the plot.
     * @param colorManual        User-defined colors for the dots.
     * @param dotSize            Size of the dots.
     * @param dotTransparency    Transparency of the dots.
     * @throws IOException If an error occurs while saving the chart.
     */
    public EnrichmentDotPlot(String title, List<EnrichmentResult> enrichmentResults,
                             List<Pathway> pathways, String outputFilePath,
                             Color[] colorManual,
                             double dotSize, float dotTransparency) throws IOException {
        this.title = title;
        this.enrichmentResults = enrichmentResults;
        this.pathways = pathways;
        this.colorManual = colorManual;
        setDotSize(dotSize);
        setDotTransparency(dotTransparency);

        XYSeriesCollection dataset = createDataset(enrichmentResults, pathways);
        JFreeChart dotPlot = createChart(dataset);

        // Save the chart as a PNG file
        int width = 1200;
        int height = 800;
        File file = new File(outputFilePath);
        ChartUtils.saveChartAsPNG(file, dotPlot, width, height);
    }

    /**
     * Sets the size of the dots in the plot.
     *
     * @param dotSize The size of the dots, must be positive.
     * @throws IllegalArgumentException if dotSize is not positive.
     */
    void setDotSize(double dotSize) {
        if (dotSize <= 0) {
            throw new IllegalArgumentException("Dot size must be positive.");
        }
        this.dotSize = dotSize;
    }

    /**
     * Sets the transparency of the dots.
     *
     * @param dotTransparency The transparency value between 0 (completely transparent) and 1 (completely opaque).
     * @throws IllegalArgumentException if dotTransparency is not between 0 and 1.
     */
    void setDotTransparency(float dotTransparency) {
        if (dotTransparency < 0 || dotTransparency > 1) {
            throw new IllegalArgumentException("Transparency must be between 0 and 1.");
        }
        this.dotTransparency = dotTransparency;
    }

    /**
     * Creates the dot plot chart using the specified dataset.
     *
     * @param dataset The dataset containing the enrichment results.
     * @return A JFreeChart object representing the dot plot.
     */
    private JFreeChart createChart(XYSeriesCollection dataset) {
        JFreeChart dotPlot = ChartFactory.createScatterPlot(
                title,
                "Adjusted P-Value",
                "Enrichment Score",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        XYPlot plot = dotPlot.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true);
        plot.setRenderer(renderer);

        // Set the shape for the dots using the specified dot size and transparency
        Shape dotShape = new Ellipse2D.Double(-dotSize / 2, -dotSize / 2, dotSize, dotSize);
        renderer.setDefaultShape(dotShape);
        renderer.setDefaultShapesFilled(true);
        renderer.setDefaultShapesVisible(true);

        // Set dot transparency
        renderer.setDefaultOutlinePaint(new Color(0, 0, 0, (int)(dotTransparency * 255)));

        // Customize plot background
        plot.setBackgroundPaint(Color.WHITE);

        // Customize axes
        customizeAxes(plot);

        // Apply colors
        applyColors(renderer);

        // Add labels to points
        addItemLabels(renderer);

        return dotPlot;
    }

    /**
     * Customizes the appearance of the axes in the plot.
     *
     * @param plot The XYPlot object to customize.
     */
    private void customizeAxes(XYPlot plot) {
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setVerticalTickLabels(false);
        domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        domainAxis.setTickLabelPaint(Color.BLACK);
        domainAxis.setTickLabelInsets(new RectangleInsets(5, 5, 5, 5));

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        rangeAxis.setTickLabelPaint(Color.BLACK);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);
    }

    /**
     * Applies colors to the dots in the plot based on user-defined colors or default colors.
     *
     * @param renderer The renderer to apply colors to.
     */
    private void applyColors(XYLineAndShapeRenderer renderer) {
        Color colorItem;
        for (int i = 0; i < enrichmentResults.size(); i++) {
            if (colorManual == null || colorManual.length == 0) {
                colorItem = EnrichmentBarChart.getDefaultColor(i);
            } else {
                colorItem = colorManual[i % colorManual.length];
            }
            colorItem = new Color(colorItem.getRed(), colorItem.getGreen(), colorItem.getBlue(), (int) (dotTransparency * 255));
            renderer.setSeriesPaint(i, colorItem);
        }
    }

    /**
     * Creates a dataset for the dot plot from the enrichment results and pathways.
     *
     * @param enrichmentResults The enrichment results to be plotted.
     * @param pathways         The pathways corresponding to the enrichment results.
     * @return An XYSeriesCollection containing the dataset.
     */
    XYSeriesCollection createDataset(List<EnrichmentResult> enrichmentResults, List<Pathway> pathways) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        Set<String> addedSeriesNames = new HashSet<>(); // Set for unique series names

        for (int i = 0; i < enrichmentResults.size(); i++) {
            EnrichmentResult result = enrichmentResults.get(i);
            double adjustedPValue = result.adjustedPValue();
            double enrichmentScore = result.enrichmentScore();

            if (!Double.isNaN(adjustedPValue) && adjustedPValue < 0.05) {
                String seriesName = pathways.get(i).description();

                // Check if the series has already been added
                if (!addedSeriesNames.contains(seriesName)) {
                    XYSeries series = new XYSeries(seriesName);
                    series.add(adjustedPValue, enrichmentScore);
                    dataset.addSeries(series);
                    addedSeriesNames.add(seriesName); // Add name to the set
                } else {
                    // Optional: log the duplicate series name
                    logDuplicateSeries(seriesName);
                }
            }
        }

        return dataset;
    }

    /**
     * Logs a message if a duplicate series name is encountered.
     *
     * @param seriesName The name of the series that is a duplicate.
     */
    private void logDuplicateSeries(String seriesName) {
        // Placeholder for logging duplicates, can be enhanced with a logging framework
        logger.error("Series with the name '{}' already exists. Skipping.", seriesName);
    }

    /**
     * Adds item labels to the points in the plot, indicating the pathway description.
     *
     * @param renderer The renderer to which the item labels are added.
     */
    private void addItemLabels(XYLineAndShapeRenderer renderer) {
        XYItemLabelGenerator labelGenerator = (_, series, _) -> pathways.get(series).description();
        renderer.setDefaultItemLabelGenerator(labelGenerator);
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(new Font("SansSerif", Font.PLAIN, 14));
        renderer.setDefaultItemLabelPaint(Color.BLACK);

        renderer.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(
                ItemLabelAnchor.OUTSIDE12,
                TextAnchor.TOP_LEFT
        ));
    }
}
