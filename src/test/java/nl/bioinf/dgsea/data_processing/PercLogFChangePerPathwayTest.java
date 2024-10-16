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
    private List<Deg> degs1;
    private List<PathwayGene> pathwayGenes1;
    private Set<String> pathwayIds;
    private PercLogFChangePerPathway percLogFChangePerPathway;

    @BeforeEach
    public void setTestData() {
        degs1 = new ArrayList<>();
        pathwayGenes1 = new ArrayList<>();
        pathwayIds = new LinkedHashSet<>(); // LinkedHashSet to keep order of added elements, assuming what pathwayGenes is also in order we can do nested iteratarion faster, see:PercLogFChangePerPathway.getTotalLfc
    }

    /**
     * Check if exception when to little data was given to determine pathway-deg connections
     */
    @Test
    void percAllPathways_exceptionOnNoDataEntries() {
        List<Deg> degsEmpty = new ArrayList<>();
        List<PathwayGene> pathwaysGenesEmpty = new ArrayList<>();
        degs1.add(new Deg("", 0.0, 0.0));
        pathwayGenes1.add(new PathwayGene("", 1, "", ""));
        assertThrows(IllegalArgumentException.class, () -> new PercLogFChangePerPathway(degsEmpty, pathwayGenes1, 0.05));
        assertThrows(IllegalArgumentException.class, () -> new PercLogFChangePerPathway(degs1, pathwaysGenesEmpty, 0.05));
    }

    /**
     * Check if hsa10 is not linked to gene1
     */
    @Test
    void percAllPathways_missingMatchingGeneSymbols() {
        degs1.add(new Deg("gene1", 2.4, 0.0));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene2", ""));
        pathwayIds.add("hsa10");
        percLogFChangePerPathway = new PercLogFChangePerPathway(degs1, pathwayGenes1, 0.05);
        assertEquals(0.0,percLogFChangePerPathway.percAllPathways().get("hsa10"));
    }

    /**
     * Check if hsa10 is 0.0% despite being the only pathway
     */
    @Test
    void percAllPathways_noLfc() {
        degs1.add(new Deg("gene1", 0.0, 0.0));
        degs1.add(new Deg("gene2", 0.0, 0.0));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene1", ""));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene2", ""));
        pathwayIds.add("hsa10");
        percLogFChangePerPathway = new PercLogFChangePerPathway(degs1, pathwayGenes1, 0.05);
        assertEquals(0.0,percLogFChangePerPathway.percAllPathways().get("hsa10"));
    }

    /**
     * Check if hsa10 is 100.0%
     */
    @Test
    void percAllPathways_allLfc() {
        degs1.add(new Deg("gene1", 1.0, 0.0));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene1", ""));
        pathwayIds.add("hsa10");
        percLogFChangePerPathway = new PercLogFChangePerPathway(degs1, pathwayGenes1, 0.05);
        assertEquals(100.0,percLogFChangePerPathway.percAllPathways().get("hsa10"));
    }

    /**
     * Check if all 3 pathways are registered, even when percentage is 0.0.
     */
    @Test
    void percAllPathways_allPathwaysRegistered() {
        pathwayIds.add("hsa10");
        pathwayIds.add("hsa11");
        pathwayIds.add("hsa12");
        pathwayIds.add("hsa14");
        degs1.add(new Deg("gene1", 1.0, 0.0));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene1", ""));
        pathwayGenes1.add(new PathwayGene("hsa11", 1, "gene1", ""));
        pathwayGenes1.add(new PathwayGene("hsa12", 1, "gene1", ""));
        pathwayGenes1.add(new PathwayGene("hsa14", 1, "gene1", ""));
        percLogFChangePerPathway = new PercLogFChangePerPathway(degs1, pathwayGenes1, 0.05);
        assertEquals(4,percLogFChangePerPathway.percAllPathways().size());
    }

    @Test
    void percAllPathways_idealEasyCase() {
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
        percLogFChangePerPathway = new PercLogFChangePerPathway(degs1, pathwayGenes1, 0.05);
        assertEquals(40.0,percLogFChangePerPathway.percAllPathways().get("hsa14"));
    }

    @Test
    void percAllPathways_idealCase() {
        setIdealCase();
        percLogFChangePerPathway = new PercLogFChangePerPathway(degs1, pathwayGenes1, 0.05);
        Range awnserRange = new Range(36.36, 36.37);
        Map<String, Double> percAllPathways = percLogFChangePerPathway.percAllPathways();
        assertTrue(awnserRange.contains(percAllPathways.get("hsa14")));
    }

    /**
     * Check if hsa10 has any deg remaining after pval filter
     */
    @Test
    void percAllPathways_noSignificantDegs() {
        setUpNonSignificantDegs();
        percLogFChangePerPathway = new PercLogFChangePerPathway(degs1, pathwayGenes1, 0.05);
        Map<String, Double> percAllPathways = percLogFChangePerPathway.percAllPathways();
        assertEquals(0.0, percAllPathways.get("hsa10"));
    }

    /**
     * Check if in-significant(adjustedPValue > pval) degs do not influence the percentage of hsa11
     */
    @Test
    void percAllPathways_doNotCount_inSignificant() {
        setUpNonSignificantDegs();
        degs1.add(new Deg("gene4", 3.0, 0.0));
        pathwayGenes1.add(new PathwayGene("hsa11", 1, "gene4", ""));
        percLogFChangePerPathway = new PercLogFChangePerPathway(degs1, pathwayGenes1, 0.05);
        Map<String, Double> percAllPathways = percLogFChangePerPathway.percAllPathways();
        assertEquals(100.0, percAllPathways.get("hsa11"));
    }

    /**
     * Check if ideal case yields correct results.
     */
    @Test
    void filterMostInfluentialPathways_idealCase() {
        setIdealCase();
        percLogFChangePerPathway = new PercLogFChangePerPathway(degs1, pathwayGenes1, 0.05);
        Map<String, Double> percAllPathways = percLogFChangePerPathway.percAllPathways();
        Map<String, Double> percSomePathways = percLogFChangePerPathway.filterMostInfluentialPathways(3, percAllPathways);
        Range awnserRange = new Range(27.27, 27.28);
        assertEquals(3, percSomePathways.size());
        assertTrue(awnserRange.contains(percSomePathways.get("hsa12"))); // just about included
    }

    /**
     * Check if first value does not get filtered, just because it's the first.
     */
    @Test
    void filterMostInfluentialPathways_doNotFilterFirst() {
        pathwayIds.add("hsa10");
        pathwayIds.add("hsa11");
        degs1.add(new Deg("gene1", 4.5, 0.0));
        degs1.add(new Deg("gene2", -0.5, 0.0));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene1", ""));
        pathwayGenes1.add(new PathwayGene("hsa11", 1, "gene2", ""));
        percLogFChangePerPathway = new PercLogFChangePerPathway(degs1, pathwayGenes1, 0.05);
        Map<String, Double> perAllPathways = percLogFChangePerPathway.percAllPathways();
        Map<String, Double> percSomePathways = percLogFChangePerPathway.filterMostInfluentialPathways(1, perAllPathways);
        assertEquals(1, percSomePathways.size());
        assertEquals(90.0, percSomePathways.get("hsa10"));
    }

    /**
     * Check if size is still 3 when maxNpathways is larger (5)
     */
    @Test
    void filterMostInfluentialPathways_filterNone() {
        pathwayIds.add("hsa10");
        pathwayIds.add("hsa11");
        pathwayIds.add("hsa12");
        degs1.add(new Deg("gene1", 4.5, 0.0));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene1", ""));
        pathwayGenes1.add(new PathwayGene("hsa11", 1, "gene1", ""));
        pathwayGenes1.add(new PathwayGene("hsa12", 1, "gene1", ""));
        percLogFChangePerPathway = new PercLogFChangePerPathway(degs1, pathwayGenes1, 0.05);
        Map<String, Double> perAllPathways = percLogFChangePerPathway.percAllPathways();
        Map<String, Double> percSomePathways = percLogFChangePerPathway.filterMostInfluentialPathways(5, perAllPathways);
        assertEquals(3, percSomePathways.size());
    }

    /**
     * Check if first pathway gets picked when percentage-values are equal.
     */
    @Test
    void filterMostInfluentialPathways_pickFirstIfEqual() {
        pathwayIds.add("hsa10");
        pathwayIds.add("hsa11");
        degs1.add(new Deg("gene1", 1.5, 0.0));
        degs1.add(new Deg("gene2", 2.0, 0.0));
        degs1.add(new Deg("gene3", 1.0, 0.0));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene1", ""));
        pathwayGenes1.add(new PathwayGene("hsa11", 1, "gene2", ""));
        pathwayGenes1.add(new PathwayGene("hsa11", 1, "gene3", ""));
        percLogFChangePerPathway = new PercLogFChangePerPathway(degs1, pathwayGenes1, 0.05);
        Map<String, Double> perAllPathways = percLogFChangePerPathway.percAllPathways(); // hsa11 is in this case first
        Map<String, Double> percSomePathways = percLogFChangePerPathway.filterMostInfluentialPathways(1, perAllPathways);
        assertTrue(percSomePathways.containsKey("hsa11"));
    }

    /**
     * Checks if maxNPathways is larger than 0.
     */
    @Test
    void filterMostInfluentialPathways_noZeroPathways() {
        pathwayIds.add("hsa10");
        degs1.add(new Deg("gene1", 1.5, 0.0));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene1", ""));
        percLogFChangePerPathway = new PercLogFChangePerPathway(degs1, pathwayGenes1, 0.05);
        Map<String, Double> perAllPathways = percLogFChangePerPathway.percAllPathways(); // hsa11 is in this case first
        assertThrows(IllegalArgumentException.class, () ->percLogFChangePerPathway.filterMostInfluentialPathways(0, perAllPathways));
        assertThrows(IllegalArgumentException.class, () ->percLogFChangePerPathway.filterMostInfluentialPathways(-13, perAllPathways));
    }


    void setIdealCase() {
        pathwayIds.add("hsa10"); // average lfc: 2.0; perc lfc: 18.18
        pathwayIds.add("hsa11"); // average lfc: 2.0; perc lfc: 18.18
        pathwayIds.add("hsa12"); // average lfc: 3.0; perc lfc: 27.27
        pathwayIds.add("hsa14"); // average lfc: 4.0; perc lfc: 36.36
        pathwayIds.add("hsa15"); // average lfc: 0.0; perc lfc: 0.0
        degs1.add(new Deg("gene1", 1.0, 0.0));
        degs1.add(new Deg("gene2", 2.0, 0.0));
        degs1.add(new Deg("gene3", -3.0, 0.0));
        degs1.add(new Deg("gene4", 4.0, 0.0));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene1", ""));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene3", ""));
        pathwayGenes1.add(new PathwayGene("hsa11", 1, "gene2", ""));
        pathwayGenes1.add(new PathwayGene("hsa12", 1, "gene3", ""));
        pathwayGenes1.add(new PathwayGene("hsa14", 1, "gene4", ""));
        pathwayGenes1.add(new PathwayGene("hsa15", 1, "gene5", ""));
    }

    void setUpNonSignificantDegs() {
        pathwayIds.add("hsa10");
        pathwayIds.add("hsa11");
        degs1.add(new Deg("gene1", 1.0, 1.0));
        degs1.add(new Deg("gene2", 2.0, 1.0));
        degs1.add(new Deg("gene3", 2.0, 0.05000000000001));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene1", ""));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene2", ""));
        pathwayGenes1.add(new PathwayGene("hsa10", 1, "gene3", ""));
    }
}
