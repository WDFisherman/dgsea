package nl.bioinf.dgsea.table_outputs;

import nl.bioinf.dgsea.data_processing.Deg;
import nl.bioinf.dgsea.data_processing.Pathway;
import nl.bioinf.dgsea.data_processing.PathwayGene;

import java.util.List;

public abstract class Table {
    public static List<Deg> degs;
    public static List<Pathway> pathways;
    public static List<PathwayGene> pathwayGenes;

    public String getTwoByTwoContingencyTable() throws Exception {
        StringBuilder output = new StringBuilder();

        for (Pathway pathway : pathways) {
            String pathwayId = pathway.pathwayId();
            int sumInPathway = getSumInPathway(pathwayId);
            int sumTotalPathway = getSumTotalPathway(pathwayId);
            int sumIsSignificantDeg = getSumIsSignificantDeg();

            // Calculate counts for the contingency table
            int significantInPathwayCount = sumInPathway; // DEGs in the pathway
            int notInPathwayCount = getSumTotalDeg() - significantInPathwayCount; // Total DEGs - DEGs in the pathway

            // Count non-significant DEGs in the pathway
            int nonSignificantInPathwayCount = getSumNonSignificantInPathway(pathwayId);
            int totalNonDEGsInPathway = sumTotalPathway - significantInPathwayCount; // Total genes - DEGs in the pathway

            // Build output for the current pathway
            output.append(pathway.description()).append("\n | D | D* | Sum\n");
            output.append("C| ").append(significantInPathwayCount).append(" | ")
                    .append(nonSignificantInPathwayCount).append(" | ")
                    .append(significantInPathwayCount + nonSignificantInPathwayCount).append("\n");
            output.append("C*| ").append(notInPathwayCount).append(" | ")
                    .append(totalNonDEGsInPathway).append(" | ")
                    .append(notInPathwayCount + totalNonDEGsInPathway).append("\n");
            output.append("Sum | ").append(getSumTotalDeg()).append(" | ")
                    .append(totalNonDEGsInPathway + notInPathwayCount).append(" | ")
                    .append(getSumTotalDeg() + totalNonDEGsInPathway + notInPathwayCount).append("\n\n");

        }

        return output.toString();
    }

    private int getSumInPathway(String pathwayId) {
        return (int) pathwayGenes.stream()
                .filter(pg -> pg.pathwayId().equals(pathwayId) && isDeg(pg.geneSymbol()))
                .count();
    }

    private int getSumTotalPathway(String pathwayId) {
        return (int) pathwayGenes.stream()
                .filter(pg -> pg.pathwayId().equals(pathwayId))
                .count();
    }

    private int getSumIsSignificantDeg() {
        return (int) degs.stream()
                .filter(deg -> deg.adjustedPValue() <= 0.01) // Adjust threshold as necessary
                .count();
    }

    private int getSumTotalDeg() {
        return degs.size();
    }

    private boolean isDeg(String geneSymbol) {
        return degs.stream().anyMatch(deg -> deg.geneSymbol().equals(geneSymbol));
    }

    private int getSumNonSignificantInPathway(String pathwayId) {
        return (int) pathwayGenes.stream()
                .filter(pg -> pg.pathwayId().equals(pathwayId) && isDeg(pg.geneSymbol()))
                .filter(pg -> degs.stream().noneMatch(deg -> deg.geneSymbol().equals(pg.geneSymbol()) && deg.adjustedPValue() <= 0.01))
                .count();
    }
}
