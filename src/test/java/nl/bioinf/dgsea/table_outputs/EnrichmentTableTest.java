package nl.bioinf.dgsea.table_outputs;

import nl.bioinf.dgsea.data_processing.Deg;
import nl.bioinf.dgsea.data_processing.Pathway;
import nl.bioinf.dgsea.data_processing.PathwayGene;
import nl.bioinf.dgsea.data_processing.EnrichmentResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the EnrichmentTable class.
 */
public class EnrichmentTableTest {
    private static final String PATHWAY1_ID = "pathway1";
    private static final String PATHWAY2_ID = "pathway2";
    private static final String PATHWAY3_ID = "pathway3";

    private static final Deg DEG1 = new Deg("GENE1", 2.0, 0.01);
    private static final Deg DEG2 = new Deg("GENE2", -1.5, 0.03);
    private static final Deg DEG3 = new Deg("GENE3", 1.0, 0.05);

    private static final PathwayGene PATHWAY_GENE1 = new PathwayGene(PATHWAY1_ID, 1, "GENE1", "ENSG00000123456");
    private static final PathwayGene PATHWAY_GENE2 = new PathwayGene(PATHWAY1_ID, 2, "GENE2", "ENSG00000123457");
    private static final PathwayGene PATHWAY_GENE3 = new PathwayGene(PATHWAY2_ID, 3, "GENE1", "ENSG00000123456");
    private static final PathwayGene PATHWAY_GENE4 = new PathwayGene(PATHWAY3_ID, 4, "GENE3", "ENSG00000123458");
    private static final PathwayGene PATHWAY_GENE5 = new PathwayGene(PATHWAY3_ID, 5, "GENE4", "ENSG00000123459");

    private List<Pathway> pathways;
    private List<Deg> degs;
    private List<PathwayGene> pathwayGenes;
    private EnrichmentTable enrichmentTable;

    @BeforeEach
    public void setUp() {
        pathways = Arrays.asList(
                new Pathway(PATHWAY1_ID, "Glycolysis / Gluconeogenesis"),
                new Pathway(PATHWAY2_ID, "Citrate cycle (TCA cycle)"),
                new Pathway(PATHWAY3_ID, "Fatty acid biosynthesis")
        );

        degs = Arrays.asList(DEG1, DEG2, DEG3);

        pathwayGenes = Arrays.asList(PATHWAY_GENE1, PATHWAY_GENE2, PATHWAY_GENE3, PATHWAY_GENE4, PATHWAY_GENE5);

        enrichmentTable = new EnrichmentTable(pathways, degs, pathwayGenes);
    }

    @Test
    public void testCalculateObservedDegCount() {
        int observedCount = enrichmentTable.calculateObservedDegCount(PATHWAY1_ID);
        assertEquals(2, observedCount, "Expected 2 observed DEGs in pathway1.");
    }

    @Test
    public void testCountTotalGenesInPathway() {
        int totalCount = enrichmentTable.countTotalGenesInPathway(PATHWAY1_ID);
        assertEquals(2, totalCount, "Expected 2 total genes in pathway1.");
    }

    @Test
    public void testCalculateExpectedDegCount() {
        double expectedCount = enrichmentTable.calculateExpectedDegCount(2);
        assertEquals(1.2, expectedCount, 0.01, "Expected count based on proportions should be approximately 1.2.");
    }

    @Test
    public void testCalculateEnrichmentScore() {
        double enrichmentScore = enrichmentTable.calculateEnrichmentScore(2, 1.0);
        assertEquals(1.0, enrichmentScore, 0.01, "Enrichment score should be 1.0.");
    }

    @Test
    public void testCalculateHypergeometricPValue() {
        double pValue = enrichmentTable.calculateHypergeometricPValue(2, 2, 5, 3); // 5 total genes, 3 DEGs
        assertTrue(pValue >= 0 && pValue <= 1, "P-value should be between 0 and 1.");
    }

    @Test
    public void testHypergeometricProbability() {
        double probability = enrichmentTable.hypergeometricProbability(2, 2, 3, 5); // 3 DEGs in 5 total genes
        assertTrue(probability >= 0 && probability <= 1, "Probability should be between 0 and 1.");
    }

    @Test
    public void testAdjustPValue() {
        double adjustedPValue = enrichmentTable.adjustPValue(0.05);
        assertEquals(0.15, adjustedPValue, 0.01, "Expected adjusted p-value should be 0.15.");
    }

    @Test
    public void testCalculateEnrichment() {
        enrichmentTable.calculateEnrichment();
        List<EnrichmentResult> results = enrichmentTable.getEnrichmentResults();

        assertEquals(3, results.size(), "Expected 3 enrichment results for pathways.");
        assertEquals(PATHWAY1_ID, results.get(0).pathwayId(), "First pathway should be pathway1.");
    }

    @Test
    public void testEmptyInputLists() {
        enrichmentTable = new EnrichmentTable(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        enrichmentTable.calculateEnrichment();
        List<EnrichmentResult> results = enrichmentTable.getEnrichmentResults();

        assertTrue(results.isEmpty(), "Expected no results for empty input lists.");
    }

    @Test
    public void testNoObservedDegs() {
        List<Deg> noDegs = Arrays.asList();
        enrichmentTable = new EnrichmentTable(pathways, noDegs, pathwayGenes);
        enrichmentTable.calculateEnrichment();

        List<EnrichmentResult> results = enrichmentTable.getEnrichmentResults();
        for (EnrichmentResult result : results) {
            assertEquals(0.0, result.enrichmentScore(), 0.01, "Expected enrichment score to be 0 with no DEGs.");
            assertEquals(1.0, result.pValue(), 0.01, "Expected p-value to be 1 with no DEGs.");
        }
    }

    @Test
    public void testOverlappingPathwayGenes() {
        pathwayGenes = Arrays.asList(
                new PathwayGene(PATHWAY1_ID, 1, "GENE1", "ENSG00000123456"),
                new PathwayGene(PATHWAY2_ID, 2, "GENE1", "ENSG00000123456") // GENE1 is in both pathways
        );
        enrichmentTable = new EnrichmentTable(pathways, degs, pathwayGenes);
        enrichmentTable.calculateEnrichment();

        List<EnrichmentResult> results = enrichmentTable.getEnrichmentResults();
        assertFalse(results.isEmpty(), "Expected results to be calculated despite overlapping pathway genes.");
    }
}
