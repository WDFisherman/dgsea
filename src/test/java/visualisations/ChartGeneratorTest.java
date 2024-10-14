package visualisations;

import nl.bioinf.dgsea.data_processing.Deg;
import nl.bioinf.dgsea.data_processing.FileParseUtils;
import nl.bioinf.dgsea.data_processing.Pathway;
import nl.bioinf.dgsea.data_processing.PathwayGene;
import nl.bioinf.dgsea.visualisations.ChartGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

class ChartGeneratorTest {
    static List<Deg> degs;
    static List<Pathway> pathways;
    static List<PathwayGene> pathwayGenes;

    @BeforeAll
    public static void setData() throws Exception {
        File dataFolder = new File("test_data");
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
                ChartGeneratorTest.pathways,
                ChartGeneratorTest.pathwayGenes,
                ChartGeneratorTest.degs,
                new File("output.png")
        );
        chartGeneratorsBuilder.dpi(0.5);
        return chartGeneratorsBuilder;
    }

    @Test
    void outputCummVarChart() throws IOException {
        ChartGenerator.Builder chartGeneratorsBuilder = getChartGeneratorsBuilderForCummVar();
        ChartGenerator chartGenerator = new ChartGenerator(chartGeneratorsBuilder);
        chartGenerator.saveChartPercLogFChangePerPathway();

        assertTrue(new File("output.png").exists());



    }
}