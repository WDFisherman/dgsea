package nl.bioinf.dgsea.table_outputs;

import nl.bioinf.dgsea.data_processing.Deg;
import nl.bioinf.dgsea.data_processing.Pathway;
import nl.bioinf.dgsea.data_processing.PathwayGene;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TableTest {
    Table table;

    @BeforeEach
    void setUp() {
        List<Deg> degs = new ArrayList<>();
        List<Pathway> pathways = new ArrayList<>();
        List<PathwayGene> pathwayGenes = new ArrayList<>();

        // Set up some test data
        // Adding DEGs
        degs.add(new Deg("GeneA", 2.5, 0.005));
        degs.add(new Deg("GeneB", -1.2, 0.02));
        degs.add(new Deg("GeneC", 3.0, 0.001));

        // Adding Pathways
        pathways.add(new Pathway("Pathway1", "Test Pathway 1"));
        pathways.add(new Pathway("Pathway2", "Test Pathway 2"));

        // Adding Pathway Genes
        pathwayGenes.add(new PathwayGene("Pathway1", 101, "GeneA", "ENSG000001"));
        pathwayGenes.add(new PathwayGene("Pathway1", 102, "GeneB", "ENSG000002"));
        pathwayGenes.add(new PathwayGene("Pathway1", 103, "GeneC", "ENSG000003"));
        pathwayGenes.add(new PathwayGene("Pathway2", 104, "GeneD", "ENSG000004"));

        table = new Table(degs, pathways, pathwayGenes, 0.01);
    }


    @Test
    void testGetSumInPathway() {
        int count = table.getSumInPathway("Pathway1");
        assertEquals(2, count, "Should count 2 DEGs in Pathway1");
    }

    @Test
    void testGetSumTotalPathway() {
        int count = table.getSumTotalPathway("Pathway1");
        assertEquals(3, count, "Should count 3 genes in Pathway1");
    }

    @Test
    void testGetSumIsSignificantDeg() {
        int count = table.getSumIsSignificantDeg();
        assertEquals(2, count, "Should count 2 significant DEGs");
    }

    @Test
    void testGetSumTotalDeg() {
        int count = table.getSumTotalDeg();
        assertEquals(3, count, "Should count total 3 DEGs");
    }

    @Test
    void testIsDeg() {
        boolean result = table.isDeg("GeneA");
        assertTrue(result, "GeneA should be a DEG");

        result = table.isDeg("GeneD");
        assertFalse(result, "GeneD should not be a DEG");
    }

    @Test
    void testGetSumNonSignificantInPathway() {
        int count = table.getSumNonSignificantInPathway("Pathway1");
        assertEquals(1, count, "Should count 1 non-significant DEG in Pathway1");
    }
}
