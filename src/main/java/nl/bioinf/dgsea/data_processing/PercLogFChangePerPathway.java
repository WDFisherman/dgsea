package nl.bioinf.dgsea.data_processing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

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
     * Calculates percentage based on proportion average log-fold-change(lfc) of all degs in a pathway, to sum of average lfc in all pathways.
     * @param pathwayIdSet Set of pathway-ids to distribute percentage under.
     * @throws IllegalArgumentException if pathwayIdSet is empty/not set
     * @return pathway-id, percentage-value pairs
     */
    public Map<String, Double> percAllPathways(Set<String> pathwayIdSet) {
        if (pathwayIdSet == null || pathwayIdSet.isEmpty()) throw new IllegalArgumentException("pathwayIdSet cannot be empty or null");
        Map<String, Double> pathwayPercentages = new HashMap<>(); // could receive averages and then be modified to percentages
        Map<String, Double> avgLfcAllPathways = new HashMap<>();
        double totalLfc = getTotalLfc(pathwayIdSet, avgLfcAllPathways);
        for (Map.Entry<String, Double> entry : avgLfcAllPathways.entrySet()) {
            String pathwayId = entry.getKey();
            Double avgLfcPathway = entry.getValue();
            if(avgLfcPathway == 0.0) {
                pathwayPercentages.put(pathwayId, 0.0);
            } else {
                pathwayPercentages.put(pathwayId, avgLfcPathway / totalLfc * 100);
            }
        }
        return pathwayPercentages;
    }

    /**
     * Calculates average log-fold-change(lfc) for every pathway in `pathwayIdSet` and saves in `avgLfcAllPathways`. Also sums all these averages and returns this value
     * @param pathwayIdSet Set of pathway-ids to calculate average lfc for
     * @param avgLfcAllPathways map to modify and contain pathway-id; average-lfc pairs
     * @throws IllegalArgumentException if pathway-id was not found in input data, field: pathwayGenes
     * @return Sum of averages in all pathways
     */
    private double getTotalLfc(Set<String> pathwayIdSet, Map<String, Double> avgLfcAllPathways) {
        double totalLfcAllPathways = 0.0;
        for (String pathwayId : pathwayIdSet) {
            double totalLfcPathway = 0.0;
            int countDegsInPathway = 0;
            for(PathwayGene pathwayGene : pathwayGenes) {
                if (!pathwayIdSet.contains(pathwayGene.pathwayId())) // escape early
                    throw new IllegalArgumentException("Pathway not found: provided pathway-id: %s was not found in the pathway-genes data".formatted(pathwayGene.pathwayId().toLowerCase()));
                if(!pathwayGene.pathwayId().equals(pathwayId)) continue; // skip pathwayGene entry
                for(Deg deg : degs) {
                    if(!pathwayGene.geneSymbol().equals(deg.geneSymbol())) continue; // skip deg entry
                    totalLfcPathway += Math.abs(deg.logFoldChange());
                    countDegsInPathway++;
                }

            }
            if (countDegsInPathway == 0) logger.warn("No degs on pathway: given pathway-id={}, the pathway-genes data's gene-symbols associated by this pathway had no identical gene-symbols in the deg data. This could be either the input files or the given pathway-ids not having degs.", pathwayId);
            double avgPathway = countDegsInPathway == 0 ? 0.0 : totalLfcPathway / countDegsInPathway;
            avgLfcAllPathways.put(pathwayId, avgPathway);
            totalLfcAllPathways += totalLfcPathway;
        }
        return totalLfcAllPathways;
    }

}
