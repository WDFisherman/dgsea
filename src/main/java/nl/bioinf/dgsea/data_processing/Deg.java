package nl.bioinf.dgsea.data_processing;
/**
 * Represents a Differentially Expressed Gene (DEG) with essential statistical metrics.
 * This record holds information for a gene symbol, log fold change, and an adjusted p-value.
 *
 * @param geneSymbol The unique identifier for the gene.
 * @param logFoldChange The logarithmic fold change, representing the gene expression difference.
 * @param adjustedPValue The adjusted p-value for statistical significance.
 */
public record Deg(String geneSymbol, double logFoldChange, double adjustedPValue) {
}
