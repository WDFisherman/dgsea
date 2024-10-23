/**
 * Responsible for building, generating and saving the log-fold-change chart
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

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;

/**
 * Percentage log-fold-change(lfc) bar-chart,
 *      that makes chart-data,
 *      using results from PercLogFChangePerPathway and then forms the chart,
 *      followed by saving this to an image file. <br>
 * The resulting chart displays 1 bar for each pathway with height indicative to how high the absolute average lfc is.
 * Builder ChartGenerator.Builder is available for selective field assignation.
 */
public class PercLfcBarChart {
    private final String                 title; // chart-styling >>
    private final String                 xAxis;
    private final String                 yAxis;
    private final String[]               colorManual; //<<
    private final String                 imageFormat;
    private final File                   outputFilePath; // data-selection >>
    private final int                    maxNPathways;
    private String[]                     pathwayIds; //<<
    private final List<Pathway>          pathways; // data >>
    private final List<PathwayGene>      pathwayGenes;
    private final List<Deg>              degs;//<<
    private final Logger logger = LogManager.getLogger(PercLfcBarChart.class.getName());

    public PercLfcBarChart(Builder builder) {
        title             = builder.title;
        xAxis             = builder.xAxis;
        yAxis             = builder.yAxis;
        colorManual       = builder.colorManual;
        imageFormat       = builder.imageFormat;
        outputFilePath    = builder.outputFilePath;
        maxNPathways      = builder.maxNPathways;
        pathways          = builder.pathways;
        pathwayGenes      = builder.pathwayGenes;
        degs              = builder.degs;
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

        private String[]               colorManual = null;
        private String                 imageFormat = "png";
        private int                    maxNPathways = 20;
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

        public Builder colorManual(String[] val) {  colorManual = val; return this;}
        public Builder imageFormat(String val) {    imageFormat = val; return this;}
        public Builder maxNPathways(int val) {      maxNPathways = val; return this;}
        public Builder pathwayIds(String[] val) { pathwayIds = val; return this;}


    }

    /**
     * Gets calculated data then transforms it to bar-chart/categorical data.
     *  Then makes bar-chart and saves this to an image.
     */
    public void saveChart() {
        DefaultCategoryDataset objDataset = getDefaultCategoryDataset();

        JFreeChart objChart = ChartFactory.createBarChart(
                title,
                xAxis,
                yAxis,
                objDataset, //Chart Data
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
            throw new RuntimeException("Failed to save chart to image file, given file path: %s".formatted(outputFilePath));
        }
    }

    /**
     * Gets calculated data then transforms it into bar-chart/categorical data.
     * @return bar-chart/categorical data
     */
    private DefaultCategoryDataset getDefaultCategoryDataset() {
        DefaultCategoryDataset objDataset = new DefaultCategoryDataset();
        PercLfcPathways percLfcPathways = new PercLfcPathways(this.degs, this.pathwayGenes);
        if (pathwayIds == null) { // not provided by end-user
            this.pathwayIds = getPathwayAllAvIds();
        }
        double[] percentageAllPathways = percLfcPathways.percAllPathways(pathwayIds);

        Map<String, Double> percentageSomePathways = percLfcPathways.filterMostInfluentialPathways(maxNPathways, percentageAllPathways, pathwayIds);
        for(Pathway pathway:pathways) {
            if(isInSelectedPathways(pathway.pathwayId(), percentageSomePathways.keySet().stream().toList())) {
                objDataset.setValue(percentageSomePathways.get(pathway.pathwayId()),pathway.description(),"");
            }
        }
        return objDataset;
    }

    /**
     * Check if given pathway id is in list of selected ids
     * @param curPathwayId given pathway id
     * @param pathwayIdList list of selected ids
     * @return true if it is in, false otherwise
     */
    private boolean isInSelectedPathways(String curPathwayId, Collection<String> pathwayIdList) {
        return pathwayIdList.contains(curPathwayId);
    }

    /**
     * Give all available pathway ids, based on the pathwayGenes field/dataset.
     * @return pathway ids
     */
    private String[] getPathwayAllAvIds() {
        return pathways.stream().map(Pathway::pathwayId).distinct().toArray(String[]::new);
    }

    /**
     * Applies colors to the chart based on user input or defaults.
     * @param cplot plot to set series paint for
     */
    private void applyColors(CategoryPlot cplot) {
        if (colorManual != null && colorManual.length > 0) {
            for (int i = 0; i < pathwayIds.length; i++) {
                Color color;
                try {
                    color = Color.decode(colorManual[i % colorManual.length]);
                } catch (NumberFormatException e) {
                    try {
                        Field field = Class.forName("java.awt.Color").getField(colorManual[i % colorManual.length]);
                        color = (Color)field.get(null);
                    } catch (Exception e2) {
                        color = getDefaultColor(i);
                        logger.warn("Given color was neither decimal, octal, or hexidecimal, nor a valid Java color string. Given color: " + colorManual[i % colorManual.length]);
                    }
                }
                cplot.getRenderer().setSeriesPaint(i, color);
            }
        } else {
            for (int i = 0; i < pathwayIds.length; i++) {
                cplot.getRenderer().setSeriesPaint(i, getDefaultColor(i)); // Standaardkleur toepassen
            }
        }
    }

    /**
     * Provides default color for the chart when no manual color is provided.
     * @param index used to choose one of 5 colors
     * @return Color red,blue,green,orange, or black
     */
    private Color getDefaultColor(int index) {
        return switch (index % 5) {
            case 0 -> Color.RED;
            case 1 -> Color.BLUE;
            case 2 -> Color.GREEN;
            case 3 -> Color.ORANGE;
            default -> Color.BLACK;
        };
    }

}
