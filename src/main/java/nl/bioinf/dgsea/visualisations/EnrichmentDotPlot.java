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
 */
public class EnrichmentDotPlot {
    private final String title;
    private final List<EnrichmentResult> enrichmentResults;
    private final List<Pathway> pathways;
    private final Color[] colorManual; // User-defined colors\
    private double dotSize;       // Size of the dots
    private float dotTransparency; // Transparency of the dots

    /**
     * Constructor for EnrichmentDotPlot.
     *
     * @param title              The title of the plot.
     * @param enrichmentResults  The list of enrichment results.
     * @param pathways           The list of pathways.
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

    private void setDotSize(double dotSize) {
        if (dotSize <= 0) {
            throw new IllegalArgumentException("Dot size must be positive.");
        }
        this.dotSize = dotSize;
    }

    private void setDotTransparency(float dotTransparency) {
        if (dotTransparency < 0 || dotTransparency > 1) {
            throw new IllegalArgumentException("Transparency must be between 0 and 1.");
        }
        this.dotTransparency = dotTransparency;
    }

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

    private void applyColors(XYLineAndShapeRenderer renderer) {
        Color colorItem;
        for (int i = 0; i < enrichmentResults.size(); i++) {
            if (colorManual == null || colorManual.length == 0) {
                colorItem = getDefaultColor(i);
            } else {
                colorItem = colorManual[i % colorManual.length];
            }
            colorItem = new Color(colorItem.getRed(), colorItem.getGreen(), colorItem.getBlue(), (int) (dotTransparency * 255));
            renderer.setSeriesPaint(i, colorItem);
        }
    }

    Color getColorFromString(String colorStr) {
        // Mapping of common color names to Color objects
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

        // Check if the colorStr is a named color
        if (colorNameMap.containsKey(colorStr.toLowerCase())) {
            return colorNameMap.get(colorStr.toLowerCase());
        }

        // Otherwise, try interpreting it as a hex color code
        try {
            return Color.decode(colorStr);
        } catch (NumberFormatException e) {
            return Color.GRAY; // Fallback color
        }
    }

    Color getDefaultColor(int index) {
        return switch (index % 5) {
            case 0 -> Color.RED;
            case 1 -> Color.BLUE;
            case 2 -> Color.GREEN;
            case 3 -> Color.ORANGE;
            default -> Color.MAGENTA; // Fallback color
        };
    }

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

    private void logDuplicateSeries(String seriesName) {
        // Placeholder for logging duplicates, can be enhanced with a logging framework
        System.err.println("Series with the name '" + seriesName + "' already exists. Skipping.");
    }

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
