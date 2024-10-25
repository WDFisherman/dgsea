package nl.bioinf.dgsea.data_processing;
/**
 * Represents the result of an enrichment analysis for a specific pathway, containing relevant statistical scores.
 * This record holds information for a pathway identifier, enrichment score, p-value, and an adjusted p-value.
 *
 * @param pathwayId The unique identifier for the pathway.
 * @param enrichmentScore The score indicating the degree of enrichment for the pathway.
 * @param pValue The p-value for the enrichment score's statistical significance.
 * @param adjustedPValue The adjusted p-value to account for multiple testing.
 */
public record EnrichmentResult(String pathwayId, double enrichmentScore, double pValue, double adjustedPValue) {
}
