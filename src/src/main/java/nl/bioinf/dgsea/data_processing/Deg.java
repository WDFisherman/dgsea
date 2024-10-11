package nl.bioinf.dgsea.data_processing;

public record Deg(String geneSymbol, double logFoldChange, double adjustedPValue) {
}
