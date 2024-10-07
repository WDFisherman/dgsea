package nl.bioinf.dgsea.table_outputs;

import nl.bioinf.dgsea.data_processing.Deg;
import nl.bioinf.dgsea.data_processing.Pathway;
import nl.bioinf.dgsea.data_processing.PathwayGene;

import java.util.List;

public class EnrichmentTable {
    private final List<Pathway> pathways;
    private final List<Deg> degs;
    private final List<PathwayGene> pathwayGenes;

    public EnrichmentTable(List<Pathway> pathways, List<Deg> degs, List<PathwayGene> pathwayGenes) {
        this.pathways = pathways;
        this.degs = degs;
        this.pathwayGenes = pathwayGenes;
    }

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

            // Step 4: Calculate p-value and adjusted p-value
            double pValue = calculatePValue(observedDegCount, expectedDegCount, totalGenesInPathway);
            double adjustedPValue = adjustPValue(pValue);

            // Print the result
            System.out.println(description + "\t" + observedDegCount + "\t" + expectedDegCount +
                    "\t" + enrichmentScore + "\t" + pValue + "\t" + adjustedPValue);
        }
    }

    private int calculateObservedDegCount(String pathwayId) {
        // Count how many DEGs are in this pathway
        return (int) pathwayGenes.stream()
                .filter(pg -> pg.pathwayId().equals(pathwayId))
                .filter(pg -> isDeg(pg.geneSymbol()))
                .count();
    }

    private int countTotalGenesInPathway(String pathwayId) {
        // Count total genes in the pathway
        return (int) pathwayGenes.stream()
                .filter(pg -> pg.pathwayId().equals(pathwayId))
                .count();
    }

    private double calculateExpectedDegCount(int totalGenesInPathway) {
        // Calculate expected DEGs by chance: proportion of DEGs in the total gene pool
        double proportionOfDegs = degs.size() / (double) pathwayGenes.size(); // Proportion of DEGs in total genes
        return totalGenesInPathway * proportionOfDegs;
    }

    private double calculateEnrichmentScore(int observedDegCount, double expectedDegCount) {
        // Enrichment Score = (Observed - Expected) / sqrt(Expected)
        if (expectedDegCount == 0) {
            return 0; // Handle division by zero if expected count is zero
        }
        return (observedDegCount - expectedDegCount) / Math.sqrt(expectedDegCount);
    }

    private boolean isDeg(String geneSymbol) {
        // Check if a gene is in the list of DEGs
        return degs.stream().anyMatch(deg -> deg.geneSymbol().equals(geneSymbol));
    }

    private double calculatePValue(int observedDegCount, double expectedDegCount, int totalGenesInPathway) {
        // A  p-value calculation, using a binomial test
        double pValue;
        if (expectedDegCount == 0) {
            return 1.0; // If no expected DEGs, set p-value to 1.0 (not significant)
        }

        double successProb = expectedDegCount / totalGenesInPathway;
        pValue = 1.0; // Initialize p-value

        // Calculate the p-value using the binomial formula
        for (int k = observedDegCount; k <= totalGenesInPathway; k++) {
            double binomialProb = binomialProbability(totalGenesInPathway, k, successProb);
            pValue -= binomialProb; // Accumulate the tail probability
        }

        return Math.max(pValue, 0.0); // Ensure p-value is not negative
    }

    private double binomialProbability(int n, int k, double p) {
        // Calculate binomial probability P(X = k) = C(n, k) * (p^k) * (1-p)^(n-k)
        double binomCoefficient = binomialCoefficient(n, k);
        return binomCoefficient * Math.pow(p, k) * Math.pow(1 - p, n - k);
    }

    private double binomialCoefficient(int n, int k) {
        // Calculate binomial coefficient C(n, k)
        if (k > n) return 0;
        if (k == 0 || k == n) return 1;

        double coeff = 1;
        for (int i = 1; i <= k; i++) {
            coeff *= (n - i + 1);
            coeff /= i;
        }
        return coeff;
    }

    private double adjustPValue(double pValue) {
        // Adjust p-value (Bonferroni correction as an example)
        int totalTests = pathways.size();
        return Math.min(pValue * totalTests, 1.0); // Bonferroni correction
    }
}

//