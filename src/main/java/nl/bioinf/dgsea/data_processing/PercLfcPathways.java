/**
 * Responsible for calculating data for LfcBarChart.
 * @Authur Willem Daniël Visser
 */
package nl.bioinf.dgsea.data_processing;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Calculates data for LfcBarChart. <br></br>
 * Explanation algorithm/formula: Calculates the percentage of average expression change, for every differently expressed gene(deg), by 1 or more pathways.
 * The result should indicate a rough estimate of the size of the differential expression overarching a pathway.
 * Which is determent by taking the average log-fold-change for every pathway, using absolute values.
 * Followed by scaling this into a percentage of average change.
 * A high value associated with a pathway, means the genes contribute a lot of change on average. A low value does the opposite.
 */
public class PercLfcPathways {
    private final List<Deg> degs;
    private final List<PathwayGene> pathwayGenes;

    /**
     * Constructs a PercLogFChangePerPathway
     * @param degs degs with at least one item and matching gene-symbols between pathwayGenes and degs
     * @param pathwayGenes pathway-gene combinations/associations with at least one item and matching gene-symbols between pathwayGenes and degs
     * @throws IllegalArgumentException if less than 1 item is present in either parameters
     */
    public PercLfcPathways(List<Deg> degs, List<PathwayGene> pathwayGenes) {
        if (degs.isEmpty()) throw new IllegalArgumentException("degs cannot be empty");
        if (pathwayGenes.isEmpty()) throw new IllegalArgumentException("pathwayGenes cannot be empty");
        this.degs = degs;
        this.pathwayGenes = pathwayGenes;
    }

    /**
     * Calculates percentage based on proportion average log-fold-change(lfc) of all degs in a pathway, to sum of average lfc in all pathways.
     * @param pathwayIds Set of pathway-ids to distribute percentage under.
     * @throws IllegalArgumentException if pathwayIds is empty/not set
     * @return pathway-id, percentage-value pairs
     */
    public double[] percAllPathways(String[] pathwayIds) throws IllegalArgumentException {
        if (pathwayIds == null || pathwayIds.length == 0) throw new IllegalArgumentException("pathwayIds cannot be empty or null");
        double[] pathwayPercentages = new double[pathwayIds.length]; // could receive averages and then be modified to percentages
        final double[]  avgLfcAllPathways = new double[pathwayIds.length];
        double totalLfc = getTotalLfc(pathwayIds, avgLfcAllPathways);
        int pathwayIndex = 0;
        for (String _ : pathwayIds) {
            double avgLfcPathway = avgLfcAllPathways[pathwayIndex];
            if(avgLfcPathway == 0.0) {
                pathwayPercentages[pathwayIndex] = 0.0;
            } else {
                pathwayPercentages[pathwayIndex] = avgLfcPathway / totalLfc * 100;
            }
            pathwayIndex++;
        }
        return pathwayPercentages;
    }

    /**
     * Calculates average log-fold-change(lfc) for every pathway in `pathwayIds` and saves in `avgLfcAllPathways`. Also sums all these averages and returns this value
     * @param pathwayIds Set of pathway-ids to calculate average lfc for
     * @param avgLfcAllPathways map to modify and contain pathway-id; average-lfc pairs
     * @throws IllegalArgumentException if pathway-id was not found in input data, field: pathwayGenes
     * @return Sum of averages in all pathways
     */
    private double getTotalLfc(String[] pathwayIds, double[] avgLfcAllPathways) throws IllegalArgumentException {
        double totalLfcAllPathways = 0.0;
        int pathwayIndex = 0;
        final Map<String, List<PathwayGene>> pathwayGeneMap = getPathwayGeneMap();
        final Map<String, Deg> degMap = getDegMap();

        for (String pathwayId : pathwayIds) {
            double totalLfcPathway = 0.0;
            int countDegsInPathway = 0;
            List<PathwayGene> genesForPathway = pathwayGeneMap.getOrDefault(pathwayId, Collections.emptyList());
            if (genesForPathway.isEmpty())
                throw new IllegalArgumentException("Pathway not found: provided pathway-id: '%s' was not found in the pathway-genes data".formatted(pathwayId.toLowerCase()));
            // Only iterate over the genes that belong to the current pathwayId
            for (PathwayGene gene : genesForPathway) {
                if (!degMap.containsKey(gene.geneSymbol())) continue;
                Deg deg = degMap.get(gene.geneSymbol());
                totalLfcPathway += Math.abs(deg.logFoldChange());
                countDegsInPathway++;
            }
            double avgPathway = countDegsInPathway == 0 ? 0.0 : totalLfcPathway / countDegsInPathway;
            avgLfcAllPathways[pathwayIndex] = avgPathway;
            totalLfcAllPathways += totalLfcPathway;
            pathwayIndex++;
        }
        return totalLfcAllPathways;
    }

    /**
     * Make map for each deg in degs, with geneSymbol as key.
     * @return degs map containing gene-symbol, deg-entry pairs
     */
    private Map<String, Deg> getDegMap() {
        final Map<String, Deg> degMap = new HashMap<>();
        for (Deg deg : degs) {
            degMap.put(deg.geneSymbol(), deg);
        }
        return degMap;
    }

    /**
     * Makes map for each pathway in pathwayGenes, with pathway-id as key
     * @return pathways map containing pahtway-id, pathwayGene-entry pairs
     */
    private Map<String, List<PathwayGene>> getPathwayGeneMap() {
        final Map<String, List<PathwayGene>> pathwayGeneMap = new HashMap<>();
        for (PathwayGene gene : pathwayGenes) {
            pathwayGeneMap
                    .computeIfAbsent(gene.pathwayId(), _ -> new ArrayList<>())
                    .add(gene);
        }
        return pathwayGeneMap;
    }

    /**
     * Filter maxNPathwys highest percentages in pathwayPercentages. Connects pathwayIds to percentages.
     * @param maxNPathways top this many highest percentage-amounts
     * @param pathwayPercentages percentage-amounts
     * @param pathwayIds keeps sorted and filtered values connected to their pathways
     * @return map containing pathway-id, percentage pairs.
     */
    public Map<String, Double> filterMostInfluentialPathways(int maxNPathways, double[] pathwayPercentages, String[] pathwayIds) {
        if (maxNPathways <= 0) throw new IllegalArgumentException("maxNPathways needs to be at least 0");
        Map<String, Double> lfcAllPathways = IntStream.range(0, pathwayPercentages.length).boxed()
                .collect(Collectors.toMap(i -> pathwayIds[i], i -> pathwayPercentages[i]));
        return lfcAllPathways.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(maxNPathways)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, _) -> e1, HashMap::new));
    }


}