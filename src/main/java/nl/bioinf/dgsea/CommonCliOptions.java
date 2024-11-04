/**
 * Common command-line options and parameters for the DGSEA application.
 * This class serves as a utility to define and manage options that are common
 * across various commands within the application.
 */
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

import java.awt.*;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Dummy class to satisfy the application's structure.
 * This class should not be instantiated, as it serves as a placeholder.
 */
public class CommonCliOptions {
    public static void main(String[] args) {
        throw new UnsupportedOperationException("This is a dummy class.");
    }
}

/**
 * Common options shared among all command-line interfaces in the application.
 * This includes verbosity level and p-value thresholds for statistical analysis.
 */
class CommonToAll {

    @Option(names = {"-v", "-verbosity"},
            description = "Verbose logging",
            defaultValue = "true")
    boolean[] verbose;

    @Option(
            names = {"--pval"}, paramLabel = "[0.0-1.0 ? 0.05]",
            description = """
    P-value threshold for counting significant DEGs in the continuity table. Used for filtering DEGs before generating plots. Default = ${DEFAULT-VALUE}
    """,
            defaultValue="0.01")
    double pval;

    /**
     * Sets the logging scope based on the verbosity option provided.
     * Adjusts the log level according to the verbosity array.
     */
    public void setLoggingScope() {
        if (verbose.length > 1) {
            LogManager.getLogger().atLevel(Level.WARN).log(verbose);
        } else if (verbose.length > 0) {
            LogManager.getLogger().atLevel(Level.INFO).log(verbose);
        } else {
            LogManager.getLogger().atLevel(Level.ERROR).log(verbose);
        }
    }
}

/**
 * Class for handling common file input parameters for the application.
 * This includes reading input files related to DEGs, pathways, and pathway genes.
 */
class CommonFileParams {
    private final FileParseUtils fileParseUtils = new FileParseUtils();

    @Parameters(
            index = "0",
            paramLabel = "<inputDEGS.csv|tsv>",
            description = "Input DEGs file in CSV or TSV format, columns: gene symbol, log-fold change, and adjusted p-value."
    )
    private File inputFileDegs;

    @Parameters(index = "1",
            paramLabel = "<inputPathwayDescriptions.csv|tsv>",
            description = "Input pathway descriptions file, columns: pathway ID and description of pathway.")
    private File inputFilePathwayDescriptions;

    @Parameters(index = "2",
            paramLabel = "<inputPathwayGenes.csv|tsv>",
            description = "Input pathway + genes file, columns: pathway ID, Entrez gene ID, gene symbol, and Ensembl gene ID.")
    private File inputFilePathwayGenes;

    /**
     * Parses and retrieves a list of differentially expressed genes (DEGs).
     *
     * @return List of DEGs.
     * @throws RuntimeException if an error occurs while parsing the input file.
     */
    public List<Deg> getDegs() {
        try {
            return fileParseUtils.parseDegsFile(inputFileDegs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses and retrieves a list of pathways from the input file.
     *
     * @return List of pathways.
     * @throws RuntimeException if an error occurs while parsing the input file.
     */
    public List<Pathway> getPathways() {
        try {
            return fileParseUtils.parsePathwayFile(inputFilePathwayDescriptions);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses and retrieves a list of pathway genes from the input file.
     *
     * @return List of pathway genes.
     * @throws RuntimeException if an error occurs while parsing the input file.
     */
    public List<PathwayGene> getPathwayGenes() {
        try {
            return fileParseUtils.parsePathwayGeneFile(inputFilePathwayGenes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

/**
 * Contains common chart parameters and options for the CLI.
 * This includes titles, image format, color schemes, and output path.
 */
class CommonChartParams {
    final Logger logger = LogManager.getLogger();
    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @Option(names = {"--title", "-t", "-T"},
            description = "Title of the chart")
    String title;

    @Option(names = {"--x-axis-label", "-x-lab"},
            description = "X-axis title of the chart")
    String xAxisTitle;

    @Option(names = {"--y-axis-label", "-y-lab"},
            description = "Y-axis title of the chart")
    String yAxisTitle;

    @Option(names = {"--image-format", "-if"},
            paramLabel = "png|jpg",
            description = "Image format of the output image, default = '${DEFAULT-VALUE}'",
            defaultValue = "png")
    String imageFormat;
    @Option(names = {"--max-n-pathways", "-p-max"}, paramLabel = "1-inf", description = "Max number of pathways to include in chart. '--pathway-ids' overrides this option. Default = ${DEFAULT-VALUE}", defaultValue = "20")
    int maxNPathways;

    @Option(names = {"--color-manual", "-cm"},
            arity = "1..*",
            split = ";",
            paramLabel = "red|0xRRGGBB",
            description = """
    One or more colors to apply to chart. Cycles trough if too few colors were given. Default colors apply if none are given. Options:
    red,green,blue,for more see: https://docs.oracle.com/javase/6/docs/java/awt/Color.html, 000000-FFFFFF, #000000-#FFFFFF, 0x000000-0xFFFFFF""")
    private String[] colorManual;
    @Parameters(index = "3+",
                paramLabel = "<outputPathImage.*>",
                description = "Output path of generated chart")
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
