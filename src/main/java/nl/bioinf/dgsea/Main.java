/**
 * Main entry point for the DGSEA application.
 * This class handles command-line input and invokes the appropriate
 * functionality based on the provided arguments.
 */
package nl.bioinf.dgsea;

import picocli.CommandLine;

/**
 * The Main class serves as the entry point for the DGSEA application.
 * It processes command-line arguments and delegates execution to the
 * CommandlineController.
 */
public class Main {

    /**
     * The main method which is the entry point of the application.
     * It prints the command-line arguments received and executes
     * the CommandlineController with those arguments.
     *
     * @param args command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        int exitCode;
        exitCode = new CommandLine(new CommandlineController()).execute(args);
         System.exit(exitCode);
    }
}
