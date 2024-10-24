package nl.bioinf.dgsea.table_outputs;

import nl.bioinf.dgsea.data_processing.Deg;
import nl.bioinf.dgsea.data_processing.Pathway;
import nl.bioinf.dgsea.data_processing.PathwayGene;
import nl.bioinf.dgsea.data_processing.EnrichmentResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to calculate and store enrichment results for gene pathways.
 */
public class EnrichmentTable {
    private final List<Pathway> pathways;
    private final List<Deg> degs;
    private final List<PathwayGene> pathwayGenes;
    private final List<EnrichmentResult> enrichmentResults;

    /**
     * Constructor for EnrichmentTable.
     *
     * @param pathways      List of pathways.
     * @param degs          List of differentially expressed genes (DEGs).
     * @param pathwayGenes  List of genes associated with pathways.
     */
    public EnrichmentTable(List<Pathway> pathways, List<Deg> degs, List<PathwayGene> pathwayGenes) {
        this.pathways = pathways;
        this.degs = degs;
        this.pathwayGenes = pathwayGenes;
        this.enrichmentResults = new ArrayList<>();
    }

    /**
     * Calculates enrichment scores and stores the results.
     */
    public void calculateEnrichment() {
        System.out.println("Pathway\tObserved DEGs\tExpected DEGs\tEnrichment Score\tP-value\tAdjusted P-value");

        for (Pathway pathway : pathways) {
            String pathwayId = pathway.pathwayId();
            String description = pathway.description();

            // Step 1: Calculate observed DEGs
            int observedDegCount = calculateObservedDegCount(pathwayId);

            // Step 2: Calculate expected DEGs (using a simple proportion approach)
            int totalGenesInPathway = countTotalGenesInPathway(pathwayId);
            double expectedDegCount = calculateExpectedDegCount(totalGenesInPathway);

            // Step 3: Calculate enrichment score (ES)
            double enrichmentScore = calculateEnrichmentScore(observedDegCount, expectedDegCount);

            // Step 4: Calculate p-value using the hypergeometric test
            double pValue = (observedDegCount > 0)
                    ? calculateHypergeometricPValue(observedDegCount, totalGenesInPathway, pathwayGenes.size(), degs.size())
                    : 1.0; // Default to 1 (not significant)

            // Step 5: Adjust p-value (Bonferroni)
            double adjustedPValue = adjustPValue(pValue);

            // Store the result in the enrichmentResults list
            enrichmentResults.add(new EnrichmentResult(pathwayId, enrichmentScore, pValue, adjustedPValue));

            // Print the result (optional, for logging purposes)
            System.out.println(description + "\t" + observedDegCount + "\t" + expectedDegCount +
                    "\t" + enrichmentScore + "\t" + pValue + "\t" + adjustedPValue);
        }
    }

    /**
     * Calculates the observed number of DEGs in the specified pathway.
     *
     * @param pathwayId ID of the pathway.
     * @return Count of observed DEGs.
     */
    public int calculateObservedDegCount(String pathwayId) {
        return (int) pathwayGenes.stream()
                .filter(pg -> pg.pathwayId().equals(pathwayId))
                .filter(pg -> isDeg(pg.geneSymbol()))
                .count();
    }

    /**
     * Counts total number of genes in the specified pathway.
     *
     * @param pathwayId ID of the pathway.
     * @return Count of total genes in the pathway.
     */
    public int countTotalGenesInPathway(String pathwayId) {
        return (int) pathwayGenes.stream()
                .filter(pg -> pg.pathwayId().equals(pathwayId))
                .count();
    }

    /**
     * Calculates the expected number of DEGs based on the total number of genes in the pathway.
     *
     * @param totalGenesInPathway Total genes in the pathway.
     * @return Expected number of DEGs.
     */
    public double calculateExpectedDegCount(int totalGenesInPathway) {
        double proportionOfDegs = degs.size() / (double) pathwayGenes.size();
        return totalGenesInPathway * proportionOfDegs;
    }

    /**
     * Calculates the enrichment score.
     *
     * @param observedDegCount Observed DEGs count.
     * @param expectedDegCount Expected DEGs count.
     * @return Enrichment score.
     */
    public double calculateEnrichmentScore(int observedDegCount, double expectedDegCount) {
        if (expectedDegCount <= 0) {
            return 0; // Prevent division by zero
        }
        if (observedDegCount == 0) {
            return 0; // Return zero if no DEGs
        }
        return (observedDegCount - expectedDegCount) / Math.sqrt(expectedDegCount);
    }

    /**
     * Checks if a gene symbol is a DEG.
     *
     * @param geneSymbol The gene symbol to check.
     * @return True if it is a DEG, false otherwise.
     */
    private boolean isDeg(String geneSymbol) {
        return degs.stream().anyMatch(deg -> deg.geneSymbol().equals(geneSymbol));
    }

    /**
     * Calculates the hypergeometric p-value.
     *
     * @param observedDegCount Number of DEGs observed in the pathway.
     * @param totalGenesInPathway Total number of genes in the pathway.
     * @param totalGenes Total number of genes in the dataset.
     * @param totalDegs Total number of DEGs in the dataset.
     * @return P-value based on hypergeometric test.
     */
    public double calculateHypergeometricPValue(int observedDegCount, int totalGenesInPathway, int totalGenes, int totalDegs) {
        if (totalGenesInPathway == 0 || totalDegs == 0) {
            return 1.0; // Cannot calculate p-value with zero genes or DEGs
        }

        double pValue = 0.0;
        for (int k = observedDegCount; k <= totalGenesInPathway; k++) {
            pValue += hypergeometricProbability(k, totalGenesInPathway, totalDegs, totalGenes);
        }

        return Double.isNaN(pValue) ? 1.0 : pValue; // If NaN, set p-value to 1
    }

    /**
     * Calculates hypergeometric probability.
     *
     * @param k Number of observed successes (DEGs in pathway).
     * @param n Number of trials (total genes in pathway).
     * @param K Number of successes in population (total DEGs).
     * @param N Total population size (total genes).
     * @return Hypergeometric probability.
     */
    public double hypergeometricProbability(int k, int n, int K, int N) {
        if (k > n || K > N || k < 0 || n < 0 || K < 0 || N < 0) {
            return 0.0; // Invalid parameters return a probability of 0
        }

        double numerator = binomialCoefficient(K, k) * binomialCoefficient(N - K, n - k);
        double denominator = binomialCoefficient(N, n);
        return numerator / denominator;
    }

    private double binomialCoefficient(int n, int k) {
        if (k > n) return 0;
        if (k == 0 || k == n) return 1;

        double coeff = 1;
        for (int i = 1; i <= k; i++) {
            coeff *= (n - i + 1);
            coeff /= i;
        }
        return coeff;
    }

    /**
     * Adjusts the p-value using the Bonferroni correction method.
     *
     * @param pValue Original p-value to adjust.
     * @return Adjusted p-value.
     */
    public double adjustPValue(double pValue) {
        int totalTests = pathways.size();
        if (Double.isNaN(pValue)) {
            return 1.0; // If NaN, set adjusted p-value to 1
        }
        return Math.min(pValue * totalTests, 1.0); // Ensure p-value does not exceed 1
    }

    /**
     * Returns the list of enrichment results.
     *
     * @return List of EnrichmentResult objects.
     */
    public List<EnrichmentResult> getEnrichmentResults() {
        return enrichmentResults;
    }
}
