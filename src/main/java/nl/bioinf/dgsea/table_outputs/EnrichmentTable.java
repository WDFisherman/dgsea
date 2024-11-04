package nl.bioinf.dgsea.table_outputs;

import nl.bioinf.dgsea.data_processing.Deg;
import nl.bioinf.dgsea.data_processing.Pathway;
import nl.bioinf.dgsea.data_processing.PathwayGene;
import nl.bioinf.dgsea.data_processing.EnrichmentResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to calculate and store enrichment results for gene pathways based on differentially expressed genes (DEGs).
 * This class supports methods for calculating enrichment scores, p-values, and adjusted p-values
 * using hypergeometric testing and Bonferroni correction.
 */
public class EnrichmentTable {
    private final List<Pathway> pathways;
    private final List<Deg> degs;
    private final List<PathwayGene> pathwayGenes;
    private final List<EnrichmentResult> enrichmentResults;
    private final Logger logger = LogManager.getLogger(EnrichmentTable.class);

    /**
     * Constructs an EnrichmentTable with the specified pathways, DEGs, and pathway-gene relationships.
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
     * Calculates enrichment scores and p-values for each pathway and stores the results in `enrichmentResults`.
     * The method applies the Bonferroni correction to adjust p-values for multiple comparisons.
     * Writes results to a CSV file specified by the outputFilePath.
     *
     * @param outputFilePath Path to the output CSV file.
     */
    public void calculateEnrichment(String outputFilePath) {
        // Write header to CSV
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            writer.write("Pathway,Observed DEGs,Expected DEGs,Enrichment Score,P-value,Adjusted P-value");
            writer.newLine();

            for (Pathway pathway : pathways) {
                String pathwayId = pathway.pathwayId();
                String description = pathway.description();

                int observedDegCount = calculateObservedDegCount(pathwayId);
                int totalGenesInPathway = countTotalGenesInPathway(pathwayId);
                double expectedDegCount = calculateExpectedDegCount(totalGenesInPathway);
                double enrichmentScore = calculateEnrichmentScore(observedDegCount, expectedDegCount);
                double pValue = (observedDegCount > 0)
                        ? calculateHypergeometricPValue(observedDegCount, totalGenesInPathway, pathwayGenes.size(), degs.size())
                        : 1.0;
                double adjustedPValue = adjustPValue(pValue);

                enrichmentResults.add(new EnrichmentResult(pathwayId, enrichmentScore, pValue, adjustedPValue));

                // Write the results to the CSV file
                writer.write(String.join(",",
                        description,
                        String.valueOf(observedDegCount),
                        String.valueOf(expectedDegCount),
                        String.valueOf(enrichmentScore),
                        String.valueOf(pValue),
                        String.valueOf(adjustedPValue)));
                writer.newLine();
            }
        } catch (IOException e) {
            logger.error("Error writing to CSV file: {}", e.getMessage());
        }
    }

        /**
         * Calculates the observed count of DEGs in the specified pathway.
         *
         * @param pathwayId ID of the pathway.
         * @return Count of observed DEGs within the pathway.
         */
        public int calculateObservedDegCount(String pathwayId) {
            return (int) pathwayGenes.stream()
                    .filter(pg -> pg.pathwayId().equals(pathwayId))
                    .filter(pg -> isDeg(pg.geneSymbol()))
                    .count();
        }

        /**
         * Counts the total number of genes associated with the specified pathway.
         *
         * @param pathwayId ID of the pathway.
         * @return Total number of genes in the pathway.
         */
        public int countTotalGenesInPathway(String pathwayId) {
            return (int) pathwayGenes.stream()
                    .filter(pg -> pg.pathwayId().equals(pathwayId))
                    .count();
        }

        /**
         * Calculates the expected number of DEGs in a pathway based on the overall proportion of DEGs.
         *
         * @param totalGenesInPathway Total number of genes in the pathway.
         * @return Expected number of DEGs in the pathway.
         */
        public double calculateExpectedDegCount(int totalGenesInPathway) {
            double proportionOfDegs = degs.size() / (double) pathwayGenes.size();
            return totalGenesInPathway * proportionOfDegs;
        }

        /**
         * Calculates the enrichment score for a pathway based on observed and expected DEG counts.
         *
         * @param observedDegCount Observed number of DEGs.
         * @param expectedDegCount Expected number of DEGs.
         * @return Calculated enrichment score.
         */
        public double calculateEnrichmentScore(int observedDegCount, double expectedDegCount) {
            if (expectedDegCount <= 0) {
                return 0;
            }
            if (observedDegCount == 0) {
                return 0;
            }
            return (observedDegCount - expectedDegCount) / Math.sqrt(expectedDegCount);
        }

        /**
         * Determines if a gene is a DEG based on its gene symbol.
         *
         * @param geneSymbol Gene symbol to check.
         * @return True if the gene is a DEG; false otherwise.
         */
        private boolean isDeg(String geneSymbol) {
            return degs.stream().anyMatch(deg -> deg.geneSymbol().equals(geneSymbol));
        }

        /**
         * Calculates the hypergeometric p-value for observing a specific number of DEGs in a pathway.
         *
         * @param observedDegCount Number of observed DEGs.
         * @param totalGenesInPathway Total genes in the pathway.
         * @param totalGenes Total genes in the dataset.
         * @param totalDegs Total DEGs in the dataset.
         * @return Calculated p-value.
         */
        public double calculateHypergeometricPValue(int observedDegCount, int totalGenesInPathway, int totalGenes, int totalDegs) {
            if (totalGenesInPathway == 0 || totalDegs == 0) {
                return 1.0;
            }

            double pValue = 0.0;
            for (int k = observedDegCount; k <= totalGenesInPathway; k++) {
                pValue += hypergeometricProbability(k, totalGenesInPathway, totalDegs, totalGenes);
            }

            return Double.isNaN(pValue) ? 1.0 : pValue;
        }

        /**
         * Calculates the hypergeometric probability for observing a certain number of DEGs in a pathway.
         *
         * @param k Number of observed DEGs.
         * @param n Total genes in the pathway.
         * @param K Total DEGs in the dataset.
         * @param N Total genes in the dataset.
         * @return Calculated hypergeometric probability.
         */
        public double hypergeometricProbability(int k, int n, int K, int N) {
            if (k > n || K > N || k < 0 || n < 0 || K < 0 || N < 0) {
                return 0.0;
            }

            double numerator = binomialCoefficient(K, k) * binomialCoefficient(N - K, n - k);
            double denominator = binomialCoefficient(N, n);
            return numerator / denominator;
        }

        /**
         * Calculates the binomial coefficient, used in hypergeometric probability calculations.
         *
         * @param n Total items.
         * @param k Chosen items.
         * @return Binomial coefficient value.
         */
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
         * Adjusts a p-value using the Bonferroni correction.
         *
         * @param pValue Original p-value.
         * @return Adjusted p-value.
         */
        public double adjustPValue(double pValue) {
            int totalTests = pathways.size();
            if (Double.isNaN(pValue)) {
                return 1.0;
            }
            return Math.min(pValue * totalTests, 1.0);
        }

        /**
         * Retrieves the list of enrichment results.
         *
         * @return List of {@link EnrichmentResult} objects.
         */
        public List<EnrichmentResult> getEnrichmentResults() {
            return enrichmentResults;
        }
    }
