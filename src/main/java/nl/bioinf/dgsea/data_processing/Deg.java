package nl.bioinf.degs.data_processing;

public record Deg(String geneSymbol, double logFoldChange, double adjustedPValue) {
}

