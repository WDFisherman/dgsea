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

public class EnrichmentTableTest {
    private List<Pathway> pathways;
    private List<Deg> degs;
    private List<PathwayGene> pathwayGenes;
    private EnrichmentTable enrichmentTable;

    @BeforeEach
    public void setUp() {
        // Setup mock data for testing
        pathways = Arrays.asList(
                new Pathway("pathway1", "Glycolysis / Gluconeogenesis"),
                new Pathway("pathway2", "Citrate cycle (TCA cycle)"),
                new Pathway("pathway3", "Fatty acid biosynthesis")
        );

        degs = Arrays.asList(
                new Deg("GENE1", 2.0, 0.01),
                new Deg("GENE2", -1.5, 0.03),
                new Deg("GENE3", 1.0, 0.05)
        );

        pathwayGenes = Arrays.asList(
                new PathwayGene("pathway1", 1, "GENE1", "ENSG00000123456"), // Aangepast voor de constructor
                new PathwayGene("pathway1", 2, "GENE2", "ENSG00000123457"), // Aangepast voor de constructor
                new PathwayGene("pathway2", 3, "GENE1", "ENSG00000123456"), // Aangepast voor de constructor
                new PathwayGene("pathway3", 4, "GENE3", "ENSG00000123458"), // Aangepast voor de constructor
                new PathwayGene("pathway3", 5, "GENE4", "ENSG00000123459")  // Aangepast voor de constructor
        );

        enrichmentTable = new EnrichmentTable(pathways, degs, pathwayGenes);
    }

    @Test
    public void testCalculateObservedDegCount() {
        int observedCount = enrichmentTable.calculateObservedDegCount("pathway1");
        assertEquals(2, observedCount); // GENE1 en GENE2 zijn DEGs
    }

    @Test
    public void testCountTotalGenesInPathway() {
        int totalCount = enrichmentTable.countTotalGenesInPathway("pathway1");
        assertEquals(2, totalCount); // Pathway1 heeft 2 genen
    }

    @Test
    public void testCalculateExpectedDegCount() {
        double expectedCount = enrichmentTable.calculateExpectedDegCount(2);
        assertEquals(1.2, expectedCount, 0.01); // Verwachte DEGs op basis van de proportie
    }

    @Test
    public void testCalculateEnrichmentScore() {
        double enrichmentScore = enrichmentTable.calculateEnrichmentScore(2, 1.0);
        assertEquals(1.0, enrichmentScore, 0.01); // Controleer de enrichment score
    }

    @Test
    public void testCalculateHypergeometricPValue() {
        double pValue = enrichmentTable.calculateHypergeometricPValue(2, 2, 5, 3); // 5 totale genen, 3 DEGs
        assertTrue(pValue >= 0 && pValue <= 1); // P-waarde moet tussen 0 en 1 liggen
    }

    @Test
    public void testHypergeometricProbability() {
        double probability = enrichmentTable.hypergeometricProbability(2, 2, 3, 5); // 3 DEGs in 5 totale genen
        assertTrue(probability >= 0 && probability <= 1); // Controleer of de kans correct is
    }

    @Test
    public void testAdjustPValue() {
        double adjustedPValue = enrichmentTable.adjustPValue(0.05);
        assertEquals(0.15, adjustedPValue, 0.01); // Verwachte waarde na aanpassing
    }

    @Test
    public void testCalculateEnrichment() {
        enrichmentTable.calculateEnrichment();
        List<EnrichmentResult> results = enrichmentTable.getEnrichmentResults();

        assertEquals(3, results.size()); // We hebben 3 pathways
        assertEquals("pathway1", results.get(0).pathwayId()); // Controleer de eerste pathway
    }
    @Test
    public void testEmptyInputLists() {
        enrichmentTable = new EnrichmentTable(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        enrichmentTable.calculateEnrichment();
        List<EnrichmentResult> results = enrichmentTable.getEnrichmentResults();

        assertTrue(results.isEmpty()); // Geen resultaten als er geen pathways of DEGs zijn
    }
    @Test
    public void testNoObservedDegs() {
        List<Deg> noDegs = Arrays.asList();
        enrichmentTable = new EnrichmentTable(pathways, noDegs, pathwayGenes);
        enrichmentTable.calculateEnrichment();

        List<EnrichmentResult> results = enrichmentTable.getEnrichmentResults();
        for (EnrichmentResult result : results) {
            assertEquals(0.0, result.enrichmentScore(), 0.01); // Geen DEGs betekent een enrichment score van 0
            assertEquals(1.0, result.pValue(), 0.01); // P-waarde zou 1 moeten zijn (niet significant)
        }
    }
    @Test
    public void testOverlappingPathwayGenes() {
        pathwayGenes = Arrays.asList(
                new PathwayGene("pathway1", 1, "GENE1", "ENSG00000123456"),
                new PathwayGene("pathway2", 2, "GENE1", "ENSG00000123456") // GENE1 zit in beide pathways
        );
        enrichmentTable = new EnrichmentTable(pathways, degs, pathwayGenes);
        enrichmentTable.calculateEnrichment();

        List<EnrichmentResult> results = enrichmentTable.getEnrichmentResults();
        assertFalse(results.isEmpty()); // Resultaten moeten nog steeds berekend worden
    }
}
