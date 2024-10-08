package nl.bioinf.dgsea.visualisations;

import nl.bioinf.dgsea.data_processing.Deg;
import nl.bioinf.dgsea.data_processing.EnrichmentResult;
import nl.bioinf.dgsea.data_processing.Pathway;
import nl.bioinf.dgsea.data_processing.PathwayGene;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChartGenerators {
    final String                   title;
    final String                   xAxis;
    final String                   yAxis;
    final double                   dpi;
    final String                   colorScheme;
    final String[]                 colorManual; // Give warning when too few colors are given
    final Color                    singleColor;
    final double                   dotSize;
    final float                    dotTransparency;
    final String                   imageFormat;
    final File                     outputFilePath;
    final HashMap<String, Range>   positionRanges;
    final List<Pathway>            pathways;
    final List<PathwayGene>        pathwayGenes;
    final List<Deg>                degs;
    final List<EnrichmentResult>   enrichmentResults;
    final int                      maxNPathways;


    public ChartGenerators(Builder builder) {
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
        maxNPathways        = builder.maxNPathways;
        positionRanges      = builder.positionRanges;
        pathways            = builder.pathways;
        pathwayGenes        = builder.pathwayGenes;
        degs                = builder.degs;
        enrichmentResults   = builder.enrichmentResults;
        outputFilePath      = builder.outputFilePath;

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

        public ChartGenerators build() {
            return new ChartGenerators(this);
        }

    }

    void outputEnrichmentBarChart() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    void outputEnrichmentDotChart() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    void outputRunningSumPlot() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    void outputCummVarChart() throws IOException {
        DefaultCategoryDataset objDataset = CummVarChart.getDefaultCategoryDataset();

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
        if (imageFormat.equals("png")) {
            ChartUtils.saveChartAsPNG(outputFilePath, objChart, 1000, 1000);
        } else {
            ChartUtils.saveChartAsJPEG(outputFilePath, 1.0f, objChart, 1000, 1000);
        }

    }

    static class CummVarChart {
        private static DefaultCategoryDataset getDefaultCategoryDataset() {
            DefaultCategoryDataset objDataset = new DefaultCategoryDataset();

            objDataset.setValue(65,"","Glycolysis / Gluconeogenesis");
            objDataset.setValue(24,"","Citrate cycle (TCA cycle)");
            objDataset.setValue(11,"","Pentose phosphate pathway");
            return objDataset;
        }

        /**
         * Calculates average log-fold-change on genes in a particular pathway.
         * @param pathwayId hsa or similar id, common in `Table.pathways` and `Table.pathwayGenes`
         */
        private static double averageLogFChangePathway(String pathwayId) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }


}
