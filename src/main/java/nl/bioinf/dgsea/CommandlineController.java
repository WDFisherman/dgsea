/**
 * Manages the command-line interface of this application using Picocli.
 * This app is split into 4 custom subcommands starting from main.
 * The class from CommonCliOptions.java is used for inheritance of multiple options common in 2 or more sub-commands.
 *
 * @authors Jort Gommers & Willem Daniël Visser
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
 * Primary command for the CLI application.
 * Calls the sub-command. If no sub-command is given, it throws an error and provides global help.
 */
@Command(name="main", version="main 1.0", mixinStandardHelpOptions = true,
        subcommands = {CommandLine.HelpCommand.class, EnrichBarChart.class, EnrichDotChart.class,
                PercLogFChangePerPathwayCmd.class, ContinuityTable.class})
public class CommandlineController implements Runnable {

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    /**
     * Executes the command, throwing an exception if no sub-command is provided.
     */
    @Override
    public void run() {
        throw new CommandLine.ParameterException(spec.commandLine(), "Missing required subcommand");
    }
}

/**
 * First-layer (CLI) sub-command for generating an enrichment bar chart.
 * Calculates enrichment scores for each pathway and saves the chart to a file.
 */
@Command(name = "enrich_bar_chart", version = "Enrichment bar-chart 1.0", mixinStandardHelpOptions = true,
        description = "Generates and saves an enrichment bar chart showing top enriched pathways.")
class EnrichBarChart implements Runnable {

    @Mixin
    private CommonToAll commonToAll;

    @Mixin
    private CommonFileParams commonFileParams;

    @Mixin
    private CommonChartParams commonChartParams;

    @Option(names = {"--output-file"}, paramLabel = "FILE",
            description = "Output file path for the bar chart (e.g., ./output/enrichment_bar_chart.png)")
    private String outputFilePath;

    @Option(names = {"--max-n-pathways"}, paramLabel = "[1-inf]",
            description = "Max number of pathways to include in chart. '--pathway-ids' overrides this option.",
            defaultValue = "20")
    private int maxNPathways;

    /**
     * Executes the command to generate and save an enrichment bar chart.
     * Retrieves data from common file parameters and generates the chart using the enrichment service.
     */
    @Override
    public void run() {
        commonToAll.setLoggingScope();

        List<Deg> degs = commonFileParams.getDegs();
        List<Pathway> pathways = commonFileParams.getPathways();
        List<PathwayGene> pathwayGenes = commonFileParams.getPathwayGenes();

        String[] colorArray = commonChartParams.colorManual != null && commonChartParams.colorManual.length != 0 ?
                commonChartParams.colorManual : null;

        EnrichmentAnalysisService enrichmentService = new EnrichmentAnalysisService();
        try {
            enrichmentService.generateEnrichmentChart(
                    degs,
                    pathways,
                    pathwayGenes,
                    maxNPathways,
                    outputFilePath,
                    colorArray,
                    commonChartParams.colorScheme,
                    EnrichmentAnalysisService.ChartType.BAR_CHART, // Bar chart
                    null,  // dotSize not needed
                    null   // dotTransparency not needed
            );
        } catch (IOException e) {
            System.err.println("Error reading input data or saving PNG: " + e.getMessage());
        }
    }
}

/**
 * First-layer (CLI) sub-command for generating an enrichment dot chart.
 * Calculates enrichment scores for each pathway and saves the chart to a file.
 */
@Command(name = "enrich_dot_chart", version = "Enrichment dot-chart 1.0", mixinStandardHelpOptions = true,
        description = "This command generates an enrichment dot chart using given data.")
class EnrichDotChart implements Runnable {

    @Mixin
    private CommonToAll commonToAll;

    @Mixin
    private CommonFileParams commonFileParams;

    @Mixin
    private CommonChartParams commonChartParams;

    @Option(names = {"--dot-size"}, paramLabel = "[0.0-inf]",
            description = "Dot size, default = ${DEFAULT-VALUE}", defaultValue = "30.0")
    private double dotSize;

    @Option(names = {"--dot-transparency"}, paramLabel = "[0.0-1.0]",
            description = "Dot transparency, default = ${DEFAULT-VALUE}", defaultValue = "1.0")
    private float dotTransparency;

    @Option(names = {"--max-n-pathways"}, paramLabel = "[1-inf]",
            description = "Max number of pathways to include in chart. '--pathway-ids' overrides this option.",
            defaultValue = "20")
    private int maxNPathways;

