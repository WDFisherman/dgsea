package nl.bioinf.dgsea.table_outputs;

import nl.bioinf.dgsea.data_processing.Deg;
import nl.bioinf.dgsea.data_processing.Pathway;
import nl.bioinf.dgsea.data_processing.PathwayGene;

import java.util.List;

public class Table {
    protected final List<Deg> degs;
    protected final List<Pathway> pathways;
    protected final List<PathwayGene> pathwayGenes;
    private final double pval;

    public Table(List<Deg> degs, List<Pathway> pathways, List<PathwayGene> pathwayGenes, double pval) {
        this.degs = degs;
        this.pathways = pathways;
        this.pathwayGenes = pathwayGenes;
        this.pval = pval;
    }

    public String getTwoByTwoContingencyTable() throws IllegalStateException {
        if (pathways == null || degs == null || pathwayGenes == null) {
            throw new IllegalStateException("Data lists must be initialized before use.");
        }

        StringBuilder output = new StringBuilder();

        for (Pathway pathway : pathways) {
            String pathwayId = pathway.pathwayId();
            int sumInPathway = getSumInPathway(pathwayId);
            int sumTotalPathway = getSumTotalPathway(pathwayId);

            // Calculate counts for the contingency table
            int notInPathwayCount = getSumTotalDeg() - sumInPathway;

            // Count non-significant DEGs in the pathway
            int nonSignificantInPathwayCount = getSumNonSignificantInPathway(pathwayId);
            int totalNonDEGsInPathway = sumTotalPathway - sumInPathway;

            // Build output for the current pathway
            output.append(pathway.description()).append("\n | D | D* | Sum\n");
            output.append("C| ").append(sumInPathway).append(" | ")
                    .append(nonSignificantInPathwayCount).append(" | ")
                    .append(sumInPathway + nonSignificantInPathwayCount).append("\n");
            output.append("C*| ").append(notInPathwayCount).append(" | ")
                    .append(totalNonDEGsInPathway).append(" | ")
                    .append(notInPathwayCount + totalNonDEGsInPathway).append("\n");
            output.append("Sum | ").append(getSumTotalDeg()).append(" | ")
                    .append(totalNonDEGsInPathway + nonSignificantInPathwayCount).append(" | ")
                    .append(getSumTotalDeg() + totalNonDEGsInPathway + nonSignificantInPathwayCount).append("\n\n");
        }

        return output.toString();
    }

    public int getSumInPathway(String pathwayId) {
        return (int) pathwayGenes.stream()
                .filter(pg -> pg.pathwayId().equals(pathwayId) && isDeg(pg.geneSymbol()))
                .filter(pg -> degs.stream().anyMatch(deg -> deg.geneSymbol().equals(pg.geneSymbol()) && deg.adjustedPValue() <= pval))
                .count();
    }


    public int getSumTotalPathway(String pathwayId) {
        return (int) pathwayGenes.stream()
                .filter(pg -> pg.pathwayId().equals(pathwayId))
                .count();
    }

    public int getSumIsSignificantDeg() {
        return (int) degs.stream()
                .filter(deg -> deg.adjustedPValue() <= 0.01)
                .count();
    }

    public int getSumTotalDeg() {
        return degs.size();
    }

    public boolean isDeg(String geneSymbol) {
        return degs.stream().anyMatch(deg -> deg.geneSymbol().equals(geneSymbol));
    }

    public int getSumNonSignificantInPathway(String pathwayId) {
        return (int) pathwayGenes.stream()
                .filter(pg -> pg.pathwayId().equals(pathwayId) && isDeg(pg.geneSymbol()))
                .filter(pg -> degs.stream().noneMatch(deg -> deg.geneSymbol().equals(pg.geneSymbol()) && deg.adjustedPValue() <= pval))
                .count();
    }
}
