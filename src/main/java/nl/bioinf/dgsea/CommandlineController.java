package nl.bioinf.dgsea;


import nl.bioinf.dgsea.data_processing.*;
import nl.bioinf.dgsea.table_outputs.Table;
import nl.bioinf.dgsea.visualisations.ChartGenerator;
import nl.bioinf.dgsea.table_outputs.EnrichmentTable;
import nl.bioinf.dgsea.visualisations.EnrichmentBarChart;
import nl.bioinf.dgsea.visualisations.EnrichmentDotPlot;
import org.apache.logging.log4j.Level;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;
import picocli.CommandLine.Mixin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Command(name="main", version="main 1.0", mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class, EnrichBarChart.class, EnrichDotChart.class, PercLogFChangePerPathwayCmd.class, ContinuityTable.class})
public class CommandlineController implements Runnable {
    private final Logger logger = LogManager.getLogger(CommandlineController.class.getName());
    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @Override
    public void run() {
        throw new CommandLine.ParameterException(spec.commandLine(), "Missing required subcommand");
    }
}

@Command(name = "enrich_bar_chart", version = "Enrichment bar-chart 1.0", mixinStandardHelpOptions = true, description = "No description yet")
class EnrichBarChart implements Runnable {
    private final Logger logger = LogManager.getLogger(EnrichBarChart.class.getName());
    @Mixin
    CommonToAll commonToAll = new CommonToAll();
    @Mixin
    CommonFileParams commonFileParams = new CommonFileParams();
    @Mixin
    CommonChartParams commonChartParams = new CommonChartParams();


    @Override
    public void run() {
        commonToAll.setLoggingScope();
        System.out.println("commonToAll.pval = " + commonToAll.pval);
        System.out.println("commonToAll.verbose = " + Arrays.toString(commonToAll.verbose));
    }
}

