package nl.bioinf.dgsea.data_processing;
/**
 * Represents an association between a biological pathway and a gene, including identifiers for multiple gene databases.
 *
 * @param pathwayId The unique identifier for the pathway associated with the gene.
 * @param entrezGeneId The Entrez Gene ID, a unique identifier for the gene in the NCBI database.
 * @param geneSymbol The official symbol for the gene.
 * @param ensemblGeneId The Ensembl Gene ID, a unique identifier for the gene in the Ensembl database.
 */
public record PathwayGene(String pathwayId, int entrezGeneId, String geneSymbol, String ensemblGeneId) {
}
