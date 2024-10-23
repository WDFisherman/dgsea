/**
 * Manages the commandline interface of this application, using Picocli.
 *  This app is split in 4 custom subcommands starting from main.
 *  Class from CommonCliOptions.java is used for inheritance of multiple options common in 2 or more sub-commands.
 * @Authors Jort Gommers & Willem Daniël Visser
 */

package nl.bioinf.dgsea;

import nl.bioinf.dgsea.data_processing.*;
import nl.bioinf.dgsea.table_outputs.TwoByTwoContingencyTable;
import nl.bioinf.dgsea.visualisations.ChartGenerator;
import nl.bioinf.dgsea.table_outputs.EnrichmentTable;
import nl.bioinf.dgsea.visualisations.EnrichmentBarChart;
import nl.bioinf.dgsea.visualisations.EnrichmentDotPlot;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Mixin;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    @Option(names = {"--max-n-pathways"}, paramLabel = "[1-inf]", description = "Max number of pathways to include in chart. '--pathway-ids' overrides this option.")
    private int maxNPathways;

    @Override
    public void run() {
        commonToAll.setLoggingScope();

        // Fetch input data
        List<Deg> degs = commonFileParams.getDegs();
        List<Pathway> pathways = commonFileParams.getPathways();
        List<PathwayGene> pathwayGenes = commonFileParams.getPathwayGenes();

        try {
            EnrichmentTable enrichmentTable = new EnrichmentTable(pathways, degs, pathwayGenes);
            enrichmentTable.calculateEnrichment();
            List<EnrichmentResult> results = enrichmentTable.getEnrichmentResults();

            List<EnrichmentResult> significantResults = results.stream()
                    .filter(result -> !Double.isNaN(result.adjustedPValue()) && result.adjustedPValue() < 0.05)
                    .toList();

            // Use the limit option here
            List<EnrichmentResult> topResults = significantResults.stream()
                    .sorted(Comparator.comparingDouble(EnrichmentResult::enrichmentScore).reversed())
                    .limit(maxNPathways) // Apply the limit from the command line
                    .collect(Collectors.toList());

            // Check if output file path is provided
            String outputFile = (outputFilePath != null && !outputFilePath.isEmpty()) ? outputFilePath : "pathway_enrichment_bar_chart.png";

            // Process the color input, if provided
            String[] colorArray = null;
            if (commonChartParams.colorManual != null && commonChartParams.colorManual.length != 0) {
                colorArray = commonChartParams.colorManual;
            }

            // Generate the bar chart with color input
            EnrichmentBarChart barChart = new EnrichmentBarChart(
                    "Top " + maxNPathways + " Pathway Enrichment", // Update title with limit
                    topResults,
                    pathways,
                    outputFile,
                    colorArray, // Pass the color array to the constructor
                    commonChartParams.colorScheme
            );

            System.out.println("Bar chart saved as PNG at: " + outputFile);

        } catch (IOException e) {
            System.err.println("Error reading input data or saving PNG: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
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

    @Option(names = {"--color"}, paramLabel = "COLORS", description = "Manual colors for the dots, provide as a comma-separated list (e.g., red,blue,green).")
    private String[] colorManual;

    @Option(names = {"--max-n-pathways"}, paramLabel = "[1-inf]", description = "Max number of pathways to include in chart. '--pathway-ids' overrides this option.")
    private int maxNPathways;

    @Override
    public void run() {
        commonToAll.setLoggingScope();
        List<Deg> degs = commonFileParams.getDegs();
        List<Pathway> pathways = commonFileParams.getPathways();
        List<PathwayGene> pathwayGenes = commonFileParams.getPathwayGenes();

        try {
            // Calculate enrichment and fetch results
            EnrichmentTable enrichmentTable = new EnrichmentTable(pathways, degs, pathwayGenes);
            enrichmentTable.calculateEnrichment();
            List<EnrichmentResult> results = enrichmentTable.getEnrichmentResults();

            // Select significant results
            List<EnrichmentResult> significantResults = results.stream()
                    .filter(result -> !Double.isNaN(result.adjustedPValue()) && result.adjustedPValue() < 0.05)
                    .toList();

            // Sort by enrichment score and apply limit
            List<EnrichmentResult> topResults = significantResults.stream()
                    .sorted(Comparator.comparingDouble(EnrichmentResult::enrichmentScore).reversed())
                    .limit(maxNPathways) // Apply the limit from the command line
                    .collect(Collectors.toList());

            // Use specified colors or default manual colors
            String[] colorArray = (colorManual != null && colorManual.length != 0) ? colorManual : commonChartParams.colorManual;

            // Create and save the dot plot
            String dotPlotOutputFilePath = "pathway_enrichment_dot_plot.png";
            EnrichmentDotPlot dotPlot = new EnrichmentDotPlot(
                    "Pathway Enrichment Dot Plot (Top " + maxNPathways + ")", // Update title with limit
                    topResults,
                    pathways,
                    dotPlotOutputFilePath,
                    colorArray,  // Pass specified colors
                    commonChartParams.colorScheme,
                    dotSize,     // Pass dot size
                    dotTransparency // Pass dot transparency
            );

            System.out.println("Dot plot saved as PNG at: " + dotPlotOutputFilePath);
            System.out.println("Dot Size: " + dotSize);

        } catch (IOException e) {
            System.err.println("Error reading input data or saving PNG: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
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
        ChartGenerator chartGenerator = new ChartGenerator(getChartGeneratorsBuilder(degs, pathways, pathwayGenes));
        chartGenerator.saveChartPercLogFChangePerPathway();
    }
    private ChartGenerator.Builder getChartGeneratorsBuilder(List<Deg> degs, List<Pathway> pathways, List<PathwayGene> pathwayGenes) {
        return new ChartGenerator.Builder(
                commonChartParams.title,
                commonChartParams.xAxisTitle,
                commonChartParams.yAxisTitle,
                degs,
                pathways,
                pathwayGenes,
                commonChartParams.outputPath
        ).colorScheme(commonChartParams.colorScheme)
        .colorManual(commonChartParams.colorManual)
        .dpi(commonChartParams.imageDpi)
        .maxNPathways(maxNPathways)
        .imageFormat(commonChartParams.imageFormat)
        .pathwayIds(pathwayIds);
    }
}

/**
 * First-layer (CLI-) sub-command |
 * Prints or stores to text file a continuity table of count data on 2 aspects of degs for every pathway: presence in pathway and presence of significance
 */
@Command(name = "con_table", version = "Continuity table 1.0", mixinStandardHelpOptions = true, description = "Prints or stores to text file a continuity table of count data on 2 aspects of degs for every pathway: presence in pathway and presence of significance")
class ContinuityTable implements Runnable {
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
        String output = twoByTwoContingencyTable.getTable();
        System.out.println(output);
    }
}

