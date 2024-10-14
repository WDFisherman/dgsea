package nl.bioinf.dgsea.data_processing;

// Import necessary Java classes for file handling and data structures
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Utility class for parsing various data files related to Differentially Expressed Genes (DEGs), pathways, and pathway genes.
public class FileParseUtils {

    // Parses a file containing DEGs and returns a list of Deg objects.
    public List<Deg> parseDegsFile(File file) throws Exception {
        List<Deg> degs = new ArrayList<>(); // List to store DEGs
        try (BufferedReader br = new BufferedReader(new FileReader(file))) { // Use try-with-resources to ensure the BufferedReader is closed
            String line;
            // Read each line from the file
            while ((line = br.readLine()) != null) {
                String[] values = line.split(","); // Split line by comma (CSV format)
                // Check for a minimum number of columns
                if (values.length < 3) {
                    throw new Exception("Invalid DEG file format. Expected at least 3 columns.");
                }
                // Parse values from the line
                String geneSymbol = values[0].trim(); // Gene symbol
                double logFoldChange = Double.parseDouble(values[1].trim()); // Log fold change
                double adjustedPValue = Double.parseDouble(values[2].trim()); // Adjusted p-value
                // Create a new Deg object and add it to the list
                degs.add(new Deg(geneSymbol, logFoldChange, adjustedPValue));
            }
        } catch (IOException e) {
            // Handle IOException when reading the file
            throw new IOException("Error reading DEG file: " + e.getMessage());
        } catch (NumberFormatException e) {
            // Handle parsing errors for numeric values
            throw new NumberFormatException("Error parsing numeric values in DEG file: " + e.getMessage());
        }
        return degs; // Return the list of DEGs
    }

    // Parses a file containing pathways and returns a list of Pathway objects.
    public List<Pathway> parsePathwayFile(File file) throws Exception {
        List<Pathway> pathways = new ArrayList<>(); // List to store pathways
        try (BufferedReader br = new BufferedReader(new FileReader(file))) { // Use try-with-resources for BufferedReader
            String line;
            // Read each line from the file
            while ((line = br.readLine()) != null) {
                String[] values = line.split(","); // Split line by comma (CSV format)
                // Check for a minimum number of columns
                if (values.length < 2) {
                    throw new Exception("Invalid Pathway file format. Expected at least 2 columns.");
                }
                // Parse values from the line
                String pathwayId = values[0].trim(); // Pathway ID
                String description = values[1].trim(); // Pathway description
                // Create a new Pathway object and add it to the list
                pathways.add(new Pathway(pathwayId, description));
            }
        } catch (IOException e) {
            // Handle IOException when reading the file
            throw new IOException("Error reading Pathway file: " + e.getMessage());
        }
        return pathways; // Return the list of Pathways
    }

    // Parses a file containing pathway-gene relationships and returns a list of PathwayGene objects.
    public List<PathwayGene> parsePathwayGeneFile(File file) throws Exception {
        List<PathwayGene> pathwayGenes = new ArrayList<>(); // List to store pathway-gene relationships
        try (BufferedReader br = new BufferedReader(new FileReader(file))) { // Use try-with-resources for BufferedReader
            String line;
            // Read each line from the file
            while ((line = br.readLine()) != null) {
                String[] values = line.split(","); // Split line by comma (CSV format)
                // Check for a minimum number of columns
                if (values.length < 4) {
                    throw new Exception("Invalid PathwayGene file format. Expected at least 4 columns.");
                }
                // Parse values from the line
                String pathwayId = values[0].trim(); // Pathway ID
                int entrezGeneId = Integer.parseInt(values[1].trim()); // Entrez Gene ID
                String geneSymbol = values[2].trim(); // Gene symbol
                String ensemblGeneId = values[3].trim(); // Ensembl Gene ID
                // Create a new PathwayGene object and add it to the list
                pathwayGenes.add(new PathwayGene(pathwayId, entrezGeneId, geneSymbol, ensemblGeneId));
            }
        } catch (IOException e) {
            // Handle IOException when reading the file
            throw new IOException("Error reading PathwayGene file: " + e.getMessage());
        } catch (NumberFormatException e) {
            // Handle parsing errors for numeric values
            throw new NumberFormatException("Error parsing numeric values in PathwayGene file: " + e.getMessage());
        }
        return pathwayGenes; // Return the list of PathwayGene relationships
    }
}
