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
import java.util.Set;
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

/**
 * First-layer (CLI-) sub-command |
 * Calculates enrichment scores for each pathway, generates and saves enrichment bar chart to file.
 */
@Command(name = "enrich_bar_chart", version = "Enrichment bar-chart 1.0", mixinStandardHelpOptions = true, description = "No description yet")
class EnrichBarChart implements Runnable {
    @Mixin
    private CommonToAll commonToAll;
    @Mixin
    private CommonFileParams commonFileParams;
    @Mixin
    private CommonChartParams commonChartParams;

    @Override
    public void run() {
        commonToAll.setLoggingScope();
    }
}

/**
 * First-layer (CLI-) sub-command |
 * Calculates enrichment scores for each pathway, generates and saves enrichment dot chart to file.
 */
@Command(name = "enrich_dot_chart", version = "Enrichment dot-chart 1.0", mixinStandardHelpOptions = true, description = "This command over-aches commands for now: enrich_bar_chart and enrich_dot_chart and makes both charts")
class EnrichDotChart implements Runnable {
    @Mixin
    private CommonToAll commonToAll;
    @Mixin
    private CommonFileParams commonFileParams;
    @Mixin
    private CommonChartParams commonChartParams;

    @Option(names = {"--dot-size"}, paramLabel = "[0.0-inf]", description = "Dot size, default = ${DEFAULT-VALUE}", defaultValue = "1.0")
    private double dotSize;
    @Option(names = {"--dot-transparency"}, paramLabel = "[0.0-1.0]", description = "Dot transparency, default = ${DEFAULT-VALUE}", defaultValue = "1.0")
    private double dotTransparency;

    @Override
    public void run() {
        commonToAll.setLoggingScope();
        List<Deg> degs = commonFileParams.getDegs();
        List<Pathway> pathways = commonFileParams.getPathways();
        List<PathwayGene> pathwayGenes = commonFileParams.getPathwayGenes();
        try {
            // Bereken de enrichment en haal de resultaten op
            EnrichmentTable enrichmentTable = new EnrichmentTable(pathways, degs, pathwayGenes);
            enrichmentTable.calculateEnrichment();
            List<EnrichmentResult> results = enrichmentTable.getEnrichmentResults();

            // Selecteer de pathways met een significante adjusted p-value
            List<EnrichmentResult> significantResults = results.stream()
                    .filter(result -> !Double.isNaN(result.adjustedPValue()) && result.adjustedPValue() < 0.05) // Filter uit op NaN waarden en significante p-waarden
                    .toList();

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
            System.err.println("Fout bij het lezen van de invoer data of het opslaan van de PNG: " + e.getMessage());
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
    private Set<String> pathwayIds;
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

