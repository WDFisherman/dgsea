/**
 * Generates one of 3 charts based on present fields.
 * @Authors: Jort Gommers & Willem Daniël Visser
 */

package nl.bioinf.dgsea.visualisations;

import nl.bioinf.dgsea.data_processing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.w3c.dom.ranges.Range;

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
    private final String                 title; // chart-styling >>
    private final String                 xAxis;
    private final String                 yAxis;
    private final double                 dpi;
    private final String                 colorScheme;
    private final String[]               colorManual;
    private final Color                  singleColor;
    private final double                 dotSize;
    private final float                  dotTransparency; //<<
    private final String                 imageFormat;
    private final File                   outputFilePath;
    private final HashMap<String, Range> positionRanges; // data-selection >>
    private final int                    maxNPathways;
    private Set<String>                  pathwayIds; //<<
    private final List<Pathway>          pathways; // data >>
    private final List<PathwayGene>      pathwayGenes;
    private final List<Deg>              degs;
    private final List<EnrichmentResult> enrichmentResults; //<<
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
        private Set<String>            pathwayIds = null;

        public Builder(String title, String xAxis, String yAxis, List<Deg> degs, List<Pathway> pathways, List<PathwayGene> pathwayGenes, File outputFilePath) {
            this.title          = title;
            this.xAxis          = xAxis;
            this.yAxis          = yAxis;
            this.degs           = degs;
            this.pathways       = pathways;
            this.pathwayGenes   = pathwayGenes;
            this.outputFilePath = outputFilePath;
        }

        public Builder positionRanges(HashMap<String, Range> val) {
            positionRanges = val; return this;
        }
        public Builder enrichmentResults(ArrayList<EnrichmentResult> val) {
            enrichmentResults = val; return this;
        }
        public Builder dpi(double val) { dpi = val; return this; }
        public Builder colorScheme(String val) { colorScheme = val; return this; }
        public Builder colorManual(String[] val) { colorManual = val; return this; }
        public Builder singleColor(Color val) { singleColor = val; return this; }
        public Builder dotSize(double val) { dotSize = val; return this; }
        public Builder dotTransparency(float val) { dotTransparency = val; return this; }
        public Builder imageFormat(String val) { imageFormat = val; return this; }
        public Builder maxNPathways(int val) { maxNPathways = val; return this; }
        public Builder pathwayIds(Set<String> val) { pathwayIds = val; return this; }

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
        applyColors(cplot); // Apply user-defined colors to the chart
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
    private void applyColors(CategoryPlot cplot) {
        if (colorManual != null && colorManual.length > 0) {
            for (int i = 0; i < colorManual.length; i++) {
                try {
                    cplot.getRenderer().setSeriesPaint(i, Color.decode(colorManual[i])); // Decode hex color
                } catch (Exception e) {
                    logger.warn("Invalid color code for series {}: {}", i, colorManual[i]);
                    cplot.getRenderer().setSeriesPaint(i, singleColor); // Fallback to single color
                }
            }
        } else {
            cplot.getRenderer().setSeriesPaint(0, singleColor); // Default to single color if no manual colors are specified
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
            if(pathwayIds == null || pathwayIds.contains(pathway.pathwayId())) {
                objDataset.setValue(percentageAllPathways.get(pathway.pathwayId()), "", pathway.description());
            }
        }
        return objDataset;
    }

    /**
     * Give all available pathway ids, based on the pathwayGenes field/dataset.
     * @return pathway ids
     */
    private Set<String> getPathwayAllAvIds() {
        return pathways.stream().map(Pathway::pathwayId).collect(Collectors.toSet());
    }
}
