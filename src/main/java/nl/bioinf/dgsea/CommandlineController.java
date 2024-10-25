/**
 * Manages the commandline interface of this application, using Picocli.
 *  This app is split in 4 custom subcommands starting from main.
 *  Class from CommonCliOptions.java is used for inheritance of multiple options common in 2 or more sub-commands.
 * @Authors Jort Gommers & Willem Daniël Visser
 */

package nl.bioinf.dgsea;

import nl.bioinf.dgsea.data_processing.*;
import nl.bioinf.dgsea.table_outputs.TwoByTwoContingencyTable;
import nl.bioinf.dgsea.visualisations.PercLfcBarChart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Mixin;

import java.io.File;
import java.io.IOException;
import java.util.List;


/**
 * Primary command |
 * Calls sub-command, if not sub-command is given, it will throw this issue and provide global help.
 */
@Command(name="main", version="main 1.0", mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class, EnrichBarChart.class, EnrichDotChart.class, PercLogFChangePerPathwayCmd.class, ContinuityTable.class})
public class CommandlineController implements Runnable {
    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @Override
    public void run() {
        throw new CommandLine.ParameterException(spec.commandLine(), "Missing required subcommand");
    }
}
/**
 * First-layer (CLI-) sub-command |
 * Calculates enrichment scores for each pathway, generates and saves enrichment dot chart to file.
 */
@Command(name = "enrich_bar_chart", version = "Enrichment bar-chart 1.0", mixinStandardHelpOptions = true, description = "Generates and saves an enrichment bar chart showing top enriched pathways.")
class EnrichBarChart implements Runnable {
    @Mixin
    private CommonToAll commonToAll;
    @Mixin
    private CommonFileParams commonFileParams;
    @Mixin
    private CommonChartParams commonChartParams;

    @Option(names = {"--output-file"}, paramLabel = "FILE", description = "Output file path for the bar chart (e.g., ./output/enrichment_bar_chart.png)")
    private String outputFilePath;

    @Option(names = {"--max-n-pathways"}, paramLabel = "[1-inf]", description = "Max number of pathways to include in chart. '--pathway-ids' overrides this option.", defaultValue = "20")
    private int maxNPathways;

    @Override
    public void run() {
        commonToAll.setLoggingScope();

        List<Deg> degs = commonFileParams.getDegs();
        List<Pathway> pathways = commonFileParams.getPathways();
        List<PathwayGene> pathwayGenes = commonFileParams.getPathwayGenes();

        Color[] colorArray = commonChartParams.getColorManualAsColors();

        EnrichmentAnalysisService enrichmentService = new EnrichmentAnalysisService();
        try {
            enrichmentService.generateEnrichmentChart(
                    degs,
                    pathways,
                    pathwayGenes,
                    maxNPathways,
                    outputFilePath,
                    commonChartParams.title,
                    colorArray,
                    commonChartParams.colorScheme,
                    EnrichmentAnalysisService.ChartType.BAR_CHART, // Bar chart
                    null,  // dotSize niet nodig
                    null   // dotTransparency niet nodig
            );
        } catch (IOException e) {
            System.err.println("Error reading input data or saving PNG: " + e.getMessage());
        }
    }
}




/**
 * First-layer (CLI-) sub-command |
 * Calculates enrichment scores for each pathway, generates and saves enrichment dot chart to file.
 */

@Command(name = "enrich_dot_chart", version = "Enrichment dot-chart 1.0", mixinStandardHelpOptions = true, description = "This command generates an enrichment dot chart using given data.")
class EnrichDotChart implements Runnable {
    @Mixin
    private CommonToAll commonToAll;
    @Mixin
    private CommonFileParams commonFileParams;
    @Mixin
    private CommonChartParams commonChartParams;

    @Option(names = {"--dot-size"}, paramLabel = "[0.0-inf]", description = "Dot size, default = ${DEFAULT-VALUE}", defaultValue = "30.0")
    private double dotSize;

    @Option(names = {"--dot-transparency"}, paramLabel = "[0.0-1.0]", description = "Dot transparency, default = ${DEFAULT-VALUE}", defaultValue = "1.0")
    private float dotTransparency;

    @Option(names = {"--max-n-pathways"}, paramLabel = "[1-inf]", description = "Max number of pathways to include in chart. '--pathway-ids' overrides this option.", defaultValue = "20")
    private int maxNPathways;

    @Option(names = {"--output-file"}, paramLabel = "FILE", description = "Output file path for the dot plot (e.g., ./output/enrichment_dot_plot.png)")
    private String outputFilePath;

