/**
 * Is responsible for: creating 2by2con table, returning 2by2con table.
 * What is 2by2con table in this context.
 */

package nl.bioinf.dgsea.table_outputs;

import nl.bioinf.dgsea.data_processing.Deg;
import nl.bioinf.dgsea.data_processing.Pathway;
import nl.bioinf.dgsea.data_processing.PathwayGene;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An assembly class that uses data from a "differential gene set expression analysis"
 */
public class TwoByTwoContingencyTable {
    private final List<Deg> degs;
    private final List<Pathway> pathways;
    private final List<PathwayGene> pathwayGenes;
    private final double pval;

    /**
     * Constructs a 2- by- 2- contingency table class using data from "differential gene set expression analysis"
     * @param degs differential expressed genes
     * @param pathways pathways with descriptions
     * @param pathwayGenes genes belonging in which pathway
     * @param pval threshold for significance
     */
    public TwoByTwoContingencyTable(List<Deg> degs, List<Pathway> pathways, List<PathwayGene> pathwayGenes, double pval) {
        if (pathways == null || degs == null || pathwayGenes == null) {
            throw new IllegalStateException("Data lists must be initialized before use.");
        }
        this.degs = degs;
        this.pathways = pathways;
        this.pathwayGenes = pathwayGenes;
        this.pval = pval;
    }

    /**
     * Collects count data and assembles table
     * @return table with the following structure:
     * Pathway-description (pathway-id)
     *     | D  | D*  | Sum
     * C   | 12 | 34  | 46
     * C*  | 10 | 20  | 30
     * Sum | 22 | 54  | 76
     */
    public String getTable() {
        StringBuilder output = new StringBuilder();

        for (Pathway pathway : pathways) {
            String pathwayId = pathway.pathwayId();
            int countTotal = getCountTotal();

            int countInPathway = getCountInPathway(pathwayId);
            int countNotInPathway = countTotal - countInPathway;

            int countSignificant = getCountSignificant();
            int countNotSignificant = countTotal - countSignificant;

            int countInPathwaySignificant = getCountInPathwaySignificant(pathwayId); // Significant = all subtracted by not significant
            int countNotInPathwaySignificant = countSignificant - countInPathwaySignificant;

            int countInPathwayNotSignificant = countInPathway - countInPathwaySignificant;
            int countNotInPathwayNotSignificant = countNotInPathway - countNotInPathwaySignificant;


            output.append("""

                    %s (%s)
                    \t | D\t | D*\t | Sum
                    ----------------------
                    C\t | %s\t | %s\t | %s
                    C*\t | %s\t | %s\t | %s
                    Sum\t | %s\t | %s\t | %s
                    """.formatted(
                    pathway.description(), pathway.pathwayId(),
                    countInPathwaySignificant, countInPathwayNotSignificant, countInPathway,
                    countNotInPathwaySignificant, countNotInPathwayNotSignificant, countNotInPathway,
                    countSignificant, countNotSignificant, countTotal
            ));
        }
        output.append("\nD=is.. D*=is not.., Significant deg C=in.. C*=not in.., ..pathway.");
        return output.toString();
    }

    /**
     * Counts any deg
     * @return number of degs
     */
    private int getCountTotal() {
        return degs.size();
    }

    /**
     * Counts degs that are significant
     * @return number of degs
     */
    private int getCountSignificant() {
        return (int) degs.stream().filter(deg->deg.adjustedPValue() <= pval).count();
    }

    /**
     * Counts degs that are present in set pathway.
     * @param pathwayId to look up in pathwayGenes
     * @return number of degs
     */
    private int getCountInPathway(String pathwayId) {
        Set<String> allGeneSymbols = getAllGeneSymbols(pathwayId);
        return (int) degs.stream().filter(deg->allGeneSymbols.contains(deg.geneSymbol())).count();
    }

    /**
     * Counts degs that are both in pathway and being significant
     * @param pathwayId to look up in pathwayGenes
     * @return number of degs
     */
    private int getCountInPathwaySignificant(String pathwayId) {
        Set<String> allGeneSymbols = getAllGeneSymbols(pathwayId);
        return (int) degs.stream().filter(deg -> deg.adjustedPValue() <= pval && allGeneSymbols.contains(deg.geneSymbol())).count();
    }

    /**
     * Collects set of geneSymbols of pathway found in pathwayGenes
     * @param pathwayId to look up in pathwayGenes
     * @return set of gene- symbols
     */
    private Set<String> getAllGeneSymbols(String pathwayId) {
        return pathwayGenes.stream().filter(pg->pg.pathwayId().equals(pathwayId)).map(PathwayGene::geneSymbol).collect(Collectors.toSet());
    }

}
