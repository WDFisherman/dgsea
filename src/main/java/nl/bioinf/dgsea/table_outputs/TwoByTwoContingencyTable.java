/**
 * Is responsible for: creating 2by2con table, returning 2by2con table.
 * What is 2by2con table in this context.
 */

package nl.bioinf.dgsea.table_outputs;

import nl.bioinf.dgsea.data_processing.Deg;
import nl.bioinf.dgsea.data_processing.Pathway;
import nl.bioinf.dgsea.data_processing.PathwayGene;

import java.util.*;

/**
 * An assembly class that uses data from a "differential gene set expression analysis"
 */
public class TwoByTwoContingencyTable {
    private final List<Deg> degs;
    private final List<Pathway> pathways;
    private final double pval;
    private final Map<String, List<String>> mapPathwayGenes;

    /**
     * Constructs a 2- by- 2- contingency table class using data from "differential gene set expression analysis"
     * @param degs differential expressed genes
     * @param pathways pathways with descriptions
     * @param pathwayGenes genes belonging in which pathway, gets converted to this.mapPathwayGenes
     * @param pval threshold for significance
     */
    public TwoByTwoContingencyTable(List<Deg> degs, List<Pathway> pathways, List<PathwayGene> pathwayGenes, double pval) {
        if (pathways == null || degs == null || pathwayGenes == null) {
            throw new IllegalStateException("Data lists must be initialized before use.");
        }
        this.degs = degs;
        this.pathways = pathways;
        this.mapPathwayGenes = getPathwayGeneMap(pathwayGenes);
        this.pval = pval;
    }

    /**
     * Collects count data and assembles table
     * @throws NullPointerException if this.pathways has an id that's not in this.mapPathwayGenes
     * @return table with the following structure:
     * Pathway-description (pathway-id)
     *     | D  | D*  | Sum
     * C   | 12 | 34  | 46
     * C*  | 10 | 20  | 30
     * Sum | 22 | 54  | 76
     */
    public String getTable() throws NullPointerException {
        StringBuilder output = new StringBuilder();

        for (Pathway pathway : pathways) {
            String pathwayId = pathway.pathwayId();
            Set<String> allGeneSymbols = getPathwaySpecificGeneSymbols(pathwayId, mapPathwayGenes);

            int countTotal = getCountTotal();

            int countInPathway = getCountInPathway(allGeneSymbols);
            int countNotInPathway = countTotal - countInPathway;
            int countSignificant = getCountSignificant();
            int countNotSignificant = countTotal - countSignificant;

            int countInPathwaySignificant = getCountInPathwaySignificant(allGeneSymbols); // Significant = all subtracted by not significant
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
     * @return number of degs
     */
    private int getCountInPathway(Set<String> allGeneSymbols) {
        return (int) degs.stream().filter(deg->allGeneSymbols.contains(deg.geneSymbol())).count();
    }

    /**
     * Counts degs that are both in pathway and being significant
     * @return number of degs
     */
    private int getCountInPathwaySignificant(Set<String> allGeneSymbols) {
        return (int) degs.stream().filter(deg -> deg.adjustedPValue() <= pval && allGeneSymbols.contains(deg.geneSymbol())).count();
    }

    /**
     * Collects set of geneSymbols of set pathway found in pathwayGenes
     * @param pathwayId to look up in pathwayGenes
     * @param mapPathwayGenes key:pathway, value:gene-list
     * @throws NullPointerException if pathway id was none of mapPathwayGenes keys
     * @return set of gene- symbols
     */
    private Set<String> getPathwaySpecificGeneSymbols(String pathwayId, Map<String, List<String>> mapPathwayGenes) throws NullPointerException {
        return new HashSet<>(mapPathwayGenes.get(pathwayId));
    }

    /**
     * Makes map for each pathway in pathwayGenes, with pathway-id as key
     * @return pathways map containing pahtway-id, gene-symbol pairs
     */
    private Map<String, List<String>> getPathwayGeneMap(List<PathwayGene> pathwayGenes) {
        final Map<String, List<String>> pathwayGeneMap = new HashMap<>();
        for (PathwayGene gene : pathwayGenes) {
            pathwayGeneMap
                    .computeIfAbsent(gene.pathwayId(), _ -> new ArrayList<>())
                    .add(gene.geneSymbol());
        }
        return pathwayGeneMap;
    }

}
