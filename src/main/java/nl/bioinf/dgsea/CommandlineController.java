package nl.bioinf.degs;


import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;
import picocli.CommandLine.Mixin;

import java.io.File;
import java.util.Arrays;

@Command
class CommonToAll {
    @Option(names = {"-v", "-verbosity"}, description = "Verbose logging")
    boolean[] verbose;

    @Option(names = {"--pval"}, paramLabel = "[0.0-1.0 ? 0.05]", description = "P-value threshold, default = ${DEFAULT-VALUE}", defaultValue="0.05")
    double pval;
}

@Command
class CommonFileParams {
    @Parameters(
            index = "0", paramLabel = "<inputDEGS.csv|tsv>",
            description = "Input degs file in csv or tsv format, columns: gene-symbol, log-fold change and adjusted p-value."
    )
    File inputFileDegs;

    @Parameters(
            index = "1", paramLabel = "<inputPathwayDescriptions.csv|tsv>", description = "Input pathway descriptions file, columns: pathway-id and description of pathway."
    )
    File inputFilePathwayDescriptions;

    @Parameters(index = "2", paramLabel = "<inputPathwayGenes.csv|tsv>", description = "Input pathway + genes file, columns: pathway-id, entrez gene-id, gene-symbol and ensembl gene-id")
    File inputFilePathwayGenes;
}

@Command
class CommonChartParams {
    @Option(names = {"--image-format"}, paramLabel = "[png|jpg ? png]", description = "Image format of output image, default = 'png'", defaultValue = "png")
    String imageFormat;

    @Option(names = {"--image-dpi"}, paramLabel = "[0.0-inf ? 1.0]", description = "Dpi of output image, default = 1.0", defaultValue="1.0")
    double imageDpi;

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

@Command(name="main", version="main 1.0", mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class, EnrichBarChart.class, EnrichDotChart.class, CumulativeExprVarChart.class, ContinuityTable.class})
public class CommandlineController implements Runnable {
    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @Override
    public void run() {
        throw new CommandLine.ParameterException(spec.commandLine(), "Missing required subcommand");
    }
}

@Command(name = "enrich_bar_chart", version = "Enrichment bar-chart 1.0", mixinStandardHelpOptions = true)
class EnrichBarChart implements Runnable {
    @Mixin
    CommonToAll commonToAll = new CommonToAll();
    @Mixin
    CommonFileParams commonFileParams = new CommonFileParams();
    @Mixin
    CommonChartParams commonChartParams = new CommonChartParams();


    @Override
    public void run() {
        System.out.println("commonToAll.pval = " + commonToAll.pval);
        System.out.println("commonToAll.verbose = " + Arrays.toString(commonToAll.verbose));
    }
}

@Command(name = "enrich_dot_chart", version = "Enrichment dot-chart 1.0", mixinStandardHelpOptions = true)
class EnrichDotChart implements Runnable {
    @Mixin
    CommonToAll commonToAll = new CommonToAll();
    @Mixin
    CommonFileParams commonFileParams = new CommonFileParams();
    @Mixin
    CommonChartParams commonChartParams = new CommonChartParams();

    @Option(names = {"--dot-size"}, paramLabel = "[0.0-inf]", description = "Dot size, default = ${DEFAULT-VALUE}", defaultValue = "1.0")
    private double dotSize;

    @Option(names = {"--dot-transparency"}, paramLabel = "[0.0-1.0]", description = "Dot transparency, default = ${DEFAULT-VALUE}", defaultValue = "1.0")
    private double dotTransparency;

    @Override
    public void run() {
        System.out.println("dotSize = " + dotSize);
        System.out.println("dotTransparency = " + dotTransparency);
    }
}

@Command(name = "cumm_expr_var_chart", version = "Cumulative expression variation chart 1.0", mixinStandardHelpOptions = true)
class CumulativeExprVarChart implements Runnable {
    @Mixin
    CommonToAll commonToAll = new CommonToAll();
    @Mixin
    CommonFileParams commonFileParams = new CommonFileParams();
    @Mixin
    CommonChartParams commonChartParams = new CommonChartParams();

    @Option(names = {"--pathway-ids"}, paramLabel = "hsa(...)", arity = "0..*", split = ",", description = "Pathway ids of interest")
    private String[] pathwayIds;

    @Option(names = {"--max-n-pathways"}, paramLabel = "[1-inf]", description = "Max number of pathways to include in chart. '--pathway-ids' overrides this option.")
    private int maxNPathways;

    @Override
    public void run() {

    }
}

@Command(name = "con_table", version = "Continuity table 1.0", mixinStandardHelpOptions = true)
class ContinuityTable implements Runnable {
    @Mixin
    CommonToAll commonToAll = new CommonToAll();
    @Mixin
    CommonFileParams commonFileParams = new CommonFileParams();

    @Option(names = {"--output"}, paramLabel = "[csv|print]", description = "Option on how to return output table. (csv-file or print to terminal)")
    private String output;

    @Option(names = {"--outputFilePath"}, description = "File to write table text to.")
    private File outputFilePath;

    @Override
    public void run() {

    }
}



