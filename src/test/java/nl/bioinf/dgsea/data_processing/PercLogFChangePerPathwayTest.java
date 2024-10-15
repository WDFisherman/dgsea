package nl.bioinf.dgsea.data_processing;

import org.jfree.data.Range;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.provider.*;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PercLogFChangePerPathwayTest {
    private List<Deg> degs;
    private List<Pathway> pathways;
    private List<PathwayGene> pathwayGenes;
    private List<Deg> degs1;
    private List<Deg> degs2;
    private List<Pathway> pathways1;
    private List<Pathway> pathways2;
    private List<PathwayGene> pathwayGenes1;
    private List<PathwayGene> pathwaysGenes2;
    private Set<String> pathwayIds;

    @BeforeAll
    public void setData() throws Exception {
        File dataFolder = new File("src/test/resources/");
        File pathwayFile = new File(dataFolder, "hsa_pathways.csv");
        File pathwayGenesFile = new File(dataFolder, "pathways.csv");
        File degsFile = new File(dataFolder, "degs.csv");
        FileParseUtils fileParseUtils = new FileParseUtils();
        degs = fileParseUtils.parseDegsFile(degsFile);
        pathways = fileParseUtils.parsePathwayFile(pathwayFile);
        pathwayGenes = fileParseUtils.parsePathwayGeneFile(pathwayGenesFile);
    }

    @BeforeEach
    public void setTestData() {
        degs1 = new ArrayList<>();
        degs2 = new ArrayList<>();
        pathways1 = new ArrayList<>();
        pathways2 = new ArrayList<>();
        pathwayGenes1 = new ArrayList<>();
        pathwaysGenes2 = new ArrayList<>();
        pathwayIds = new LinkedHashSet<>();
        pathwayIds.add("hsa00010");
        pathwayIds.add("hsa00020");
        pathwayIds.add("hsa00030");
        pathwayIds.add("hsa00040");
    }

    private static Stream<Arguments> provideEmptyInputs() {
        List<Deg> degsEmpty = new ArrayList<>();
        List<PathwayGene> pathwaysGenesEmpty = new ArrayList<>();
        List<Deg> degs1 = new ArrayList<>();
        List<PathwayGene> pathwayGenes1 = new ArrayList<>();
        degs1.add(new Deg("", 0.0, 0.0));
        pathwayGenes1.add(new PathwayGene("", 1, "", ""));
        return Stream.of(
            Arguments.of(degsEmpty, pathwayGenes1, IllegalArgumentException.class),
            Arguments.of(degs1, pathwaysGenesEmpty, IllegalArgumentException.class)
        );
    }

    @Test
    void percAllPathways_expectException() {
        List<Deg> degsEmpty = new ArrayList<>();
        List<PathwayGene> pathwaysGenesEmpty = new ArrayList<>();
        degs1.add(new Deg("", 0.0, 0.0));
        pathwayGenes1.add(new PathwayGene("", 1, "", ""));
        assertThrows(IllegalArgumentException.class, () -> new PercLogFChangePerPathway(degsEmpty, pathwayGenes1));
        assertThrows(IllegalArgumentException.class, () -> new PercLogFChangePerPathway(degs1, pathwaysGenesEmpty));
    }

    @Test
    void percAllPathways_expectOnNoPathway() {
        degs1.add(new Deg("gene1", 0.0, 0.0));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene1", ""));
        Set<String> pathwayIds = new HashSet<>();
        PercLogFChangePerPathway percLogFChangePerPathway = new PercLogFChangePerPathway(degs1, pathwayGenes1);
        assertThrows(IllegalArgumentException.class, () -> percLogFChangePerPathway.percAllPathways(pathwayIds));
    }

    @Test
    void percAllPathways_expectMissingPathway() {
        degs1.add(new Deg("gene1", 0.0, 0.0));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene1", ""));
        Set<String> pathwayIds = new HashSet<>();
        Set<String> pathwayIds1 = new HashSet<>();
        Set<String> pathwayIds2 = new HashSet<>();
        Set<String> pathwayIds3 = new HashSet<>();
        pathwayIds.add("hsa11");
        pathwayIds1.add("hsa1"); // test exact match
        pathwayIds2.add("hsa100");
        pathwayIds3.add("10");
        PercLogFChangePerPathway percLogFChangePerPathway = new PercLogFChangePerPathway(degs1, pathwayGenes1);
        assertThrows(IllegalArgumentException.class, () -> percLogFChangePerPathway.percAllPathways(pathwayIds));
        assertThrows(IllegalArgumentException.class, () -> percLogFChangePerPathway.percAllPathways(pathwayIds1));
        assertThrows(IllegalArgumentException.class, () -> percLogFChangePerPathway.percAllPathways(pathwayIds2));
        assertThrows(IllegalArgumentException.class, () -> percLogFChangePerPathway.percAllPathways(pathwayIds3));
    }

    @Test
    void percAllPathways_expectMissingMatchingGeneSymbols() {
        degs1.add(new Deg("gene1", 2.4, 0.0));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene2", ""));
        Set<String> pathwayIds = new HashSet<>();
        pathwayIds.add("hsa10");
        PercLogFChangePerPathway percLogFChangePerPathway = new PercLogFChangePerPathway(degs1, pathwayGenes1);
        assertEquals(0.0,percLogFChangePerPathway.percAllPathways(pathwayIds).get("hsa10"));
    }

    @Test
    void percAllPathways_expectStillZero() {
        degs1.add(new Deg("gene1", 0.0, 0.0));
        degs1.add(new Deg("gene2", 0.0, 0.0));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene1", ""));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene2", ""));
        Set<String> pathwayIds = new HashSet<>();
        pathwayIds.add("hsa10");
        PercLogFChangePerPathway percLogFChangePerPathway = new PercLogFChangePerPathway(degs1, pathwayGenes1);
        assertEquals(0.0,percLogFChangePerPathway.percAllPathways(pathwayIds).get("hsa10"));
    }

    @Test
    void percAllPathways_expectHundred() {
        degs1.add(new Deg("gene1", 1.0, 0.0));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene1", ""));
        Set<String> pathwayIds = new HashSet<>();
        pathwayIds.add("hsa10");
        PercLogFChangePerPathway percLogFChangePerPathway = new PercLogFChangePerPathway(degs1, pathwayGenes1);
        assertEquals(100.0,percLogFChangePerPathway.percAllPathways(pathwayIds).get("hsa10"));
    }

    @Test
    void percAllPathways_expectAllPossiblePathways() {
        pathwayIds = new LinkedHashSet<>();
        pathwayIds.add("hsa10");
        pathwayIds.add("hsa11");
        pathwayIds.add("hsa12");
        pathwayIds.add("hsa14");
        degs1.add(new Deg("gene1", 1.0, 0.0));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene1", ""));
        pathwayGenes1.add(new PathwayGene("hsa11", 1, "gene1", ""));
        pathwayGenes1.add(new PathwayGene("hsa12", 1, "gene1", ""));
        pathwayGenes1.add(new PathwayGene("hsa14", 1, "gene1", ""));
        PercLogFChangePerPathway percLogFChangePerPathway = new PercLogFChangePerPathway(degs1, pathwayGenes1);
        assertEquals(4,percLogFChangePerPathway.percAllPathways(pathwayIds).size());
    }

    @Test
    void percAllPathways_idealEasyCase() {
        pathwayIds = new LinkedHashSet<>();
        pathwayIds.add("hsa10");
        pathwayIds.add("hsa11");
        pathwayIds.add("hsa12");
        pathwayIds.add("hsa14");
        degs1.add(new Deg("gene1", 1.0, 0.0));
        degs1.add(new Deg("gene2", 2.0, 0.0));
        degs1.add(new Deg("gene3", 3.0, 0.0));
        degs1.add(new Deg("gene4", 4.0, 0.0));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene1", ""));
        pathwayGenes1.add(new PathwayGene("hsa11", 1, "gene2", ""));
        pathwayGenes1.add(new PathwayGene("hsa12", 1, "gene3", ""));
        pathwayGenes1.add(new PathwayGene("hsa14", 1, "gene4", ""));
        PercLogFChangePerPathway percLogFChangePerPathway = new PercLogFChangePerPathway(degs1, pathwayGenes1);
        assertEquals(40.0,percLogFChangePerPathway.percAllPathways(pathwayIds).get("hsa14"));
    }

    @Test
    void percAllPathways_idealCase() {
        pathwayIds = new LinkedHashSet<>();
        pathwayIds.add("hsa10");
        pathwayIds.add("hsa11");
        pathwayIds.add("hsa12");
        pathwayIds.add("hsa14");
        pathwayIds.add("hsa15");
        degs1.add(new Deg("gene1", 1.0, 0.0));
        degs1.add(new Deg("gene2", 2.0, 0.0));
        degs1.add(new Deg("gene3", 3.0, 0.0));
        degs1.add(new Deg("gene4", 4.0, 0.0));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene1", ""));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene3", ""));
        pathwayGenes1.add(new PathwayGene("hsa11", 1, "gene2", ""));
        pathwayGenes1.add(new PathwayGene("hsa12", 1, "gene3", ""));
        pathwayGenes1.add(new PathwayGene("hsa14", 1, "gene4", ""));
        pathwayGenes1.add(new PathwayGene("hsa15", 1, "gene5", ""));
        PercLogFChangePerPathway percLogFChangePerPathway = new PercLogFChangePerPathway(degs1, pathwayGenes1);
        Range awnserRange = new Range(30.769, 30.770);
        Map<String, Double> percAllPathways = percLogFChangePerPathway.percAllPathways(pathwayIds);
        assertTrue(awnserRange.contains(percAllPathways.get("hsa14")));
    }

    void noSignificantDegs() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    void doNotCountInSignificant() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
