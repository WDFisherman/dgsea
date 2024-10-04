package nl.bioinf.dgsea.table_outputs;

import nl.bioinf.dgsea.data_processing.Deg;
import nl.bioinf.dgsea.data_processing.Pathway;
import nl.bioinf.dgsea.data_processing.PathwayGene;

import java.util.List;

public abstract class Table {
    public static List<Deg> degs;
    public static List<Pathway> pathways;
    public static List<PathwayGene> pathwayGenes;

    public static void getTwoByTwoContingencyTable(TableOutputOptions tableOutputOption, String[] selectedPathways) {
        int sumInPathway = Table.getSumInPathway(selectedPathways[0]);
        int sumTotalPathway = Table.getSumTotalPathway(); // all non-and-significant degs
        int sumOutPathway = sumTotalPathway - sumInPathway;
        int sumIsSignificantDeg = getSumIsSignificantDeg();
        int sumTotalDeg = getSumTotalDeg(); // all not- and- in pathway
        int sumIsNotSigDeg = sumTotalDeg - sumIsSignificantDeg;
        int ratioInPathway;
        int ratioOutPathway;
        if(sumOutPathway != 0) {ratioInPathway =  sumInPathway / sumOutPathway;} else { ratioInPathway = 1;}
        if(sumInPathway != 0) {ratioOutPathway =  sumOutPathway / sumInPathway;} else { ratioOutPathway = 1;}
        String result = """
Notch singnaling pathway (%s)
     | D | D* | Sum
   C | %s | %s  | %s
  C* | %s | %s  | %s
 Sum | %s | %s  | %s

 C: in pathway, C*: not in pathway
 D: DEG (FDR <= 0.01), D*: non DEG""".formatted(
                selectedPathways[0],
                sumIsSignificantDeg*ratioInPathway, sumIsNotSigDeg*ratioInPathway, sumInPathway,
                sumIsSignificantDeg*ratioOutPathway, sumIsNotSigDeg*ratioOutPathway, sumOutPathway,
                sumIsSignificantDeg, sumIsNotSigDeg, sumTotalDeg

        );
        if (tableOutputOption == TableOutputOptions.PRINT) {
            System.out.println(result);
        }
    }

    private static int getSumInPathway(String pathway) {
        int count = 0;
        for (PathwayGene gene : pathwayGenes) {
            if (gene.pathwayId().equals(pathway)) {
                count++;
            }
        }
        return count;
    }

    private static int getSumTotalPathway() {
        return pathwayGenes.size();
    }

    private static int getSumIsSignificantDeg() {
        return (int) degs.stream().filter(deg -> deg.adjustedPValue() < 0.01).count();
    }

    private static int getSumTotalDeg() {
        return degs.size();
    }
}

