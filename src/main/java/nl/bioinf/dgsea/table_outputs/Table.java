package nl.bioinf.dgsea.table_outputs;

import nl.bioinf.dgsea.data_processing.Deg;
import nl.bioinf.dgsea.data_processing.Pathway;
import nl.bioinf.dgsea.data_processing.PathwayGene;

import java.util.ArrayList;
import java.util.List;

public abstract class Table {
    public static List<Deg> degs = new ArrayList<>();
    public static List<Pathway> pathways = new ArrayList<>();
    public static List<PathwayGene> pathwayGenes = new ArrayList<>();

    public String getTwoByTwoContingencyTable() throws IllegalStateException {
        if (pathways == null || degs == null || pathwayGenes == null) {
            throw new IllegalStateException("Data lists must be initialized before use.");
        }

        StringBuilder output = new StringBuilder();

        for (Pathway pathway : pathways) {
            String pathwayId = pathway.pathwayId();
            int sumInPathway = getSumInPathway(pathwayId);
            int sumTotalPathway = getSumTotalPathway(pathwayId);
            int sumIsSignificantDeg = getSumIsSignificantDeg();

            // Calculate counts for the contingency table
            int significantInPathwayCount = sumInPathway;
            int notInPathwayCount = getSumTotalDeg() - significantInPathwayCount;

            // Count non-significant DEGs in the pathway
            int nonSignificantInPathwayCount = getSumNonSignificantInPathway(pathwayId);
            int totalNonDEGsInPathway = sumTotalPathway - significantInPathwayCount;

            // Build output for the current pathway
            output.append(pathway.description()).append("\n | D | D* | Sum\n");
            output.append("C| ").append(significantInPathwayCount).append(" | ")
                    .append(nonSignificantInPathwayCount).append(" | ")
                    .append(significantInPathwayCount + nonSignificantInPathwayCount).append("\n");
            output.append("C*| ").append(notInPathwayCount).append(" | ")
                    .append(totalNonDEGsInPathway).append(" | ")
                    .append(notInPathwayCount + totalNonDEGsInPathway).append("\n");
            output.append("Sum | ").append(getSumTotalDeg()).append(" | ")
                    .append(totalNonDEGsInPathway + nonSignificantInPathwayCount).append(" | ")
                    .append(getSumTotalDeg() + totalNonDEGsInPathway + nonSignificantInPathwayCount).append("\n\n");
        }

        return output.toString();
    }

    protected int getSumInPathway(String pathwayId) {
        return (int) pathwayGenes.stream()
                .filter(pg -> pg.pathwayId().equals(pathwayId) && isDeg(pg.geneSymbol()))
                .filter(pg -> degs.stream().anyMatch(deg -> deg.geneSymbol().equals(pg.geneSymbol()) && deg.adjustedPValue() <= 0.01))
                .count();
    }


    protected int getSumTotalPathway(String pathwayId) {
        return (int) pathwayGenes.stream()
                .filter(pg -> pg.pathwayId().equals(pathwayId))
                .count();
    }

    protected int getSumIsSignificantDeg() {
        return (int) degs.stream()
                .filter(deg -> deg.adjustedPValue() <= 0.01)
                .count();
    }

    protected int getSumTotalDeg() {
        return degs.size();
    }

    protected boolean isDeg(String geneSymbol) {
        return degs.stream().anyMatch(deg -> deg.geneSymbol().equals(geneSymbol));
    }

    protected int getSumNonSignificantInPathway(String pathwayId) {
        return (int) pathwayGenes.stream()
                .filter(pg -> pg.pathwayId().equals(pathwayId) && isDeg(pg.geneSymbol()))
                .filter(pg -> degs.stream().noneMatch(deg -> deg.geneSymbol().equals(pg.geneSymbol()) && deg.adjustedPValue() <= 0.01))
                .count();
    }
}
