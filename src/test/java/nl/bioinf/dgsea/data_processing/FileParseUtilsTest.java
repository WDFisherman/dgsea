package nl.bioinf.dgsea.data_processing;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
class FileParseUtilsTest {
    final FileParseUtils fileParseUtils = new FileParseUtils();

    @Test
    void parseDegsFile_idealCase() {
        try {
            fileParseUtils.parseDegsFile(new File("src/test/resources/degs.csv"));
            assertTrue(true);
        } catch (IOException e) {
            fail("Did no expect IOException, got an error: " + e.getMessage());
        }
    }
    @Test
    void parsePathwayFile_idealCase() {
        try {
            fileParseUtils.parsePathwayFile(new File("src/test/resources/hsa_pathways.csv"));
            assertTrue(true);
        } catch (IOException e) {
            fail("Did no expect IOException, got an error: " + e.getMessage());
        }
    }
    @Test
    void parsePathwayGeneFile_idealCase() {
        try {
            fileParseUtils.parsePathwayGeneFile(new File("src/test/resources/pathways.csv"));
            assertTrue(true);
        } catch (IOException e) {
            fail("Did no expect IOException, got an error: " + e.getMessage());
        }
    }

    @Test
    void parseDegsFile_wrongNColumns() {
        FileParseUtils fpu = new FileParseUtils();
        try {
            fpu.parseDegsFile(new File("src/test/resources/test_input/wrong-n-columns.csv"));
            fail("Expected IOException on wrong n column, got no error.");
        } catch (IOException e) {
            assertEquals(
                    "Invalid DEG file format. Expected at least 3 columns.",
                    e.getMessage()
            );
        }
    }

    @Test
    void parsePathwayFile_wrongNColumns() {
        FileParseUtils fpu = new FileParseUtils();
        try {
            fpu.parsePathwayFile(new File("src/test/resources/test_input/wrong-n-columns.csv"));
            fail("Expected IOException on wrong n column, got no error.");
        } catch (IOException e) {
            assertEquals(
                    "Invalid Pathway file format. Expected at least 2 columns.",
                    e.getMessage()
            );
        }
    }

    @Test
    void parsePathwayGeneFile_wrongNColumns() {
        try {
            fileParseUtils.parsePathwayGeneFile(new File("src/test/resources/test_input/wrong-n-columns.csv"));
            fail("Expected IOException on wrong n column, got no error.");
        } catch (IOException e) {
            assertEquals(
                    "Invalid PathwayGene file format. Expected at least 4 columns.",
                    e.getMessage()
            );
        }
    }

    @Test
    void parseDegsFile_cannotRead() {
        try {
            fileParseUtils.parseDegsFile(new File("src/test/resources/test_input/whooo.csv"));
            fail("Expected IOException on file not exist, got no error.");
        } catch (IOException _) {
            assertTrue(true);
        }
    }
    @Test
    void parsePathwayFile_cannotRead() {
        try {
            fileParseUtils.parsePathwayFile(new File("src/test/resources/test_input/whooo.csv"));
            fail("Expected IOException on file not exist, got no error.");
        } catch (IOException _) {
            assertTrue(true);
        }
    }
    @Test
    void parsePathwayGeneFile_cannotRead() {
        try {
            fileParseUtils.parsePathwayGeneFile(new File("src/test/resources/test_input/whooo.csv"));
            fail("Expected IOException on file not exist, got no error.");
        } catch (IOException _) {
            assertTrue(true);
        }
    }

    @Test
    void parseDegsFile_noFile() {
        try {
            fileParseUtils.parseDegsFile(null);
            fail("Expected IOException on file null, got no error.");
        } catch (IOException _) {
            assertTrue(true);
        }
    }
    @Test
    void parsePathwayFile_noFile() {
        try {
            fileParseUtils.parsePathwayFile(null);
            fail("Expected IOException on f on file not existile null, got no error.");
        } catch (IOException _) {
            assertTrue(true);
        }
    }
    @Test
    void parsePathwayGeneFile_noFile() {
        try {
            fileParseUtils.parsePathwayGeneFile(null);
            fail("Expected IOException on file null, got no error.");
        } catch (IOException _) {
            assertTrue(true);
        }
    }
}