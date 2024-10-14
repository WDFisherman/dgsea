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

public class ChartGenerator {
    private final String                   title; // chart-styling >>
    private final String                   xAxis;
    private final String                   yAxis;
    private final double                   dpi;
    private final String                   colorScheme;
    private final String[]                 colorManual;
    private final Color                    singleColor;
    private final double                   dotSize;
    private final float                    dotTransparency; //<<
    private final String                   imageFormat;
    private final File                     outputFilePath;
    private final HashMap<String, Range>   positionRanges; // data-selection >>
    private final int                      maxNPathways;
    private Set<String>                    pathwayIds; //<<
    private final List<Pathway>            pathways; // data >>
    private final List<PathwayGene>        pathwayGenes;
    private final List<Deg>                degs;
    private final List<EnrichmentResult>   enrichmentResults; //<<

    private final Logger logger = LogManager.getLogger(ChartGenerator.class.getName());


    public ChartGenerator(Builder builder) {
        title               = builder.title;
        xAxis               = builder.xAxis;
        yAxis               = builder.yAxis;
        dpi                 = builder.dpi;
        colorScheme         = builder.colorScheme;
        colorManual         = builder.colorManual;
        singleColor         = builder.singleColor;
        dotSize             = builder.dotSize;
        dotTransparency     = builder.dotTransparency;
        imageFormat         = builder.imageFormat;
        outputFilePath      = builder.outputFilePath;
        maxNPathways        = builder.maxNPathways;
        positionRanges      = builder.positionRanges;
        pathways            = builder.pathways;
        pathwayGenes        = builder.pathwayGenes;
        degs                = builder.degs;
        enrichmentResults   = builder.enrichmentResults;
        pathwayIds          = builder.pathwayIds;

    }

    public static class Builder {
        private final String title;
        private final String xAxis;
        private final String yAxis;
        private final List<Pathway> pathways;
        private final List<PathwayGene> pathwayGenes;
        private final List<Deg> degs;
        private final File outputFilePath;

        private double                  dpi = 0;
        private String                  colorScheme = "virdiris";
        private String[]                colorManual = null;
        private Color                   singleColor = Color.BLACK;
        private double                  dotSize = 1.0;
        private float                   dotTransparency = 1.0f;
        private String                  imageFormat = "png";
        private HashMap<String, Range>  positionRanges = null;
        private List<EnrichmentResult>  enrichmentResults = null;
        private int                     maxNPathways = -1;
        private Set<String>             pathwayIds = null;

        public Builder(String title, String xAxis, String yAxis, List<Pathway> pathways, List<PathwayGene> pathwayGenes, List<Deg> degs, File outputFilePath) {
            this.title              = title;
            this.xAxis              = xAxis;
            this.yAxis              = yAxis;
            this.pathways           = pathways;
            this.pathwayGenes       = pathwayGenes;
            this.degs               = degs;
            this.outputFilePath     = outputFilePath;
        }

        public Builder positionRanges(HashMap<String, Range> val)
        { positionRanges = val; return this; }
        public Builder enrichmentResults(ArrayList<EnrichmentResult> val)
        { enrichmentResults = val; return this; }
        public void dpi(double val) { dpi = val; }
        public void colorScheme(String val) { colorScheme = val; }
        public void colorManual(String[] val) { colorManual = val; }
        public void singleColor(Color val) { singleColor = val; }
        public void dotSize(double val) { dotSize = val; }
        public void dotTransparency(float val) { dotTransparency = val; }
        public void imageFormat(String val) { imageFormat = val; }
        public void maxNPathways(int val) { maxNPathways = val; }
        public void pathwayIds(Set<String> val) { pathwayIds = val; }

        public ChartGenerator build() {
            return new ChartGenerator(this);
        }

    }

    void outputEnrichmentBarChart() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    void outputEnrichmentDotChart() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    void outputRunningSumPlot() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Gets logFoldChange per deg per pathway,
     *  gets absolute total logFoldChange per pathway,
     *  gets average logFoldChange based on absolute total,
     *  gets total of average logFoldChanges of all pathways.
     * Calculates percentage logFoldChange per pathway.
     * percentage logFoldChange per pathway, based on absolute average logFoldChange per deg in pathway
     */
    public void saveChartPercLogFChangePerPathway() {
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
        cplot.getRenderer().setSeriesPaint(0, singleColor);
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


    private DefaultCategoryDataset getDefaultCategoryDataset() {
        PercLogFChangePerPathway percLogFChangePerPathway = new PercLogFChangePerPathway(this.degs, this.pathwayGenes);
        if (pathwayIds == null) {
            this.pathwayIds = getPathwayAllAvIds();
        }
        Map<String, Double> percentageAllPathways = percLogFChangePerPathway.percAllPathways(pathwayIds);
        DefaultCategoryDataset objDataset = new DefaultCategoryDataset();

         for(Pathway pathway:pathways) {
             if(pathwayIds.contains(pathway.pathwayId())) {
                 objDataset.setValue(percentageAllPathways.get(pathway.pathwayId()),"",pathway.description());
             }
         }
        return objDataset;
    }

    /**
     * Give all available pathway ids, based on the pathwayGenes field/dataset.
     * @return pathway ids
     */
    private LinkedHashSet<String> getPathwayAllAvIds() {
        return new LinkedHashSet<>(pathwayGenes.stream().distinct().map(PathwayGene::pathwayId).toList());
    }

}
