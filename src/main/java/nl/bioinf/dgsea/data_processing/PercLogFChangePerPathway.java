package nl.bioinf.dgsea.data_processing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

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
    private final double pval;
    Logger logger = LogManager.getLogger(PercLogFChangePerPathway.class.getName());

    /**
     * Constructs a PercLogFChangePerPathway
     * @param degs degs with at least one item and matching gene-symbols between pathwayGenes and degs
     * @param pathwayGenes pathway-gene combinations/associations with at least one item and matching gene-symbols between pathwayGenes and degs
     * @throws IllegalArgumentException if less than 1 item is present in either parameters
     */
    public PercLogFChangePerPathway(List<Deg> degs, List<PathwayGene> pathwayGenes, double pval) {
        if (degs.isEmpty()) throw new IllegalArgumentException("degs cannot be empty");
        if (pathwayGenes.isEmpty()) throw new IllegalArgumentException("pathwayGenes cannot be empty");
        this.degs = degs;
        this.pathwayGenes = pathwayGenes;
        this.pval = pval;
    }

    /**
     * Calculates percentage based on proportion average log-fold-change(lfc) of all degs in a pathway, to sum of average lfc in all pathways.
     * @throws IllegalArgumentException if pathwayIdSet is empty/not set
     * @return pathway-id, percentage-value pairs
     */
    public Map<String, Double> percAllPathways() {
        Map<String, Double> pathwayPercentages = new HashMap<>(); // could receive averages and then be modified to percentages
        Map<String, Double> avgLfcAllPathways = new HashMap<>();
        double totalLfc = getTotalLfc(avgLfcAllPathways);
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
     * @param avgLfcAllPathways map to modify and contain pathway-id; average-lfc pairs
     * @throws IllegalArgumentException if pathway-id was not found in input data, field: pathwayGenes
     * @return Sum of averages in all pathways
     */
    private double getTotalLfc(Map<String, Double> avgLfcAllPathways) {
        double totalLfcAllPathways = 0.0;
        Set<String> pathwayIdSet = pathwayGenes.stream().map(PathwayGene::pathwayId).collect(Collectors.toSet());
        for (String pathwayId : pathwayIdSet) {
            double totalLfcPathway = 0.0;
            int countDegsInPathway = 0;
            for(PathwayGene pathwayGene : pathwayGenes) {
                if(!pathwayGene.pathwayId().equals(pathwayId)) continue; // skip pathwayGene entry
                for(Deg deg : degs) {
                    if(!pathwayGene.geneSymbol().equals(deg.geneSymbol())) continue; // skip deg entry
                    if(deg.adjustedPValue() >= pval) continue;
                    totalLfcPathway += Math.abs(deg.logFoldChange());
                    countDegsInPathway++;
                }
            }
            if (countDegsInPathway == 0) logger.warn("No degs on pathway: given pathway-id={}, the pathway-genes data's gene-symbols associated by this pathway had no identical gene-symbols in the deg data. This could be either the input files or the given pathway-ids not having degs.", pathwayId);
            double avgPathway = countDegsInPathway == 0 ? 0.0 : totalLfcPathway / countDegsInPathway;
            avgLfcAllPathways.put(pathwayId, avgPathway);
            totalLfcAllPathways += avgPathway;
        }
        return totalLfcAllPathways;
    }

    public Map<String, Double> filterMostInfluentialPathways(int maxNPathways, Map<String, Double> pathwayPercentages) {
        if (maxNPathways <= 0) throw new IllegalArgumentException("maxNPathways needs to be at least 0");
        Map<String, Double> mostInfluentialPathways = new HashMap<>();
        Map.Entry<String, Double> lowestInGroup = new AbstractMap.SimpleEntry<>("", 100.0);
        for (Map.Entry<String, Double> pathwayPercentage : pathwayPercentages.entrySet()) {
            String pathwayId = pathwayPercentage.getKey();
            double percentage = pathwayPercentage.getValue();
            if (mostInfluentialPathways.size() < maxNPathways) {
                mostInfluentialPathways.put(pathwayId, percentage);
                if (percentage < lowestInGroup.getValue()) {
                    lowestInGroup = new AbstractMap.SimpleEntry<>(pathwayId, percentage);
                }
            } else {
                if (pathwayPercentages.get(pathwayId) > lowestInGroup.getValue()) {
                    mostInfluentialPathways.remove(lowestInGroup.getKey());
                    lowestInGroup = findNextLowestPathway(mostInfluentialPathways, lowestInGroup);
                    mostInfluentialPathways.put(pathwayId, pathwayPercentages.get(pathwayId));

                }
            }
        }
        return mostInfluentialPathways;
    }

    private static Map.Entry<String, Double> findNextLowestPathway(Map<String, Double> mostInfluentialPathways, Map.Entry<String, Double> lowestInGroup) {
        Map.Entry<String, Double> nextLowestInGroup = new AbstractMap.SimpleEntry<>("", 100.0);
        for (String mostInfluentialPathwayId : mostInfluentialPathways.keySet()) {
            if (mostInfluentialPathwayId.equals(lowestInGroup.getKey())) continue;  // skip current lowest value
            if (mostInfluentialPathways.get(mostInfluentialPathwayId) < nextLowestInGroup.getValue()) {
                nextLowestInGroup = new AbstractMap.SimpleEntry<>(mostInfluentialPathwayId, mostInfluentialPathways.get(mostInfluentialPathwayId));
            }
        }
        return nextLowestInGroup;
    }

}
