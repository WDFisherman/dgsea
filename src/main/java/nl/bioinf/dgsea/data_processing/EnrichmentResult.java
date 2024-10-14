package nl.bioinf.dgsea.data_processing;

public record EnrichmentResult(String pathwayId, double enrichmentScore, double pValue, double adjustedPValue) {
}
