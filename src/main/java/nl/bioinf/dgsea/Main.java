package nl.bioinf.dgsea;

import nl.bioinf.dgsea.data_processing.Deg;
import nl.bioinf.dgsea.data_processing.FileParseUtils;
import nl.bioinf.dgsea.data_processing.Pathway;
import nl.bioinf.dgsea.data_processing.PathwayGene;
import nl.bioinf.dgsea.data_processing.FileParseUtils;
import nl.bioinf.dgsea.table_outputs.EnrichmentTable;
import nl.bioinf.dgsea.table_outputs.Table;

import java.io.File;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Main m = new Main();
        m.start();
    }

    private  void start() {
        try {

            // Specify your file paths here
            String degFilePath = "test_data/degs.csv";          // Change this to the actual path
            String pathwayFilePath = "test_data/hsa_pathways.csv";  // Change this to the actual path
            String pathwayGeneFilePath = "test_data/pathways.csv";  // Change this to the actual path

            // Set your parameters
            double adjustedPValueThreshold = 0.01;  // Example threshold
            String[] selectedPathwayIds = new String[] {}; // Add any pathway IDs you want to select

            // Create an instance of FileParsing
            FileParseUtils fileParseUtils = new FileParseUtils();

            // Parse the files
            List<Deg> degs = fileParseUtils.parseDegsFile(new File(degFilePath));
            List<Pathway> pathways = fileParseUtils.parsePathwayFile(new File(pathwayFilePath));
            List<PathwayGene> pathwayGenes = fileParseUtils.parsePathwayGeneFile(new File(pathwayGeneFilePath));

            // Initialize the Table class with parsed data
            Table.degs = degs;           // Set the DEGs
            Table.pathways = pathways;   // Set the pathways
            Table.pathwayGenes = pathwayGenes; // Set the pathway genes

            // Create an instance of Table and print the contingency table
            Table table = new Table() {}; // Anonymous class since Table is abstract
            String output = table.getTwoByTwoContingencyTable();
            System.out.println(output);

            // Create enrichment table
            EnrichmentTable enrichmentTable = new EnrichmentTable(pathways, degs, pathwayGenes);
            enrichmentTable.calculateEnrichment();



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