    @Option(names = {"--output-file"}, paramLabel = "FILE",
            description = "Output file path for the dot plot (e.g., ./output/enrichment_dot_plot.png)")
    private String outputFilePath;

    /**
     * Executes the command to generate and save an enrichment dot chart.
     * Retrieves data from common file parameters and generates the chart using the enrichment service.
     */
    @Override
    public void run() {
        commonToAll.setLoggingScope();

        List<Deg> degs = commonFileParams.getDegs();
        List<Pathway> pathways = commonFileParams.getPathways();
        List<PathwayGene> pathwayGenes = commonFileParams.getPathwayGenes();

        String[] colorArray = commonChartParams.colorManual;

        EnrichmentAnalysisService enrichmentService = new EnrichmentAnalysisService();
        try {
            enrichmentService.generateEnrichmentChart(
                    degs,
                    pathways,
                    pathwayGenes,
                    maxNPathways,
                    outputFilePath,
                    colorArray,
                    commonChartParams.colorScheme,
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
 * First-layer (CLI) sub-command for generating a bar chart of average log-fold change ratios per pathway.
 */
@Command(name = "perc_lfc_per_pathway_chart", version = "percLogFChangePerPathway 1.0",
        mixinStandardHelpOptions = true,
        description = "Makes a bar-chart showing ratio's average log-fold-change on differently expressed genes per pathway.")
class PercLogFChangePerPathwayCmd implements Runnable {

    @Mixin
    private CommonToAll commonToAll;

    @Mixin
    private CommonFileParams commonFileParams;

    @Mixin
    private CommonChartParams commonChartParams;

    @Option(names = {"--pathway-ids"}, paramLabel = "hsa(...)", arity = "0..*", split = ",",
            description = "Pathway ids of interest")
    private String[] pathwayIds;

    @Option(names = {"--max-n-pathways"}, paramLabel = "[1-inf]",
            description = "Max number of pathways to include in chart. '--pathway-ids' overrides this option.")
    private int maxNPathways;

    /**
     * Executes the command to generate and save a bar chart of average log-fold changes.
     * Retrieves data from common file parameters and creates the chart using the PercLfcBarChart class.
     */
    @Override
    public void run() {
        commonToAll.setLoggingScope();
        List<Deg> degs = commonFileParams.getDegs();
        List<Pathway> pathways = commonFileParams.getPathways();
        List<PathwayGene> pathwayGenes = commonFileParams.getPathwayGenes();
        PercLfcBarChart percLfcBarChart = new PercLfcBarChart(getChartGeneratorsBuilder(degs, pathways, pathwayGenes));
        percLfcBarChart.saveChart();
    }

    /**
     * Builds the chart generator with the necessary parameters.
     *
     * @param degs List of differentially expressed genes.
     * @param pathways List of pathways to analyze.
     * @param pathwayGenes List of pathway genes associated with the pathways.
     * @return A builder for the PercLfcBarChart.
     */
    private PercLfcBarChart.Builder getChartGeneratorsBuilder(List<Deg> degs, List<Pathway> pathways, List<PathwayGene> pathwayGenes) {
        return new PercLfcBarChart.Builder(
                commonChartParams.title,
                commonChartParams.xAxisTitle,
                commonChartParams.yAxisTitle,
                degs,
                pathways,
                pathwayGenes,
                commonChartParams.outputPath)
                .colorManual(commonChartParams.colorManual)
                .maxNPathways(maxNPathways)
                .imageFormat(commonChartParams.imageFormat)
                .pathwayIds(pathwayIds);
    }
}

/**
 * First-layer (CLI) sub-command for generating and printing a continuity table.
 * The table contains count data on two aspects of DEGs for every pathway: presence in pathway and presence of significance.
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

    @Option(names = {"--output"}, paramLabel = "[file|print]",
            description = "Option on how to return output table. (csv-file or print to terminal)",
            defaultValue = "file")
    private String output;

    @Option(names = {"--outputFilePath"}, description = "File to write table text to.")
    private File outputFilePath;

    /**
     * Executes the command to generate and handle a continuity table.
     * It retrieves data and either writes it to a file or prints it to the terminal based on user options.
     */
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

    /**
     * Handles the output of the continuity table based on the specified option.
     *
     * @param outputTable The generated continuity table as a string.
     */
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
