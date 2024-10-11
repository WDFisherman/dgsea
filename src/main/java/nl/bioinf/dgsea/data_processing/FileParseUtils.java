package nl.bioinf.dgsea.data_processing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileParseUtils {

    public List<Deg> parseDegsFile(File file) throws Exception {
        List<Deg> degs = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(","); // Assuming CSV format
                if (values.length < 3) {
                    throw new Exception("Invalid DEG file format. Expected at least 3 columns.");
                }
                String geneSymbol = values[0].trim();
                double logFoldChange = Double.parseDouble(values[1].trim());
                double adjustedPValue = Double.parseDouble(values[2].trim());
                degs.add(new Deg(geneSymbol, logFoldChange, adjustedPValue));
            }
        } catch (IOException e) {
            throw new Exception("Error reading DEG file: " + e.getMessage());
        } catch (NumberFormatException e) {
            throw new Exception("Error parsing numeric values in DEG file: " + e.getMessage());
        }
        return degs;
    }

    public List<Pathway> parsePathwayFile(File file) throws Exception {
        List<Pathway> pathways = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(","); // Assuming CSV format
                if (values.length < 2) {
                    throw new Exception("Invalid Pathway file format. Expected at least 2 columns.");
                }
                String pathwayId = values[0].trim();
                String description = values[1].trim();
                pathways.add(new Pathway(pathwayId, description));
            }
        } catch (IOException e) {
            throw new Exception("Error reading Pathway file: " + e.getMessage());
        }
        return pathways;
    }

    public List<PathwayGene> parsePathwayGeneFile(File file) throws Exception {
        List<PathwayGene> pathwayGenes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(","); // Assuming CSV format
                if (values.length < 4) {
                    throw new Exception("Invalid PathwayGene file format. Expected at least 4 columns.");
                }
                String pathwayId = values[0].trim();
                int entrezGeneId = Integer.parseInt(values[1].trim());
                String geneSymbol = values[2].trim();
                String ensemblGeneId = values[3].trim();
                pathwayGenes.add(new PathwayGene(pathwayId, entrezGeneId, geneSymbol, ensemblGeneId));
            }
        } catch (IOException e) {
            throw new Exception("Error reading PathwayGene file: " + e.getMessage());
        } catch (NumberFormatException e) {
            throw new Exception("Error parsing numeric values in PathwayGene file: " + e.getMessage());
        }
        return pathwayGenes;
    }
}
