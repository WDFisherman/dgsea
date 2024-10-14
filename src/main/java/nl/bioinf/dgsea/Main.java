package nl.bioinf.dgsea;

import picocli.CommandLine;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        int exitCode;
        System.out.println("args = " + Arrays.toString(args));
        exitCode = new CommandLine(new CommandlineController()).execute(args);
//        System.exit(exitCode);
    }

}
