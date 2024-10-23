package nl.bioinf.dgsea.visualisations;

import nl.bioinf.dgsea.data_processing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.Range;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates one of 3 charts based on present fields and function called.
 * Fields: dotSize, dotTransparency, positionRanges, maxNPathway and enrichmentResults are situation specific.
 * Builder ChartGenerator.Builder is available for selective field assignation.
 */
public class ChartGenerator {
    private final String                 title;
    private final String                 xAxis;
    private final String                 yAxis;
    private final double                 dpi;
    private final String                 colorScheme;
    private final String[]               colorManual;
    private final Color                  singleColor;
    private final double                 dotSize;
    private final float                  dotTransparency;
    private final String                 imageFormat;
    private final File                   outputFilePath;
    private final HashMap<String, Range> positionRanges;
    private final int                    maxNPathways;
    private String[]                     pathwayIds;
    private final List<Pathway>          pathways;
    private final List<PathwayGene>      pathwayGenes;
    private final List<Deg>              degs;
    private final List<EnrichmentResult> enrichmentResults;
    private final Logger logger = LogManager.getLogger(ChartGenerator.class.getName());

    public ChartGenerator(Builder builder) {
        title             = builder.title;
        xAxis             = builder.xAxis;
        yAxis             = builder.yAxis;
        dpi               = builder.dpi;
        colorScheme       = builder.colorScheme;
        colorManual       = builder.colorManual;
        singleColor       = builder.singleColor;
        dotSize           = builder.dotSize;
        dotTransparency   = builder.dotTransparency;
        imageFormat       = builder.imageFormat;
        outputFilePath    = builder.outputFilePath;
        maxNPathways      = builder.maxNPathways;
        positionRanges    = builder.positionRanges;
        pathways          = builder.pathways;
        pathwayGenes      = builder.pathwayGenes;
        degs              = builder.degs;
        enrichmentResults = builder.enrichmentResults;
        pathwayIds        = builder.pathwayIds;
    }

    public static class Builder {
        private final String title;
        private final String xAxis;
        private final String yAxis;
        private final List<Pathway> pathways;
        private final List<PathwayGene> pathwayGenes;
        private final List<Deg> degs;
        private final File outputFilePath;

        private double                 dpi = 0;
        private String                 colorScheme = "viridis";
        private String[]               colorManual = null;
        private Color                  singleColor = Color.BLACK;
        private double                 dotSize = 1.0;
        private float                  dotTransparency = 1.0f;
        private String                 imageFormat = "png";
        private HashMap<String, Range> positionRanges = null;
        private List<EnrichmentResult> enrichmentResults = null;
        private int                    maxNPathways = -1;
        private String[]               pathwayIds = null;

        public Builder(String title, String xAxis, String yAxis, List<Deg> degs, List<Pathway> pathways, List<PathwayGene> pathwayGenes, File outputFilePath) {
            this.title          = title;
            this.xAxis          = xAxis;
            this.yAxis          = yAxis;
            this.degs           = degs;
            this.pathways       = pathways;
            this.pathwayGenes   = pathwayGenes;
            this.outputFilePath = outputFilePath;
        }

        public Builder positionRanges(HashMap<String, Range> val)
        { positionRanges = val; return this; }
        public Builder enrichmentResults(ArrayList<EnrichmentResult> val)
        { enrichmentResults = val; return this; }
        public Builder dpi(double val) {            dpi = val; return this;}
        public Builder colorScheme(String val) {    colorScheme = val; return this;}
        public Builder colorManual(String[] val) {  colorManual = val; return this;}
        public Builder singleColor(Color val) {     singleColor = val; return this;}
        public Builder dotSize(double val) {        dotSize = val; return this;}
        public Builder dotTransparency(float val) { dotTransparency = val; return this;}
        public Builder imageFormat(String val) {    imageFormat = val; return this;}
        public Builder maxNPathways(int val) {      maxNPathways = val; return this;}
        public Builder pathwayIds(String[] val) { pathwayIds = val; return this;}

        public ChartGenerator build() {
            return new ChartGenerator(this);
        }
    }

    /**
     * Generates a bar chart based on percentage log fold change per pathway.
     */
    public void saveChartPercLogFChangePerPathway() {
        DefaultCategoryDataset objDataset = getDefaultCategoryDataset();

        JFreeChart objChart = ChartFactory.createBarChart(
                title,
                xAxis,
                yAxis,
                objDataset, // Chart Data
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        CategoryPlot cplot = (CategoryPlot)objChart.getPlot();
        applyColors(cplot, objDataset); // Apply user-defined colors to the chart
        try {
            if (imageFormat.equals("png")) {
                ChartUtils.saveChartAsPNG(outputFilePath, objChart, 1000, 1000);
            } else {
                ChartUtils.saveChartAsJPEG(outputFilePath, 1.0f, objChart, 1000, 1000);
            }
            logger.info("Chart was saved to file: {}", outputFilePath);
        } catch(IOException e) {
            logger.error("Failed to save chart to image file, error: {}", String.valueOf(e));
        }
    }

    /**
     * Applies colors to the chart based on user input or defaults.
     */
    private void applyColors(CategoryPlot cplot, DefaultCategoryDataset dataset) {
        int seriesCount = dataset.getRowCount();  // Aantal series (categorieën)

        if (colorManual != null && colorManual.length > 0) {
            for (int i = 0; i < seriesCount; i++) {
                try {
                    cplot.getRenderer().setSeriesPaint(i, Color.decode(colorManual[i % colorManual.length])); // Gebruik mod om kleuren te herhalen
                } catch (Exception e) {
                    logger.warn("Invalid color code for series {}: {}", i, colorManual[i % colorManual.length]);
                    cplot.getRenderer().setSeriesPaint(i, singleColor); // Fallback naar enkele kleur
                }
            }
        } else {
            for (int i = 0; i < seriesCount; i++) {
                cplot.getRenderer().setSeriesPaint(i, getDefaultColor(i)); // Standaardkleur toepassen
            }
        }
    }

    /**
     * Provides default color for the chart when no manual color is provided.
     */
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

    /**
     * Gets calculated data then transforms it into bar-chart/categorical data.
     * @return bar-chart/categorical data
     */
    private DefaultCategoryDataset getDefaultCategoryDataset() {
        DefaultCategoryDataset objDataset = new DefaultCategoryDataset();
        PercLogFChangePerPathway percLogFChangePerPathway = new PercLogFChangePerPathway(this.degs, this.pathwayGenes);
        if (pathwayIds == null) { // not provided by end-user
            this.pathwayIds = getPathwayAllAvIds();
        }
        Map<String, Double> percentageAllPathways = percLogFChangePerPathway.percAllPathways(pathwayIds);


         for(Pathway pathway:pathways) {
             if(Arrays.stream(pathwayIds).noneMatch(pathwayId->pathwayId.equals(pathway.pathwayId()))) {
                 objDataset.setValue(percentageAllPathways.get(pathway.pathwayId()),"",pathway.description());
             }
         }
        return objDataset;
    }

    /**
     * Give all available pathway ids, based on the pathwayGenes field/dataset.
     * @return pathway ids
     */
    private String[] getPathwayAllAvIds() {
        return pathways.stream().map(Pathway::pathwayId).distinct().toArray(String[]::new);
    }
}
