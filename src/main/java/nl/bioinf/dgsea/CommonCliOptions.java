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
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.Arrays;
import java.util.List;

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

    @Option(names = {"--pval"}, paramLabel = "[0.0-1.0 ? 0.05]",
            description = "P-value threshold for counting significant DEGs in the continuity table. Used for filtering DEGs before generating plots. Default = ${DEFAULT-VALUE}",
            defaultValue = "0.01")
    double pval;

    /**
     * Sets the logging scope based on the verbosity option provided.
     * Adjusts the log level according to the verbosity array.
     */
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

    @Option(names = {"--title"},
            description = "Title of the chart, default = 'png'",
            defaultValue = "png")
    String title;

    @Option(names = {"--x-axis"},
            description = "X-axis title of the chart, default = 'png'",
            defaultValue = "png")
    String xAxisTitle;

    @Option(names = {"--y-axis"},
            description = "Y-axis title of the chart, default = 'png'",
            defaultValue = "png")
    String yAxisTitle;

    @Option(names = {"--image-format"},
            paramLabel = "[png|jpg ? png]",
            description = "Image format of the output image, default = 'png'",
            defaultValue = "png")
    String imageFormat;

    @Option(names = {"--color-scheme"},
            paramLabel = "[viridis|plasma|inferno|magma|cividis|grays|purples|blues|greens|oranges|reds]",
            description = "Color scheme to apply to the chart, default = '${DEFAULT-VALUE}'. 'color-manual' overrides this option.",
            defaultValue = "viridis",
            completionCandidates = CommonChartParams.ColorSchemeCandidates.class)
    String colorScheme;

    @Option(names = {"--color-manual"},
            arity = "0..*",
            paramLabel = "[red|green|blue|purple|orange|gray|black|pink|yellow|magenta|cyan|brown 1...]",
            description = "One or more colors to apply to the chart. Overrides '--color-scheme'. Cycles through if too few colors were given.")
    String[] colorManual;

    @Parameters(index = "3+",
            paramLabel = "<outputPathImage.*>",
            description = "Output path of the generated chart")
    File outputPath;

    /**
     * Provides candidates for color schemes available in the chart options.
     */
    static class ColorSchemeCandidates implements Iterable<String> {
        @Override
        public java.util.Iterator<String> iterator() {
            return Arrays.asList("viridis", "plasma", "inferno", "magma", "cividis", "grays", "purples", "blues", "greens", "oranges", "reds").iterator();
        }
    }
}
