package nl.bioinf.dgsea.data_processing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates the percentage of average expression change, for every differently expressed gene(deg), by 1 or more pathways.
 * The result should indicate a rough estimate of the size of the differential expression overarching a pathway.
 * Which is determent by taking the average log-fold-change for every pathway, using absolute values.
 * Followed by scaling this into a percentage of average change.
 * A high value associated with a pathway, means the genes contribute a lot of change on average. A low value does the opposite.
 */
public class PercLogFChangePerPathway {
    private final List<Deg> degs;
    private final List<PathwayGene> pathwayGenes;
    Logger logger = LogManager.getLogger(PercLogFChangePerPathway.class.getName());

    /**
     * Constructs a PercLogFChangePerPathway
     * @param degs degs with at least one item and matching gene-symbols between pathwayGenes and degs
     * @param pathwayGenes pathway-gene combinations/associations with at least one item and matching gene-symbols between pathwayGenes and degs
     * @throws IllegalArgumentException if less than 1 item is present in either parameters
     */
    public PercLogFChangePerPathway(List<Deg> degs, List<PathwayGene> pathwayGenes) {
        if (degs.isEmpty()) throw new IllegalArgumentException("degs cannot be empty");
        if (pathwayGenes.isEmpty()) throw new IllegalArgumentException("pathwayGenes cannot be empty");
        this.degs = degs;
        this.pathwayGenes = pathwayGenes;
    }

    /**
     * Calculates average log-fold-change on degs in a particular pathway.
     * @param pathwayId organism pathway id, must be in `pathwayGenes`
     * @return average log-fold-change
     */
    private double getAveragePathway(String pathwayId) {
        double[] totalLogFChangePathway = new double[]{0.0}; // --
        int[] countDegs = {0}; // circumvent lambda issue --
        List<PathwayGene> pathwayGeneList = pathwayGenes.stream().filter(v->v.pathwayId().equals(pathwayId)).toList();
        if (pathwayGeneList.isEmpty()) throw new RuntimeException("Given pathway could not be found in the pathway-genes file.");
        List<Deg> degList = getDegList(pathwayGeneList);
        if (!degList.isEmpty()) {
            degList.forEach(v-> {
                totalLogFChangePathway[0] = totalLogFChangePathway[0] + Math.abs(v.logFoldChange()); // Absolute, to prevent negative values from cancelling out positive ones, lowering the perceived differential expression
                countDegs[0] += 1;
            });
            return totalLogFChangePathway[0] / countDegs[0];
        } else {
            return 0.0;
        }
    }

    /**
     * Get list of degs also found in the gene-symbols of pathways in pathwayGeneList.
     * @param pathwayGeneList a list with geneSymbol matching with gene-symbol in degs
     * @return list of deg-items
     */
    private List<Deg> getDegList(List<PathwayGene> pathwayGeneList) {
        List<Deg> degList = new ArrayList<>();
        for (PathwayGene pathwayGene : pathwayGeneList) {
            List<Deg> newDegList = degs.stream().filter(v->v.geneSymbol().equals(pathwayGene.geneSymbol())).toList();
            if (!newDegList.isEmpty()) degList.add(newDegList.getFirst());
        }
        if (degList.isEmpty()) logger.info("None of given gene-symbols in pathway-genes file where pathway-id={} could be found in the degs file or vice versa. This could be either the input files or the given pathway-ids not having degs.", pathwayGeneList.getFirst().pathwayId());
        return degList;
    }

    /**
     * Collects average log-fold-change(lfc) on degs in all given pathway's
     * @param pathwayIdArray multiple organism pathway ids, must be common in `pathways` and `pathwayGenes`
     * @return pathway-id and average lfc on that pathway
     */
    private Map<String, Double> getAvgAllPathways(String[] pathwayIdArray) {
        Map<String, Double> averageLogFChangeAllPathways = new HashMap<>();
        for (String pathwayId : pathwayIdArray) {
            averageLogFChangeAllPathways.put(pathwayId, getAveragePathway(pathwayId));
        }
        return averageLogFChangeAllPathways;
    }

    /**
     * Gives percentage average deg- log-fold-change(lfc) per pathway (see class description)
     * @param pathwayIdArray multiple organism pathway ids, must be common in `pathways` and `pathwayGenes`
     * @return pathway-id and percentage lfc on that pathway (relative to total lfc by all pathways)
     * @throws RuntimeException if pathway-id in pathwayIdArray does not match any pathway-id in pathwayGene or degs.
     * The same error can accor when gene-symbol in pathwayGene, does not match any gene-symbol in degs or vice versa.
     */
    public Map<String, Double> getPercAllPathways(String[] pathwayIdArray) {
        Map<String, Double> avgAllPathways = getAvgAllPathways(pathwayIdArray);
        Map<String, Double> percLogFChangeAllPathways = new HashMap<>();
        double totalLogFChange = getTotalPathway(avgAllPathways);
        for (String pathwayId : avgAllPathways.keySet()) {
            percLogFChangeAllPathways.put(pathwayId, (avgAllPathways.get(pathwayId) / totalLogFChange * 100));
        }
        return percLogFChangeAllPathways;
    }

    /**
     * Sums total of all log-fold-change(lfc) averages in all pathways
     * @param averageLogFChangePathway multiple organism pathway ids, must be common in `pathways` and `pathwayGenes` and average lfc's of those pathways
     * @return total value
     */
    private double getTotalPathway(Map<String, Double> averageLogFChangePathway) {
        double totalLogFChange;
        totalLogFChange = averageLogFChangePathway.values().stream().mapToDouble(Double::doubleValue).sum();
        return totalLogFChange;
    }
}
