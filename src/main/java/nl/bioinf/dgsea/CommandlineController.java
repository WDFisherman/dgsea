/**
 * Manages the command-line interface of this application using Picocli.
 * This app is split into 4 custom subcommands starting from main.
 * The class from CommonCliOptions.java is used for inheritance of multiple options common in 2 or more sub-commands.
 *
 * @authors Jort Gommers & Willem Daniël Visser
 */
package nl.bioinf.dgsea;

import nl.bioinf.dgsea.table_outputs.TwoByTwoContingencyTable;
import nl.bioinf.dgsea.visualisations.PercLfcBarChart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Mixin;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;


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
    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Mixin
    private CommonToAll commonToAll;
    @Mixin
    private CommonFileParams commonFileParams;
    @Mixin
    private CommonChartParams commonChartParams;

    @Option(names = {"--output-file", "-o", "-O"}, paramLabel = "FILE",
            description = "Output file path for the bar chart (e.g., ./output/enrichment_bar_chart.png)")
    private String outputFilePath;


    /**
     * Executes the command to generate and save an enrichment bar chart.
     * Retrieves data from common file parameters and generates the chart using the enrichment service.
     */
    @Override
    public void run() {
        validateOptions();
        commonToAll.validateOptions();
        commonChartParams.validateOptions();
        commonToAll.setLoggingScope();
        
        Color[] colorArray = commonChartParams.getColorManualAsColors();

        EnrichmentAnalysisService enrichmentService = new EnrichmentAnalysisService();
        try {
            enrichmentService.generateEnrichmentChart(
                    commonFileParams.getDegs(),
                    commonFileParams.getPathways(),
                    commonFileParams.getPathwayGenes(),
                    commonChartParams.getMaxNPathways(),
                    outputFilePath,
                    commonChartParams.getTitle(),
                    colorArray,
                    EnrichmentAnalysisService.ChartType.BAR_CHART, // Bar chart
                    null,  // dotSize not needed
                    null   // dotTransparency not needed
            );
        } catch (IOException e) {
            logger.error("Error reading input data or saving PNG: {}", e.getMessage());
        }
    }

    /**
     * validates this.outputFilePath not to be null
     * @throws CommandLine.ParameterException if any validation fails
     */
    private void validateOptions() {
        if (outputFilePath == null) {
            throw new CommandLine.ParameterException(spec.commandLine(), "Output file path -output-file must be specified");
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
    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;
    private final Logger logger = LogManager.getLogger(EnrichDotChart.class);

    @Mixin
    private CommonToAll commonToAll;
    @Mixin
    private CommonFileParams commonFileParams;
    @Mixin
    private CommonChartParams commonChartParams;

    @Option(names = {"--dot-size", "-ds", "-DS"}, paramLabel = "[0.0-inf]",
            description = "Dot size, default = ${DEFAULT-VALUE}", defaultValue = "30.0")
    private double dotSize;
    @Option(names = {"--dot-transparency", "-dt", "-DT"}, paramLabel = "[0.0-1.0]",
            description = "Dot transparency, default = ${DEFAULT-VALUE}", defaultValue = "1.0")
    private float dotTransparency;
    @Option(names = {"--output-file", "-o", "-O"}, paramLabel = "FILE",
            description = "Output file path for the dot plot (e.g., ./output/enrichment_dot_plot.png)")
    private String outputFilePath;

    /**
     * Executes the command to generate and save an enrichment dot chart.
     * Retrieves data from common file parameters and generates the chart using the enrichment service.
     */
    @Override
    public void run() {
        validateOptions();
        commonToAll.validateOptions();
        commonChartParams.validateOptions();
        commonToAll.setLoggingScope();

        Color[] colorArray = commonChartParams.getColorManualAsColors();

        EnrichmentAnalysisService enrichmentService = new EnrichmentAnalysisService();
        try {
            enrichmentService.generateEnrichmentChart(
                    commonFileParams.getDegs(),
                    commonFileParams.getPathways(),
                    commonFileParams.getPathwayGenes(),
                    commonChartParams.getMaxNPathways(),
                    outputFilePath,
                    commonChartParams.getTitle(),
                    colorArray,
                    EnrichmentAnalysisService.ChartType.DOT_CHART, // Dot chart
                    dotSize,
                    dotTransparency
            );
        } catch (IOException e) {
            logger.error("Error reading input data or saving PNG: {}", e.getMessage());
        }
    }

    /**
     * validates this.dotSize and this.dotTransparency to be positive.
     * validates this.dotTransparency to be lower than 1.0
     * validates this.outputFilePath not to be null
     * @throws CommandLine.ParameterException if any validation fails
     */
    private void validateOptions() {
        if (dotSize < 0) {
            throw new CommandLine.ParameterException(spec.commandLine(), "Dot size --dot-size must be a positive number");
        }
        if (dotTransparency < 0.0) {
            throw new CommandLine.ParameterException(spec.commandLine(), "Dot transparency --dot-transparency must be a positive number");
        }
        if (dotTransparency >= 1.0) {
            throw new CommandLine.ParameterException(spec.commandLine(), "Dot transparency --dot-transparency cannot be greater than 1.0");
        }
        if (outputFilePath == null) {
            throw new CommandLine.ParameterException(spec.commandLine(), "Output file path -output-file must be specified");
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
    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;
    private final Logger logger = LogManager.getLogger(PercLogFChangePerPathwayCmd.class);

    @Mixin
    private CommonToAll commonToAll;
    @Mixin
    private CommonFileParams commonFileParams;
    @Mixin
    private CommonChartParams commonChartParams;

    @Option(names = {"--pathway-ids", "-p-ids", "-P-IDS"}, paramLabel = "hsa123", arity = "0..*", split = ",",
            description = "Pathway ids of interest")
    private String[] pathwayIds;

    /**
     * Executes the command to generate and save a bar chart of average log-fold changes.
     * Retrieves data from common file parameters and creates the chart using the PercLfcBarChart class.
     */
    @Override
    public void run() {
        validateOptions();
        commonToAll.validateOptions();
        commonChartParams.validateOptions();
        commonToAll.setLoggingScope();
        PercLfcBarChart percLfcBarChart = new PercLfcBarChart(getChartGeneratorsBuilder());
        try {
            percLfcBarChart.saveChart();
        } catch (IOException e) {
            logger.error(e.getMessage());
        } catch (IllegalArgumentException e1) {
            logger.fatal(e1.getMessage());
        }
    }

    /**
     * Builds the chart generator with the necessary parameters.
     * @return A builder for the PercLfcBarChart.
     */
    private PercLfcBarChart.Builder getChartGeneratorsBuilder() {
        return new PercLfcBarChart.Builder(
                commonChartParams.getTitle(),
                commonChartParams.getxAxisTitle(),
                commonChartParams.getyAxisTitle(),
                commonFileParams.getDegs(),
                commonFileParams.getPathways(),
                commonFileParams.getPathwayGenes(),
                commonChartParams.getOutputPath())
                .colorManual(commonChartParams.getColorManualAsColors())
                .maxNPathways(commonChartParams.getMaxNPathways())
                .imageFormat(commonChartParams.getImageFormat())
                .pathwayIds(pathwayIds);
    }

    /**
     * validates if any string in this.pathwayIds is just a space-character: ' '
     * @throws CommandLine.ParameterException if any validation fails
     */
    private void validateOptions() {
        if (pathwayIds != null && pathwayIds.length != 0) {
            if (Arrays.stream(pathwayIds).anyMatch(String::isEmpty)) {
                throw new CommandLine.ParameterException(spec.commandLine(), "Not any pathway-id in option --pathway-ids can be empty, given pathway-ids: " + Arrays.toString(pathwayIds));
            }
        }
    }
}

/**
 * First-layer (CLI) sub-command for generating and printing a continuity table.
 * The table contains count data on two aspects of DEGs for every pathway: presence in pathway and presence of significance.
 */
@Command(name = "con_table", version = "Continuity table 1.0", mixinStandardHelpOptions = true,
        description = "Prints or stores to text file a continuity table of count data on 2 aspects of DEGs for every pathway: presence in pathway and presence of significance")
class ContinuityTable implements Runnable {

    final Logger logger = LogManager.getLogger(ContinuityTable.class);

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @Mixin
    private CommonToAll commonToAll;
    @Mixin
    private CommonFileParams commonFileParams;

    @Option(names = {"--outputType", "-t", "-T"}, paramLabel = "file|print",
            description = "Option on how to return output table. (csv-file or print to terminal)",
            defaultValue = "file")
    private String output;
    @Option(names = {"--outputFilePath", "-o", "-O"}, description = "File to write table text to.")
    private File outputFilePath;

    /**
     * Executes the command to generate and handle a continuity table.
     * It retrieves data and either writes it to a file or prints it to the terminal based on user options.
     */
    @Override
    public void run() {
        validateOptions();
        commonToAll.validateOptions();
        commonToAll.setLoggingScope();
        TwoByTwoContingencyTable twoByTwoContingencyTable = new TwoByTwoContingencyTable(
                commonFileParams.getDegs(),
                commonFileParams.getPathways(),
                commonFileParams.getPathwayGenes(),
                commonToAll.getPval()
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
     * @param outputTable The generated continuity table as a string.
     */
    private void handleOutput(String outputTable) {

        if ("file".equalsIgnoreCase(output)) {
            try {
                java.nio.file.Files.write(outputFilePath.toPath(), outputTable.getBytes());
                logger.info("Continuity table written to: {}", outputFilePath.getPath());
            } catch (IOException e) {
                logger.error("Error writing continuity table to file: {}", e.getMessage());
            }
        } else {
            System.out.println(outputTable);
        }
    }

    /**
     * validates if this.output is either 'file' or 'print' (case-insensitive)
     * validates if this.output is 'file', yet no file was given in this.outputFilePath
     * @throws CommandLine.ParameterException if any validation fails
     */
    private void validateOptions() {
        if (!output.equalsIgnoreCase("file") && !output.equalsIgnoreCase("print")) {
            throw new CommandLine.ParameterException(spec.commandLine(), "Output type(--outputType) must either be 'file' or 'print', given output option: " + output);
        }
        if (outputFilePath == null && output.equalsIgnoreCase("file")) {
            throw new CommandLine.ParameterException(spec.commandLine(), "Output file path(--outputFilePath) must be specified, if output type is 'file'.");
        }
    }
}
