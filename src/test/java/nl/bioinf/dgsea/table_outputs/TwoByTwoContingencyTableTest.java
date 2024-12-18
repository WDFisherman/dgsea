package nl.bioinf.dgsea.table_outputs;

import nl.bioinf.dgsea.data_processing.Deg;
import nl.bioinf.dgsea.data_processing.Pathway;
import nl.bioinf.dgsea.data_processing.PathwayGene;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TwoByTwoContingencyTableTest {
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


    /**
     * Does the constructor throw IllegalStateException when 1 of the input data is null?
     */
    @Test
    void constructor_nullInputs() {
        assertThrows(IllegalStateException.class, () -> new TwoByTwoContingencyTable(null, pathways, pathwayGenes, 0.01));
        assertThrows(IllegalStateException.class, () -> new TwoByTwoContingencyTable(degs, null, pathwayGenes, 0.01));
        assertThrows(IllegalStateException.class, () -> new TwoByTwoContingencyTable(degs, pathways, null, 0.01));
    }

    /**
     * Does table have the correct numbers and layout?
     */
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

    /**
     * Does the table have all degs in just one pathway?
     */
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

    /**
     * Does the table show there are no matching degs counted for Pathway1?
     */
    @Test
    void getTable_noDegs() {
        String output = getOutput();
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

    private static String getOutput() {
        List<Deg> degs1 = new ArrayList<>();
        List<Pathway> pathways1 = new ArrayList<>();
        List<PathwayGene> pathwayGenes1 = new ArrayList<>();
        degs1.add(new Deg("GeneA", 2.5, 0.005));
        pathways1.add(new Pathway("Pathway1", "Test Pathway 1"));
        pathwayGenes1.add(new PathwayGene("Pathway1", 0,"", ""));
        TwoByTwoContingencyTable twoByTwoContingencyTable = new TwoByTwoContingencyTable(
                degs1,
                pathways1,
                pathwayGenes1,
                0.05
        );
        return twoByTwoContingencyTable.getTable();
    }

    /**
     * Does the function throw a null pointer when encountering a complete lack of Pathway1 in pathwayGenes?
     */
    @Test
    void getTable_nullPointerException() {
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
        assertThrows(NullPointerException.class, twoByTwoContingencyTable::getTable);

    }

    // Test for degs with missing genes in pathways
    @Test
    void getTable_degsWithoutPathwayGenes() {
        // Add DEG that does not match any gene in pathways
        degs.add(new Deg("GeneE", 1.5, 0.007));
        String result = twoByTwoContingencyTable.getTable();

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
                C\t | 0\t | 0\t | 0
                C*\t | 3\t | 1\t | 4
                Sum\t | 3\t | 1\t | 4
                
                D=is.. D*=is not.., Significant deg C=in.. C*=not in.., ..pathway.""";

        assertEquals(expected, result);
    }
}

