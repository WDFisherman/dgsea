package nl.bioinf.dgsea.data_processing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for parsing various data files related to Differentially Expressed Genes (DEGs),
 * pathways, and pathway genes.
 */
public class FileParseUtils {

    private static final String DEG_FORMAT_ERROR = "Invalid DEG file format. Expected at least 3 columns.";
    private static final String PATHWAY_FORMAT_ERROR = "Invalid Pathway file format. Expected at least 2 columns.";
    private static final String PATHWAY_GENE_FORMAT_ERROR = "Invalid PathwayGene file format. Expected at least 4 columns.";

    /**
     * Parses a file containing DEGs and returns a list of Deg objects.
     *
     * @param file the DEG file to parse
     * @return a list of Deg objects
     * @throws IOException if an I/O error occurs
     * @throws NumberFormatException if a number cannot be parsed
     */
    public List<Deg> parseDegsFile(File file) throws IOException, NumberFormatException {
        validateFile(file);
        List<Deg> degs = new ArrayList<>(); // List to store DEGs

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(","); // Split line by comma (CSV format)
                if (values.length < 3) {
                    throw new IOException(DEG_FORMAT_ERROR);
                }
                String geneSymbol = values[0].trim();
                double logFoldChange = Double.parseDouble(values[1].trim());
                double adjustedPValue = Double.parseDouble(values[2].trim());
                degs.add(new Deg(geneSymbol, logFoldChange, adjustedPValue));
            }
        }
        return degs;
    }

    /**
     * Parses a file containing pathways and returns a list of Pathway objects.
     *
     * @param file the Pathway file to parse
     * @return a list of Pathway objects
     * @throws IOException if an I/O error occurs
     */
    public List<Pathway> parsePathwayFile(File file) throws IOException {
        validateFile(file);
        List<Pathway> pathways = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length < 2) {
                    throw new IOException(PATHWAY_FORMAT_ERROR);
                }
                String pathwayId = values[0].trim();
                String description = values[1].trim();
                pathways.add(new Pathway(pathwayId, description));
            }
        }
        return pathways;
    }

    /**
     * Parses a file containing pathway-gene relationships and returns a list of PathwayGene objects.
     *
     * @param file the PathwayGene file to parse
     * @return a list of PathwayGene objects
     * @throws IOException if an I/O error occurs
     * @throws NumberFormatException if a number cannot be parsed
     */
    public List<PathwayGene> parsePathwayGeneFile(File file) throws IOException, NumberFormatException {
        validateFile(file);
        List<PathwayGene> pathwayGenes = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length < 4) {
                    throw new IOException(PATHWAY_GENE_FORMAT_ERROR);
                }
                String pathwayId = values[0].trim();
                int entrezGeneId = Integer.parseInt(values[1].trim());
                String geneSymbol = values[2].trim();
                String ensemblGeneId = values[3].trim();
                pathwayGenes.add(new PathwayGene(pathwayId, entrezGeneId, geneSymbol, ensemblGeneId));
            }
        }
        return pathwayGenes;
    }

    /**
     * Validates the given file.
     *
     * @param file the file to validate
     * @throws IOException if the file is not readable
     */
    private void validateFile(File file) throws IOException {
        if (file == null || !file.exists() || !file.canRead()) {
            throw new IOException("File is not readable: " + file);
        }
    }
}
