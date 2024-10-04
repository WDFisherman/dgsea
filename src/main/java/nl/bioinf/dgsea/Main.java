package nl.bioinf.dgsea;

import picocli.CommandLine;

import java.util.Arrays;
import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        int exitCode;
        exitCode = new CommandLine(new CommandlineController()).execute(args);
        System.exit(exitCode);
        Main main = new Main();
        main.start(args);
    }
    public void start(String[] args) {
        System.out.println("args.toString() = " + Arrays.toString(args));
    }
}
