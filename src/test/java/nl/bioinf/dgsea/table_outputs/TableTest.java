package nl.bioinf.dgsea.table_outputs;

import nl.bioinf.dgsea.data_processing.Deg;
import nl.bioinf.dgsea.data_processing.Pathway;
import nl.bioinf.dgsea.data_processing.PathwayGene;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class TableTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void cleanUpStreams() {
        System.setOut(null);
    }
//    @BeforeEach
//    void setUp() {
//
//    }

    @Test
    void getTwoByTwoContingencyTable() {
        String expectedPrint = """
Notch singnaling pathway (hsa04330)
     | D | D* | Sum
   C | 2 | 1  | 3
  C* | 0 | 0  | 0
 Sum | 2 | 1  | 3

 C: in pathway, C*: not in pathway
 D: DEG (FDR <= 0.01), D*: non DEG""";
        Table.pathwayGenes = new ArrayList<>();
        Table.degs = new ArrayList<>();
        Table.pathways = new ArrayList<>();
        Table.pathways.add(new Pathway("hsa04330", "Notch signaling pathway"));
        Table.degs.add(new Deg("APH1A", 0.209662905955719, 0.0496818331573734));
        Table.degs.add(new Deg("ATXN1", -0.370630542276104, 1.84634028337393e-05));
        Table.degs.add(new Deg("CREBBP", 0.321180507665933, 0.000398119286441702));
        Table.pathwayGenes.add(new PathwayGene("hsa04330", 6868, "ADAM17", "ENSG00000151694"));
        Table.pathwayGenes.add(new PathwayGene("hsa04330", 6310, "ATXN1", "ENSG00000124788"));
        Table.pathwayGenes.add(new PathwayGene("hsa04330", 1387, "CREBBP", "ENSG00000005339"));
        String[] pathways = {"hsa04330"};
        Table.getTwoByTwoContingencyTable(TableOutputOptions.PRINT, pathways);
        assertEquals(
                expectedPrint + System.lineSeparator(),
                outContent.toString()
        );
    }
}