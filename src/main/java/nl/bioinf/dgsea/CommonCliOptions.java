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

public class CommonCliOptions {
    public static void main(String[] args) {
        throw new UnsupportedOperationException("This is a dummy class.");
    }
}

class CommonToAll {
    @Option(names = {"-v", "-verbosity"}, description = "Verbose logging", defaultValue = "true")
    boolean[] verbose;

    @Option(names = {"--pval"}, paramLabel = "[0.0-1.0 ? 0.05]", description = "P-value threshold. For counting significant degs in con_table. For filtering degs before making a plot in enrich_bar_chart, enrich_dot_chart and perc_lfc_per_pathway_chart, default = ${DEFAULT-VALUE}", defaultValue="0.05")
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
    @Option(names = {"--title"}, description = "Title of chart, default = 'png'", defaultValue = "png")
    String title;
    @Option(names = {"--x-axis"}, description = "X-axis title of chart, default = 'png'", defaultValue = "png")
    String xAxisTitle;
    @Option(names = {"--y-axis"}, description = "Y-axis title of chart, default = 'png'", defaultValue = "png")
    String yAxisTitle;
    @Option(names = {"--image-format"}, paramLabel = "[png|jpg ? png]", description = "Image format of output image, default = 'png'", defaultValue = "png")
    String imageFormat;

    @Option(
            names = {"--color-scheme"},
            paramLabel = "[viridis|plasma|inferno|magma|cividis|grays|purples|blues|greens|oranges|reds]",
            description = "Color scheme to apply to chart, default = '${DEFAULT-VALUE}'. 'color-manual' overrides this option.",
            defaultValue="viridis",
            completionCandidates = CommonChartParams.ColorSchemeCandidates.class) // Only a single implementation, because this doesn't work in all kinds of terminal-emulators.
    String colorScheme;

    @Option(names = {"--color-manual"}, arity = "0..*", paramLabel = "[red|green|blue|purple|orange|gray|black|pink|yellow|magenta|cyan|brown 1...]", description = "One or more colors to apply to chart. Overrides '--color-scheme'. Cycles trough if too few colors were given.")
    String[] colorManual;
    @Parameters(index = "3+",paramLabel = "<outputPathImage.*>", description = "Output path of generated chart")
    File outputPath;

    static class ColorSchemeCandidates implements Iterable<String> {
        @Override
        public java.util.Iterator<String> iterator() {
            return Arrays.asList("viridis","plasma","inferno","magma","cividis","grays","purples","blues","greens","oranges","reds").iterator();
        }
    }
}
