package nl.bioinf.degs.visualisations;

import nl.bioinf.degs.data_processing.Deg;
import nl.bioinf.degs.data_processing.EnrichmentResult;
import nl.bioinf.degs.data_processing.Pathway;
import nl.bioinf.degs.data_processing.PathwayGene;
import org.w3c.dom.ranges.Range;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChartGenerators {
    public String                   title;
    public String                   xAxis;
    public String                   yAxis;
    public int                      dpi;
    public String                   colorScheme;
    public String[]                 colorManual; // Give warning when too few colors are given
    public double                   dotSize;
    public float                    dotTransparency;
    public String                   imageFormat;
    public String                   outputFilePath;
    public HashMap<String, Range>   positionRanges;
    public List<Pathway>            pathways;
    public List<PathwayGene>        pathwayGenes;
    public List<Deg>                degs;
    public List<EnrichmentResult>   enrichmentResults;
    public int                      maxNPathways;


    public ChartGenerators(Builder builder) {
        title               = builder.title;
        xAxis               = builder.xAxis;
        yAxis               = builder.yAxis;
        dpi                 = builder.dpi;
        colorScheme         = builder.colorScheme;
        colorManual         = builder.colorManual;
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
        private final String outputFilePath;



        private int                     dpi = 0;
        private String                  colorScheme = "virdiris";
        private String[]                colorManual = null;
        private double                  dotSize = 1.0;
        private float                   dotTransparency = 1.0f;
        private String                  imageFormat = "png";
        private HashMap<String, Range>  positionRanges = null;
        private List<EnrichmentResult>  enrichmentResults = null;
        private int                     maxNPathways = -1;

        public Builder(String title, String xAxis, String yAxis, List<Pathway> pathways, List<PathwayGene> pathwayGenes, List<Deg> degs, String outputFilePath) {
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
        public Builder dpi(int val) { dpi = val; return this; }
        public Builder colorScheme(String val) { colorScheme = val; return this; }
        public Builder colorManual(String[] val) { colorManual = val; return this; }
        public Builder dotSize(double val) { dotSize = val; return this; }
        public Builder dotTransparency(float val) { dotTransparency = val; return this; }
        public Builder imageFormat(String val) { imageFormat = val; return this; }
        public Builder maxNPathways(int val) { maxNPathways = val; return this; }

        public ChartGenerators build() {
            return new ChartGenerators(this);
        }

    }

    void outputEnrichmentBarChart() {

    }

    void outputEnrichmentDotChart() {

    }

    void outputRunningSumPlot() {

    }

    void outputCummVarChart() {

    }
}
