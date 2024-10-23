package nl.bioinf.dgsea.data_processing;

import org.jfree.data.Range;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.provider.*;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PercLfcPathwaysTest {
    private List<Deg> degs1;
    private List<PathwayGene> pathwayGenes1;
    private String[] pathwayIds;


    @BeforeEach
    public void setTestData() {
        degs1 = new ArrayList<>();
        pathwayGenes1 = new ArrayList<>();
        pathwayIds = new String[] {"hsa00010", "hsa00020", "hsa00030", "hsa00040"};
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

    /**
     * Does it throw IllegalArgumentException on empty data input?
     */
    @Test
    void percAllPathways_expectException() {
        List<Deg> degsEmpty = new ArrayList<>();
        List<PathwayGene> pathwaysGenesEmpty = new ArrayList<>();
        degs1.add(new Deg("", 0.0, 0.0));
        pathwayGenes1.add(new PathwayGene("", 1, "", ""));
        assertThrows(IllegalArgumentException.class, () -> new PercLfcPathways(degsEmpty, pathwayGenes1));
        assertThrows(IllegalArgumentException.class, () -> new PercLfcPathways(degs1, pathwaysGenesEmpty));
    }

    /**
     * Does it throw IllegalArgumentException when no pathway-ids were given?
     */
    @Test
    void percAllPathways_expectOnNoPathway() {
        degs1.add(new Deg("gene1", 0.0, 0.0));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene1", ""));
        String[] pathwayIds = new String[] {};
        String[] pathwayIds1 = null;
        PercLfcPathways percLfcPathways = new PercLfcPathways(degs1, pathwayGenes1);
        assertThrows(IllegalArgumentException.class, () -> percLfcPathways.percAllPathways(pathwayIds));
        assertThrows(IllegalArgumentException.class, () -> percLfcPathways.percAllPathways(pathwayIds1));
    }

    /**
     * Does it throw IllegalArgumentException when given pathway-ids are not found at all in the input data?
     */
    @Test
    void percAllPathways_expectMissingPathway() {
        degs1.add(new Deg("gene1", 0.0, 0.0));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene1", ""));
        String[] pathwayIds = new String[] {"hsa11"};
        String[] pathwayIds1 = new String[] {"hsa1"};
        String[] pathwayIds2 = new String[] {"hsa100"};
        String[] pathwayIds3 = new String[] {"10"};
        PercLfcPathways percLfcPathways = new PercLfcPathways(degs1, pathwayGenes1);
        assertThrows(IllegalArgumentException.class, () -> percLfcPathways.percAllPathways(pathwayIds));
        assertThrows(IllegalArgumentException.class, () -> percLfcPathways.percAllPathways(pathwayIds1));
        assertThrows(IllegalArgumentException.class, () -> percLfcPathways.percAllPathways(pathwayIds2));
        assertThrows(IllegalArgumentException.class, () -> percLfcPathways.percAllPathways(pathwayIds3));
    }

    /**
     * Does it not fail, but just give 0.0, when no mathing gene-symbols were found?
     */
    @Test
    void percAllPathways_expectMissingMatchingGeneSymbols() {
        degs1.add(new Deg("gene1", 2.4, 0.0));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene2", ""));
        String[] pathwayIds = new String[] {"hsa10"};
        PercLfcPathways percLfcPathways = new PercLfcPathways(degs1, pathwayGenes1);
        assertEquals(0.0, percLfcPathways.percAllPathways(pathwayIds)[0]);
    }

    /**
     * Does it give 0.0(%), when logFoldChange is 0.0, despite pathway hsa10 being the only one present?
     */
    @Test
    void percAllPathways_expectStillZero() {
        degs1.add(new Deg("gene1", 0.0, 0.0));
        degs1.add(new Deg("gene2", 0.0, 0.0));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene1", ""));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene2", ""));
        String[] pathwayIds = new String[] {"hsa10"};
        PercLfcPathways percLfcPathways = new PercLfcPathways(degs1, pathwayGenes1);
        assertEquals(0.0, percLfcPathways.percAllPathways(pathwayIds)[0]);
    }

    /**
     * Does it give 100.0(%), when only pathway hsa10 has a differently expressed gene?
     */
    @Test
    void percAllPathways_expectHundred() {
        degs1.add(new Deg("gene1", 1.0, 0.0));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene1", ""));
        String[] pathwayIds = new String[] {"hsa10"};
        PercLfcPathways percLfcPathways = new PercLfcPathways(degs1, pathwayGenes1);
        assertEquals(100.0, percLfcPathways.percAllPathways(pathwayIds)[0]);
    }

    /**
     * Does it give 4 pathways back, when 4 are put in and selected?
     */
    @Test
    void percAllPathways_expectAllPossiblePathways() {
        pathwayIds = new String[] {"hsa10", "hsa11", "hsa12", "hsa14"};
        degs1.add(new Deg("gene1", 1.0, 0.0));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene1", ""));
        pathwayGenes1.add(new PathwayGene("hsa11", 1, "gene1", ""));
        pathwayGenes1.add(new PathwayGene("hsa12", 1, "gene1", ""));
        pathwayGenes1.add(new PathwayGene("hsa14", 1, "gene1", ""));
        PercLfcPathways percLfcPathways = new PercLfcPathways(degs1, pathwayGenes1);
        assertEquals(4, percLfcPathways.percAllPathways(pathwayIds).length);
    }

    /**
     * Does it give 3 pathways back, when 4 are put in, but 3 selected?
     */
    @Test
    void percAllPathways_expectSelectedPathways() {
        pathwayIds = new String[] {"hsa10", "hsa11", "hsa12"};
        degs1.add(new Deg("gene1", 1.0, 0.0));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene1", ""));
        pathwayGenes1.add(new PathwayGene("hsa11", 1, "gene1", ""));
        pathwayGenes1.add(new PathwayGene("hsa12", 1, "gene1", ""));
        pathwayGenes1.add(new PathwayGene("hsa14", 1, "gene1", ""));
        PercLfcPathways percLfcPathways = new PercLfcPathways(degs1, pathwayGenes1);
        assertEquals(3, percLfcPathways.percAllPathways(pathwayIds).length);
    }

    /**
     * Does it throw error when maximum number of pathways is 0 or less?
     */
    @Test
    void filterMostInfluentialPathways_expectInvalidMaxNPathways() {
        pathwayIds = new String[] {"hsa10"};
        degs1.add(new Deg("gene1", 1.0, 0.0));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene1", ""));
        PercLfcPathways percLfcPathways = new PercLfcPathways(degs1, pathwayGenes1);
        double[] percAllPathways = percLfcPathways.percAllPathways(pathwayIds);
        assertThrows(IllegalArgumentException.class, () -> percLfcPathways.filterMostInfluentialPathways(-1, percAllPathways, pathwayIds));
        assertThrows(IllegalArgumentException.class, () -> percLfcPathways.filterMostInfluentialPathways(0, percAllPathways, pathwayIds));
    }

    /**
     * Does it give 3 pathways out of 4?
     */
    @Test
    void filterMostInfluentialPathways_expectSomePathways() {
        int maxNPathways = 3;
        pathwayIds = new String[] {"hsa10", "hsa11", "hsa12", "hsa14"};
        PercLfcPathways percLfcPathways = getIdealEasyCaseData();
        double[] percAllPathways = percLfcPathways.percAllPathways(pathwayIds);
        assertEquals(
                maxNPathways,
                percLfcPathways.filterMostInfluentialPathways(maxNPathways, percAllPathways, pathwayIds).size());
    }

    /**
     * Does it give 2 particular pathways because these have the highest percentage absolute average lfc?
     */
    @Test
    void filterMostInfluentialPathways_expectParticularPathways() {
        int maxNPathways = 2;
        pathwayIds = new String[] {"hsa10", "hsa11", "hsa12", "hsa14"};
        PercLfcPathways percLfcPathways = getIdealCaseData();
        double[] percAllPathways = percLfcPathways.percAllPathways(pathwayIds);
        Map<String, Double> filterMostInfluentialPathways = percLfcPathways.filterMostInfluentialPathways(maxNPathways, percAllPathways, pathwayIds);
        assertTrue(filterMostInfluentialPathways.containsKey("hsa12"));
        assertTrue(filterMostInfluentialPathways.containsKey("hsa14"));
    }

    /**
     * Does it give an expected result given the input, in a situation where every pathway has 1 gene?
     */
    @Test
    void percAllPathways_idealEasyCase() {
        pathwayIds = new String[] {"hsa10", "hsa11", "hsa12", "hsa14"};
        PercLfcPathways percLfcPathways = getIdealEasyCaseData();
        assertEquals(40.0, percLfcPathways.percAllPathways(pathwayIds)[3]);
    }

    /**
     * Does it give an expected result given the input, in a situation where every pathway has multiple genes?
     */
    @Test
    void percAllPathways_idealCase() {
        pathwayIds = new String[] {"hsa10", "hsa11", "hsa12", "hsa14", "hsa15"};
        PercLfcPathways percLfcPathways = getIdealCaseData();
        Range awnserRange = new Range(30.769, 30.770);
        double[] percAllPathways = percLfcPathways.percAllPathways(pathwayIds);
        assertTrue(awnserRange.contains(percAllPathways[3]));
    }

    private PercLfcPathways getIdealCaseData() {
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
        PercLfcPathways percLfcPathways = new PercLfcPathways(degs1, pathwayGenes1);
        return percLfcPathways;
    }

    private PercLfcPathways getIdealEasyCaseData() {
        degs1.add(new Deg("gene1", 1.0, 0.0));
        degs1.add(new Deg("gene2", 2.0, 0.0));
        degs1.add(new Deg("gene3", 3.0, 0.0));
        degs1.add(new Deg("gene4", 4.0, 0.0));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene1", ""));
        pathwayGenes1.add(new PathwayGene("hsa11", 1, "gene2", ""));
        pathwayGenes1.add(new PathwayGene("hsa12", 1, "gene3", ""));
        pathwayGenes1.add(new PathwayGene("hsa14", 1, "gene4", ""));
        PercLfcPathways percLfcPathways = new PercLfcPathways(degs1, pathwayGenes1);
        return percLfcPathways;
    }

}