@Command(name = "enrich_dot_chart", version = "Enrichment dot-chart 1.0", mixinStandardHelpOptions = true, description = "This command over-aches commands for now: con_table, enrich_bar_chart and enrich_dot_chart")
class EnrichDotChart implements Runnable {
    private final Logger logger = LogManager.getLogger(EnrichDotChart.class.getName());
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
        commonToAll.setLoggingScope();
        try {
            // Pad naar je input CSV-bestanden
            String degFilePath = "test_data/degs.csv";          // Change this to the actual path
            String pathwayFilePath = "test_data/hsa_pathways.csv";  // Change this to the actual path
            String pathwayGeneFilePath = "test_data/pathways.csv";  // Change this to the actual path

            // Parse de bestanden
            FileParseUtils fileParseUtils = new FileParseUtils();
            List<Deg> degs = fileParseUtils.parseDegsFile(new File(degFilePath));
            List<Pathway> pathways = fileParseUtils.parsePathwayFile(new File(pathwayFilePath));
            List<PathwayGene> pathwayGenes = fileParseUtils.parsePathwayGeneFile(new File(pathwayGeneFilePath));

            // Initialiseer de Table klasse met geparseerde data
            Table.degs = degs;           // Set the DEGs
            Table.pathways = pathways;   // Set the pathways
            Table.pathwayGenes = pathwayGenes; // Set the pathway genes

            // Maak de tabel en print de two-by-two contingency table
            Table table = new Table() {
            };
            String output = table.getTwoByTwoContingencyTable();
            System.out.println(output);

            // Bereken de enrichment en haal de resultaten op
            EnrichmentTable enrichmentTable = new EnrichmentTable(pathways, degs, pathwayGenes);
            enrichmentTable.calculateEnrichment();
            List<EnrichmentResult> results = enrichmentTable.getEnrichmentResults();

            // Selecteer de pathways met een significante adjusted p-value
            List<EnrichmentResult> significantResults = results.stream()
                    .filter(result -> !Double.isNaN(result.adjustedPValue()) && result.adjustedPValue() < 0.05) // Filter uit op NaN waarden en significante p-waarden
                    .collect(Collectors.toList());

            // Sorteer op enrichment score (hoogste eerst) en neem de top 20
            List<EnrichmentResult> top20Results = significantResults.stream()
                    .sorted(Comparator.comparingDouble(EnrichmentResult::enrichmentScore).reversed()) // Sorteer op enrichment score
                    .limit(20) // Neem de top 20
                    .collect(Collectors.toList());

            // Maak de bar chart en sla op als PNG
            String barChartOutputFilePath = "pathway_enrichment_chart.png";  // Pad voor het opslaan van de PNG
            EnrichmentBarChart barChart = new EnrichmentBarChart("Top 20 Pathway Enrichment met significante padjust value", top20Results, pathways, barChartOutputFilePath);
            System.out.println("Bar chart opgeslagen als PNG op: " + barChartOutputFilePath);

            // Maak de dot plot en sla op als PNG
            String dotPlotOutputFilePath = "pathway_enrichment_dot_plot.png";  // Pad voor het opslaan van de PNG
            EnrichmentDotPlot dotPlot = new EnrichmentDotPlot("Pathway Enrichment Dot Plot", top20Results, pathways, dotPlotOutputFilePath);
            System.out.println("Dot plot opgeslagen als PNG op: " + dotPlotOutputFilePath);

        } catch (IOException e) {
            System.err.println("Fout bij het opslaan van de PNG: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

@Command(name = "perc_lfc_per_pathway_chart", version = "percLogFChangePerPathway 1.0", mixinStandardHelpOptions = true, description = "Makes a bar-chart showing ratio's average log-fold-change on differently expressed genes per pathway.")
class PercLogFChangePerPathwayCmd implements Runnable {
    private final Logger logger = LogManager.getLogger(PercLogFChangePerPathwayCmd.class.getName());

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
        commonToAll.setLoggingScope();
        System.out.println("SHOUT");
        ChartGenerator.Builder chartGeneratorsBuilder;

        chartGeneratorsBuilder = getChartGeneratorsBuilder();
        if (commonChartParams.colorScheme != null) chartGeneratorsBuilder.colorScheme(commonChartParams.colorScheme);
        if (commonChartParams.colorManual != null) chartGeneratorsBuilder.colorManual(commonChartParams.colorManual);
        if (commonChartParams.imageDpi != 1.0) chartGeneratorsBuilder.dpi(commonChartParams.imageDpi);
        if (maxNPathways > 0) chartGeneratorsBuilder.maxNPathways(maxNPathways);
        if (!commonChartParams.imageFormat.isEmpty()) chartGeneratorsBuilder.imageFormat(commonChartParams.imageFormat);

        ChartGenerator chartGenerator = new ChartGenerator(chartGeneratorsBuilder);
        chartGenerator.saveChartPercLogFChangePerPathway();




    }

    private ChartGenerator.Builder getChartGeneratorsBuilder() {
        return new ChartGenerator.Builder(
                commonChartParams.title,
                commonChartParams.xAxisTitle,
                commonChartParams.yAxisTitle,
                commonFileParams.getPathways(),
                commonFileParams.getPathwayGenes(),
                commonFileParams.getDegs(),
                commonChartParams.outputPath
        );
    }
}

@Command(name = "con_table", version = "Continuity table 1.0", mixinStandardHelpOptions = true, description = "No description yet")
class ContinuityTable implements Runnable {
    private final Logger logger = LogManager.getLogger(ContinuityTable.class.getName());
    @Mixin
    CommonToAll commonToAll = new CommonToAll();
    @Mixin
    CommonFileParams commonFileParams = new CommonFileParams();

    @Option(names = {"--output"}, paramLabel = "[csv|print]", description = "Option on how to return output table. (csv-file or print to terminal)", defaultValue = "csv")
    private String output;

    @Option(names = {"--outputFilePath"}, description = "File to write table text to.")
    private File outputFilePath;

    @Override
    public void run() {}
}

@Command
class CommonToAll {
    Logger rootLogger = LogManager.getLogger(CommonToAll.class.getName());
    @Option(names = {"-v", "-verbosity"}, description = "Verbose logging", defaultValue = "true")
    boolean[] verbose;

    @Option(names = {"--pval"}, paramLabel = "[0.0-1.0 ? 0.05]", description = "P-value threshold, default = ${DEFAULT-VALUE}", defaultValue="0.05")
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

@Command
class CommonFileParams {
    private final FileParseUtils fileParseUtils = new FileParseUtils();

    @Parameters(
            index = "0", paramLabel = "<inputDEGS.csv|tsv>",
            description = "Input degs file in csv or tsv format, columns: gene-symbol, log-fold change and adjusted p-value."
    )
    private File inputFileDegs;

    @Parameters(
            index = "1", paramLabel = "<inputPathwayDescriptions.csv|tsv>", description = "Input pathway descriptions file, columns: pathway-id and description of pathway."
    )
    private File inputFilePathwayDescriptions;

    @Parameters(index = "2", paramLabel = "<inputPathwayGenes.csv|tsv>", description = "Input pathway + genes file, columns: pathway-id, entrez gene-id, gene-symbol and ensembl gene-id")
    private File inputFilePathwayGenes;

    public List<Deg> getDegs() {
        try {
            return fileParseUtils.parseDegsFile(inputFileDegs);
        }catch (Exception e) {
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

@Command
class CommonChartParams {
    @Option(names = {"--title"}, description = "Title of chart, default = 'png'", defaultValue = "png")
    String title;
    @Option(names = {"--x-axis"}, description = "X-axis title of chart, default = 'png'", defaultValue = "png")
    String xAxisTitle;
    @Option(names = {"--y-axis"}, description = "Y-axis title of chart, default = 'png'", defaultValue = "png")
    String yAxisTitle;
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