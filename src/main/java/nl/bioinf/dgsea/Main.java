package nl.bioinf.dgsea;

import nl.bioinf.dgsea.data_processing.Deg;
import nl.bioinf.dgsea.data_processing.Pathway;
import nl.bioinf.dgsea.data_processing.PathwayGene;
import nl.bioinf.dgsea.data_processing.FileParsing;
import nl.bioinf.dgsea.table_outputs.Table;

import java.io.File;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            // Specify your file paths here
            String degFilePath = "/Users/jortgommers/Downloads/dgsea-master/test_data/degs.csv";          // Change this to the actual path
            String pathwayFilePath = "/Users/jortgommers/Downloads/dgsea-master/test_data/hsa_pathways.csv";  // Change this to the actual path
            String pathwayGeneFilePath = "/Users/jortgommers/Downloads/dgsea-master/test_data/pathways.csv";  // Change this to the actual path

            // Set your parameters
            double adjustedPValueThreshold = 0.01;  // Example threshold
            String[] selectedPathwayIds = new String[] {}; // Add any pathway IDs you want to select

            // Create an instance of FileParsing
            FileParsing fileParsing = new FileParsing(adjustedPValueThreshold, selectedPathwayIds, degFilePath, pathwayFilePath);

            // Parse the files
            List<Deg> degs = fileParsing.parseDegsFile(new File(degFilePath));
            List<Pathway> pathways = fileParsing.parsePathwayFile(new File(pathwayFilePath));
            List<PathwayGene> pathwayGenes = fileParsing.parsePathwayGeneFile(new File(pathwayGeneFilePath));

            // Initialize the Table class with parsed data
            Table.degs = degs;           // Set the DEGs
            Table.pathways = pathways;   // Set the pathways
            Table.pathwayGenes = pathwayGenes; // Set the pathway genes

            // Create an instance of Table and print the contingency table
            Table table = new Table() {}; // Anonymous class since Table is abstract
            String output = table.getTwoByTwoContingencyTable();
            System.out.println(output);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
