# DGSEA-tools

DGSEA-tools provide several tools to gain insights into gene-enrichment data as part of a differential gene set enrichment analysis.
Using results of this analysis tool you will be able to:
* Get count data on 2 aspects of degs for every pathway: in or not in pathway and presence of significance
* Compare enrichment scores between pathways in a bar-chart
* Compare enrichment scores and its relationship with p- significance values between pathways in a dot-chart
* Compare pathways average deg(differently expressed gene) log-fold-change with one another in a bar-chart

Differential gene set enrichment analysis relies on the output data of a differential gene expression analysis(dgea). 
This in the form of log-fold-change data and significance measured as a p-value. 
This can be formed from count sequencing data that can be linked to gene, 
    from either a [microarray](https://www.genome.gov/genetics-glossary/Microarray-Technology), [*-omics](https://www.britannica.com/science/omics)(cDNA, RNA, proteins) expression sequencing, or similar background.
Note: As of writing this tool can only process single-omics data.

## Result (interpretation)

(todo: add visuals from tool and guide how to interpret these)

## Installation

This tool requires JRE 22 to be installed on your computer. 
For just running the application we recommend downloading the pre-build jar in this repo. See: `builds/`.

## Usage
Basic test usage, based on the test data in this repo under: `test_data/`:
```bash
& java.exe -jar dgsea-1.0-SNAPSHOT-24w41b.jar perc_lfc_per_pathway_chart test_data/degs.csv test_data/hsa_pathways.csv test_data/pathways.csv outputPathImage.png
```

```shell
java -jar dgsea-1.0-SNAPSHOT-24w41b.jar perc_lfc_per_pathway_chart test_data/degs.csv test_data/hsa_pathways.csv test_data/pathways.csv outputPathImage.png
```

You can also run any of the other commands:
```console
foo@bar: java -jar help
args = [help]
Usage: main [-hV] [COMMAND]
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  help                        Display help information about the specified
                                command.
  enrich_bar_chart            No description yet
  enrich_dot_chart            This command over-aches commands for now:
                                con_table, enrich_bar_chart and enrich_dot_chart
  perc_lfc_per_pathway_chart  Makes a bar-chart showing ratio's average
                                log-fold-change on differently expressed genes
                                per pathway.
  con_table                   No description yet
```

## Support
In case you are having trouble running this application, or you're discovered a bug, 
    you are always welcome to send an e-mail(see section: Authors and acknowledgment).

## Contributing
We would like to inform that there are no outside contributions needed. You are welcome to send us feature suggestions and bug-reports.

## Authors and acknowledgment
Made by:
* Jort Gommers: j.r.gommers@st.hanze.nl
* Willem Daniël Visser: wi.d.visser@st.hanze.nl
Data contribution:
* Marcel Kempenaar: m.kempenaar@pl.hanze.nl

## License
No specific licensing applies 