package nl.bioinf.dgsea.table_outputs;

import nl.bioinf.dgsea.data_processing.Deg;
import nl.bioinf.dgsea.data_processing.Pathway;
import nl.bioinf.dgsea.data_processing.PathwayGene;

import java.util.List;

public class Table {
    private List<Deg> degs;
    private List<Pathway> pathways;
    private List<PathwayGene> pathwayGenes;

    public String getTwoByTwoContingencyTable() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private int getSumInPathway() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private int getSumTotalPathway() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private int getSumIsSignificantDeg() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private int getSumTotalDeg() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void setDegs(List<Deg> degs) {
        this.degs = degs;
    }

    public void setPathways(List<Pathway> pathways) {
        this.pathways = pathways;
    }

    public void setPathwayGenes(List<PathwayGene> pathwayGenes) {
        this.pathwayGenes = pathwayGenes;
    }
}

