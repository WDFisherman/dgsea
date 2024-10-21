package nl.bioinf.dgsea.table_outputs;

import nl.bioinf.dgsea.data_processing.Deg;
import nl.bioinf.dgsea.data_processing.Pathway;
import nl.bioinf.dgsea.data_processing.PathwayGene;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TwoByTwoContingenecyTableTest {
    TwoByTwoContingencyTable twoByTwoContingencyTable;
    List<Deg> degs;
    List<Pathway> pathways;
    List<PathwayGene> pathwayGenes;

    @BeforeEach
    void setUp() {
        degs = new ArrayList<>();
        pathways = new ArrayList<>();
        pathwayGenes = new ArrayList<>();

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

        twoByTwoContingencyTable = new TwoByTwoContingencyTable(degs, pathways, pathwayGenes, 0.01);
    }

    @Test
    void getTable_idealCase() {
        degs.add(new Deg("GeneD", 2.5, 0.005));

        String input = twoByTwoContingencyTable.getTable();
        String expected = """

                Test Pathway 1 (Pathway1)
                \t | D\t | D*\t | Sum
                ----------------------
                C\t | 2\t | 1\t | 3
                C*\t | 1\t | 0\t | 1
                Sum\t | 3\t | 1\t | 4
                
                Test Pathway 2 (Pathway2)
                \t | D\t | D*\t | Sum
                ----------------------
                C\t | 1\t | 0\t | 1
                C*\t | 2\t | 1\t | 3
                Sum\t | 3\t | 1\t | 4
                
                D=is.. D*=is not.., Significant deg C=in.. C*=not in.., ..pathway.""";
        assertEquals(expected, input);
    }

    @Test
    void getTable_allDegsInOnePathway() {
        String input = twoByTwoContingencyTable.getTable();
        String expected = """

                Test Pathway 1 (Pathway1)
                \t | D\t | D*\t | Sum
                ----------------------
                C\t | 2\t | 1\t | 3
                C*\t | 0\t | 0\t | 0
                Sum\t | 2\t | 1\t | 3
                
                Test Pathway 2 (Pathway2)
                \t | D\t | D*\t | Sum
                ----------------------
                C\t | 0\t | 0\t | 0
                C*\t | 2\t | 1\t | 3
                Sum\t | 2\t | 1\t | 3
                
                D=is.. D*=is not.., Significant deg C=in.. C*=not in.., ..pathway.""";
        assertEquals(expected, input);
    }

    @Test
    void getTable_noDegs() {
        List<Deg> degs1 = new ArrayList<>();
        List<Pathway> pathways1 = new ArrayList<>();
        List<PathwayGene> pathwayGenes1 = new ArrayList<>();
        degs1.add(new Deg("GeneA", 2.5, 0.005));
        pathways1.add(new Pathway("Pathway1", "Test Pathway 1"));
        pathwayGenes1.add(new PathwayGene("", 0,"", ""));
        TwoByTwoContingencyTable twoByTwoContingencyTable = new TwoByTwoContingencyTable(
                degs1,
                pathways1,
                pathwayGenes1,
                0.05
        );
        String output = twoByTwoContingencyTable.getTable();
        String expected = """

                Test Pathway 1 (Pathway1)
                \t | D\t | D*\t | Sum
                ----------------------
                C\t | 0\t | 0\t | 0
                C*\t | 1\t | 0\t | 1
                Sum\t | 1\t | 0\t | 1
                
                D=is.. D*=is not.., Significant deg C=in.. C*=not in.., ..pathway.""";
        assertEquals(expected, output);
    }
}
