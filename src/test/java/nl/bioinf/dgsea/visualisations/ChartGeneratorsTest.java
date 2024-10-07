package nl.bioinf.dgsea.visualisations;

import nl.bioinf.dgsea.data_processing.FileParseUtils;
import nl.bioinf.dgsea.table_outputs.Table;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

class ChartGeneratorsTest {

    @BeforeAll
    public static void setData() throws Exception {
        File dataFolder = new File("C://Users/wd_vi/IdeaProjects/dgsea-java/test_data");
        File pathwayFile = new File(dataFolder, "hsa_pathways.csv");
        File pathwayGenesFile = new File(dataFolder, "pathways.csv");
        File degsFile = new File(dataFolder, "degs.csv");
        FileParseUtils fileParseUtils = new FileParseUtils();
        Table.degs = fileParseUtils.parseDegsFile(degsFile);
        Table.pathwayGenes = fileParseUtils.parsePathwayGeneFile(pathwayGenesFile);
        Table.pathways = fileParseUtils.parsePathwayFile(pathwayFile);
    }

    private ChartGenerators.Builder getChartGeneratorsBuilderForCummVar() {
        ChartGenerators.Builder chartGeneratorsBuilder = new ChartGenerators.Builder(
                "Promising pathway combinations",
                "pathways",
                "perc share differential expression",
                Table.pathways,
                Table.pathwayGenes,
                Table.degs,
                new File("output.png")
        );
        chartGeneratorsBuilder.dpi(0.5);
        return chartGeneratorsBuilder;
    }

    @Test
    void outputCummVarChart() throws IOException {
        ChartGenerators.Builder chartGeneratorsBuilder = getChartGeneratorsBuilderForCummVar();
        ChartGenerators chartGenerators = new ChartGenerators(chartGeneratorsBuilder);
        chartGenerators.outputCummVarChart();

        assertTrue(new File("output.png").exists());



    }
}