package nl.bioinf.dgsea.visualisations;

import nl.bioinf.dgsea.data_processing.*;
import org.jfree.data.time.DateRange;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.*;


import static org.junit.jupiter.api.Assertions.assertTrue;

class ChartGeneratorTest {
    static List<Deg> degs;
    static List<Pathway> pathways;
    static List<PathwayGene> pathwayGenes;

    @BeforeAll
    public static void setData() throws Exception {
        File dataFolder = new File("src/test/resources/");
        File pathwayFile = new File(dataFolder, "hsa_pathways.csv");
        File pathwayGenesFile = new File(dataFolder, "pathways.csv");
        File degsFile = new File(dataFolder, "degs.csv");
        FileParseUtils fileParseUtils = new FileParseUtils();
        ChartGeneratorTest.degs = fileParseUtils.parseDegsFile(degsFile);
        ChartGeneratorTest.pathwayGenes = fileParseUtils.parsePathwayGeneFile(pathwayGenesFile);
        ChartGeneratorTest.pathways = fileParseUtils.parsePathwayFile(pathwayFile);
    }

    private ChartGenerator.Builder getChartGeneratorsBuilderForCummVar() {
        ChartGenerator.Builder chartGeneratorsBuilder = new ChartGenerator.Builder(
                "Promising pathway combinations",
                "pathways",
                "perc share differential expression",
                ChartGeneratorTest.degs,
                ChartGeneratorTest.pathways,
                ChartGeneratorTest.pathwayGenes,
                new File("output.png")
        );
        chartGeneratorsBuilder.dpi(0.5);
        return chartGeneratorsBuilder;
    }

    @Test
    void saveChartPercLogFChangePerPathway() {
        ChartGenerator.Builder chartGeneratorsBuilder = getChartGeneratorsBuilderForCummVar();
        ChartGenerator chartGenerator = new ChartGenerator(chartGeneratorsBuilder);
        chartGenerator.saveChartPercLogFChangePerPathway();

        System.out.println("System.currentTimeMillis() = " + System.currentTimeMillis());
        System.out.println("new File(\"output.png\").lastModified() = " + new File("output.png").lastModified());
        assertTrue(new DateRange(System.currentTimeMillis() - (100*100), System.currentTimeMillis()).contains(new File("output.png").lastModified()));


    }

}