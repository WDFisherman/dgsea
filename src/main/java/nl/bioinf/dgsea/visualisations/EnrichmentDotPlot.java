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
import java.util.*;
import java.util.List;

import nl.bioinf.dgsea.data_processing.Pathway;
import nl.bioinf.dgsea.data_processing.EnrichmentResult;

public class EnrichmentDotPlot {
    private String title;
    private List<EnrichmentResult> enrichmentResults;
    private List<Pathway> pathways;
    private String outputFilePath;
    private String[] colorManual; // User-defined colors
    private String colorScheme;   // Color scheme if no manual colors are given
    private double dotSize;       // Size of the dots
    private float dotTransparency; // Transparency of the dots

    public EnrichmentDotPlot(String title, List<EnrichmentResult> enrichmentResults, List<Pathway> pathways, String outputFilePath, String[] colorManual, String colorScheme, double dotSize, float dotTransparency) throws IOException {
        this.title = title;
        this.enrichmentResults = enrichmentResults;
        this.pathways = pathways;
        this.outputFilePath = outputFilePath;
        this.colorManual = colorManual;
        this.colorScheme = colorScheme;
        this.dotSize = dotSize;
        this.dotTransparency = dotTransparency;

        XYSeriesCollection dataset = createDataset(enrichmentResults, pathways);
        JFreeChart dotPlot = createChart(dataset);

        // Save the chart as a PNG file
        int width = 1200;
        int height = 800;
        File file = new File(outputFilePath);
        ChartUtils.saveChartAsPNG(file, dotPlot, width, height);
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
        renderer.setDefaultOutlinePaint(new Color(0, 0, 0, (int)(dotTransparency * 255))); // Setting outline transparency

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
        for (int i = 0; i < enrichmentResults.size(); i++) {
            Color color;
            if (colorManual != null && colorManual.length > 0) {
                color = getColorFromString(colorManual[i % colorManual.length]);
            } else {
                color = getDefaultColor(i);
            }

            // Pas de transparantie toe op de kleur
            color = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (dotTransparency * 255));
            renderer.setSeriesPaint(i, color);
        }
    }


    private Color getColorFromString(String colorStr) {
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

    private Color getDefaultColor(int index) {
        switch (index % 5) {
            case 0: return Color.RED;
            case 1: return Color.BLUE;
            case 2: return Color.GREEN;
            case 3: return Color.ORANGE;
            case 4: return Color.MAGENTA;
            default: return Color.BLACK; // Fallback color
        }
    }

    private XYSeriesCollection createDataset(List<EnrichmentResult> enrichmentResults, List<Pathway> pathways) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        Set<String> addedSeriesNames = new HashSet<>(); // Set voor unieke serie-namen

        for (int i = 0; i < enrichmentResults.size(); i++) {
            EnrichmentResult result = enrichmentResults.get(i);
            double adjustedPValue = result.adjustedPValue();
            double enrichmentScore = result.enrichmentScore();

            if (!Double.isNaN(adjustedPValue) && adjustedPValue < 0.05) {
                String seriesName = pathways.get(i).description();

                // Controleer of de serie al is toegevoegd
                if (!addedSeriesNames.contains(seriesName)) {
                    XYSeries series = new XYSeries(seriesName);
                    series.add(adjustedPValue, enrichmentScore);
                    dataset.addSeries(series);
                    addedSeriesNames.add(seriesName); // Voeg naam toe aan de set
                } else {
                    // Optioneel: logica om te reageren op een duplicaat
                    System.out.println("Serie met de naam '" + seriesName + "' bestaat al. Overslaan.");
                }
            }
        }

        return dataset;
    }


    private void addItemLabels(XYLineAndShapeRenderer renderer) {
        XYItemLabelGenerator labelGenerator = (dataset, series, item) -> pathways.get(series).description();
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
