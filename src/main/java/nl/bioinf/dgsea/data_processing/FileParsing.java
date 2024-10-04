package nl.bioinf.degs.data_processing;

import java.io.File;
import java.util.List;

public class FileParsing {
    private final double adjustedPValueThreshold;
    private final String[] selectedPathwayIds;
    private final String degFilePath;
    private final String pathwayFilePath;

    public FileParsing(double adjustedPValueThreshold, String[] selectedPathwayIds, String degFilePath, String pathwayFilePath) {
        this.adjustedPValueThreshold = adjustedPValueThreshold;
        this.selectedPathwayIds = selectedPathwayIds;
        this.degFilePath = degFilePath;
        this.pathwayFilePath = pathwayFilePath;
    }

    public File getFileFromFilePath(String filePath) throws Exception {
        throw new Exception("Not implemented yet");
    }

    public List<Deg> parseDegsFile(File file) throws Exception {
        throw new Exception("Not implemented yet");
    }

    public List<Pathway> parsePathwayFile(File file) throws Exception {
        throw new Exception("Not implemented yet");
    }

    public List<PathwayGene> parsePathwayGeneFile(File file) throws Exception {
        throw new Exception("Not implemented yet");
    }

}
