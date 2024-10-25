package nl.bioinf.dgsea;

import nl.bioinf.dgsea.data_processing.Deg;
import nl.bioinf.dgsea.data_processing.FileParseUtils;
import nl.bioinf.dgsea.data_processing.Pathway;
import nl.bioinf.dgsea.data_processing.PathwayGene;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class CommonCliOptions {
    public static void main(String[] args) {
        throw new UnsupportedOperationException("This is a dummy class.");
    }
}

class CommonToAll {
    @Option(names = {"-v", "-verbosity"}, description = "Verbose logging", defaultValue = "true")
    boolean[] verbose;

    @Option(names = {"--pval"}, paramLabel = "[0.0-1.0 ? 0.05]", description = "P-value threshold. For counting significant degs in con_table. For filtering degs before making a plot in enrich_bar_chart, enrich_dot_chart and perc_lfc_per_pathway_chart, default = ${DEFAULT-VALUE}", defaultValue="0.01")
    double pval;

    public void setLoggingScope() {
        System.out.println("verbose = " + Arrays.toString(verbose));
        if (verbose.length > 1) {
            LogManager.getLogger().atLevel(Level.WARN).log(verbose);
        } else if (verbose.length > 0) {
            LogManager.getLogger().atLevel(Level.INFO).log(verbose);
        } else {
            LogManager.getLogger().atLevel(Level.ERROR).log(verbose);
        }
    }
}


class CommonFileParams {
    private final FileParseUtils fileParseUtils = new FileParseUtils();

    @Parameters(
            index = "0", paramLabel = "<inputDEGS.csv|tsv>",
            description = "Input degs file in csv or tsv format, columns: gene-symbol, log-fold change and adjusted p-value."
    )
    private File inputFileDegs;

    @Parameters(index = "1", paramLabel = "<inputPathwayDescriptions.csv|tsv>", description = "Input pathway descriptions file, columns: pathway-id and description of pathway.")
    private File inputFilePathwayDescriptions;
    @Parameters(index = "2", paramLabel = "<inputPathwayGenes.csv|tsv>", description = "Input pathway + genes file, columns: pathway-id, entrez gene-id, gene-symbol and ensembl gene-id")
    private File inputFilePathwayGenes;

    public List<Deg> getDegs() {
        try {
            return fileParseUtils.parseDegsFile(inputFileDegs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Pathway> getPathways() {
        try {
            return fileParseUtils.parsePathwayFile(inputFilePathwayDescriptions);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<PathwayGene> getPathwayGenes() {
        try {
            return fileParseUtils.parsePathwayGeneFile(inputFilePathwayGenes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

/**
 * Has common chart cli- params/options. Common to all possible charts. (note: cannot be record because of picocli)
 */
class CommonChartParams {
    Logger logger = LogManager.getLogger();
    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @Option(names = {"--title", "-t", "-T"}, description = "Title of chart")
    String title;
    @Option(names = {"--x-axis"}, description = "X-axis title of chart, default = 'png'", defaultValue = "png")
    String xAxisTitle;
    @Option(names = {"--y-axis"}, description = "Y-axis title of chart, default = 'png'", defaultValue = "png")
    String yAxisTitle;
    @Option(names = {"--image-format"}, paramLabel = "[png|jpg ? png]", description = "Image format of output image, default = 'png'", defaultValue = "png")
    String imageFormat;

    @Option(names = {"--color-manual", "-cm"}, arity = "1..*", split = ";", paramLabel = "red|0xRRGGBB", description = """
    One or more colors to apply to chart. Cycles trough if too few colors were given. Default colors apply if none are given. Options:
    red,green,blue,for more see: https://docs.oracle.com/javase/6/docs/java/awt/Color.html, 000000-FFFFFF, #000000-#FFFFFF, 0x000000-0xFFFFFF""")
    private String[] colorManual;
    @Parameters(index = "3+",paramLabel = "<outputPathImage.*>", description = "Output path of generated chart")
    File outputPath;

    public Color[] getColorManualAsColors() {
        if (colorManual == null) return new Color[0];
        Color[] colorManualAsColors = new Color[colorManual.length];
        for (int i = 0; i < colorManual.length; i++) {
            try {
                colorManualAsColors[i] = Color.decode(colorManual[i]);
            } catch (Exception _) {
                try {
                    Field field = Class.forName("java.awt.Color").getField(colorManual[i % colorManual.length]);
                    colorManualAsColors[i] = (Color) field.get(null);
                } catch (Exception _) {
                    logger.error("Given color was neither hexadecimal, nor a valid Java color string. Given color: {}", colorManual[i % colorManual.length]);
                }
            }
        }
        return Arrays.stream(colorManualAsColors).filter(Objects::nonNull).toArray(Color[]::new);
    }

    public void validateOptions() {
        if (!(Objects.equals(imageFormat, "png") || Objects.equals(imageFormat, "jpg"))) {
            throw new CommandLine.ParameterException(spec.commandLine(), "Invalid image format option. Use '--image-format png or '--image-format jpg'.");
        }
        if (maxNPathways <= 0) {
            throw new CommandLine.ParameterException(spec.commandLine(), "Invalid max number pathways option. Use '--max-n-pathway 1'.");
        }
    }

    public void setColorManual(String[] colorManual) {
        this.colorManual = colorManual;
    }
}