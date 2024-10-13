package nl.bioinf.dgsea;

import nl.bioinf.dgsea.data_processing.*;
import nl.bioinf.dgsea.table_outputs.EnrichmentTable;
import nl.bioinf.dgsea.table_outputs.Table;
import nl.bioinf.dgsea.visualisations.EnrichmentBarChart;  // Voeg de EnrichmentBarChart toe
import nl.bioinf.dgsea.visualisations.EnrichmentDotPlot;   // Voeg de EnrichmentDotPlot toe

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        Main m = new Main();
        m.start();
    }

    private void start() {
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