    @Override
    public void run() {
        commonToAll.setLoggingScope();

        List<Deg> degs = commonFileParams.getDegs();
        List<Pathway> pathways = commonFileParams.getPathways();
        List<PathwayGene> pathwayGenes = commonFileParams.getPathwayGenes();

        Color[] colorArray = commonChartParams.getColorManualAsColors();

        EnrichmentAnalysisService enrichmentService = new EnrichmentAnalysisService();
        try {
            enrichmentService.generateEnrichmentChart(
                    degs,
                    pathways,
                    pathwayGenes,
                    maxNPathways,
                    outputFilePath,
                    commonChartParams.title,
                    colorArray,
                    EnrichmentAnalysisService.ChartType.DOT_CHART, // Dot chart
                    dotSize,
                    dotTransparency
            );
        } catch (IOException e) {
            System.err.println("Error reading input data or saving PNG: " + e.getMessage());
        }
    }
}


/**
 * First-layer (CLI-) sub-command |
 * Makes a bar-chart showing ratio's average log-fold-change on differently expressed genes per pathway.
 */
@Command(name = "perc_lfc_per_pathway_chart", version = "percLogFChangePerPathway 1.0", mixinStandardHelpOptions = true, description = "Makes a bar-chart showing ratio's average log-fold-change on differently expressed genes per pathway.")
class PercLogFChangePerPathwayCmd implements Runnable {
    @Mixin
    private CommonToAll commonToAll;
    @Mixin
    private CommonFileParams commonFileParams;
    @Mixin
    private CommonChartParams commonChartParams;

    @Option(names = {"--pathway-ids"}, paramLabel = "hsa(...)", arity = "0..*", split = ",", description = "Pathway ids of interest")
    private String[] pathwayIds;
    @Option(names = {"--max-n-pathways"}, paramLabel = "[1-inf]", description = "Max number of pathways to include in chart. '--pathway-ids' overrides this option.")
    private int maxNPathways;

    @Override
    public void run() {
        commonToAll.setLoggingScope();
        List<Deg> degs = commonFileParams.getDegs();
        List<Pathway> pathways = commonFileParams.getPathways();
        List<PathwayGene> pathwayGenes = commonFileParams.getPathwayGenes();
        PercLfcBarChart percLfcBarChart = new PercLfcBarChart(getChartGeneratorsBuilder(degs, pathways, pathwayGenes));
        percLfcBarChart.saveChart();
    }
    private PercLfcBarChart.Builder getChartGeneratorsBuilder(List<Deg> degs, List<Pathway> pathways, List<PathwayGene> pathwayGenes) {
        return new PercLfcBarChart.Builder(
                commonChartParams.title,
                commonChartParams.xAxisTitle,
                commonChartParams.yAxisTitle,
                degs,
                pathways,
                pathwayGenes,
                commonChartParams.outputPath)
                .colorManual(commonChartParams.getColorManualAsColors())
                .maxNPathways(commonChartParams.maxNPathways)
                .imageFormat(commonChartParams.imageFormat)
                .pathwayIds(pathwayIds);
    }
}

/**
 * First-layer (CLI-) sub-command |
 * Prints or stores to text file a continuity table of count data on 2 aspects of degs for every pathway: presence in pathway and presence of significance
 */
@Command(name = "con_table", version = "Continuity table 1.0", mixinStandardHelpOptions = true,
        description = "Prints or stores to text file a continuity table of count data on 2 aspects of DEGs for every pathway: presence in pathway and presence of significance")
class ContinuityTable implements Runnable {
    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;
    Logger logger = LogManager.getLogger(ContinuityTable.class);
    @Mixin
    private CommonToAll commonToAll;

    @Mixin
    private CommonFileParams commonFileParams;

    @Option(names = {"--output"}, paramLabel = "[file|print]", description = "Option on how to return output table. (csv-file or print to terminal)", defaultValue = "file")
    private String output;

    @Option(names = {"--outputFilePath"}, description = "File to write table text to.")
    private File outputFilePath;

    @Override
    public void run() {
        commonToAll.setLoggingScope();
        TwoByTwoContingencyTable twoByTwoContingencyTable = new TwoByTwoContingencyTable(
                commonFileParams.getDegs(),
                commonFileParams.getPathways(),
                commonFileParams.getPathwayGenes(),
                commonToAll.pval
        );
        try {
            String outputTable = twoByTwoContingencyTable.getTable();
            handleOutput(outputTable);
        } catch (NullPointerException _) {
            logger.error("Make sure that at least one pathway-id in your pathway-descriptions file matches a pathway-id in your pathway-gene entries file.");
        }

    }

    private void handleOutput(String outputTable) {
        if ("file".equalsIgnoreCase(output)) {
            if (outputFilePath != null) {
                try {
                    java.nio.file.Files.write(outputFilePath.toPath(), outputTable.getBytes());
                    logger.info("Continuity table written to: {}", outputFilePath.getPath());
                } catch (IOException e) {
                    logger.error("Error writing continuity table to file: {}", e.getMessage());
                }
            } else {
                logger.error("No output file path provided. Use '--outputFilePath' to specify the file.");
            }
        } else if ("print".equalsIgnoreCase(output)) {
            System.out.println(outputTable);
        } else {
            throw new CommandLine.ParameterException(spec.commandLine(), "Invalid output option. Use '--output [file|print]'.");
        }
    }
}
