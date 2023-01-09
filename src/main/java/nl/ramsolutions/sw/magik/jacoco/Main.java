package nl.ramsolutions.sw.magik.jacoco;

import nl.ramsolutions.sw.magik.jacoco.generators.HtmlReportGenerator;
import nl.ramsolutions.sw.magik.jacoco.generators.XmlReportGenerator;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PatternOptionBuilder;

import java.io.File;
import java.io.IOException;

/**
 * Main entry point.
 */
public final class Main {

    private static final Options OPTIONS;
    private static final Option OPTION_HELP = Option.builder()
        .longOpt("help")
        .desc("Show this help")
        .build();
    private static final Option OPTION_PRODUCT_DIR = Option.builder()
        .longOpt("product-dir")
        .desc("Product directory")
        .numberOfArgs(Option.UNLIMITED_VALUES)
        .required()
        .type(PatternOptionBuilder.FILE_VALUE)
        .build();
    private static final Option OPTION_JACOCO_FILE = Option.builder()
        .longOpt("jacoco-file")
        .desc("Path to jacoco.exec")
        .hasArg()
        .required()
        .type(PatternOptionBuilder.FILE_VALUE)
        .build();
    private static final Option OPTION_FILTER_EXECUTABLE = Option.builder()
        .longOpt("filter-executable")
        .desc("Filter executable classes").build();
    private static final Option OPTION_HTML = Option.builder()
        .longOpt("html")
        .desc("Output HTML report to directory")
        .hasArg()
        .type(PatternOptionBuilder.FILE_VALUE)
        .build();
    private static final Option OPTION_XML = Option.builder()
        .longOpt("xml")
        .desc("Output XML report to file")
        .hasArg()
        .type(PatternOptionBuilder.FILE_VALUE)
        .build();

    static {
        OPTIONS = new Options();
        OPTIONS.addOption(OPTION_HELP);
        OPTIONS.addOption(OPTION_FILTER_EXECUTABLE);
        OPTIONS.addOption(OPTION_PRODUCT_DIR);
        OPTIONS.addOption(OPTION_JACOCO_FILE);
        OPTIONS.addOption(OPTION_HTML);
        OPTIONS.addOption(OPTION_XML);
    }

    private Main() {
    }

    /**
     * Parse command line.
     * @param args Arguments from/via OS.
     * @return Parsed {@code CommandLine} object.
     */
    private static CommandLine parseCommandline(final String[] args) throws ParseException {
        final CommandLineParser parser = new DefaultParser();
        return parser.parse(Main.OPTIONS, args);
    }

    private static boolean showHelp(final CommandLine commandLine) {
        return commandLine.hasOption(OPTION_HELP)
            || !commandLine.hasOption(OPTION_HTML) && !commandLine.hasOption(OPTION_XML);
    }

    /**
     * Main entry point.
     *
     * @param args The arguments of the program.
     * @throws IOException -
     * @throws ParseException -
     */
    public static void main(String[] args) throws IOException, ParseException {
        final CommandLine commandLine;
        try {
            commandLine = Main.parseCommandline(args);
        } catch (ParseException exception) {
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("sw5-jacoco-reporter", Main.OPTIONS);

            System.exit(-1);
            return;
        }

        if (Main.showHelp(commandLine)) {
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("sw5-jacoco-reporter", Main.OPTIONS);

            System.exit(0);
            return;
        }

        final File productDir = (File) commandLine.getParsedOptionValue(OPTION_PRODUCT_DIR);
        final File executionDataFile = (File) commandLine.getParsedOptionValue(OPTION_JACOCO_FILE);
        final boolean filterExecutableClasses = commandLine.hasOption(OPTION_FILTER_EXECUTABLE);

        if (commandLine.hasOption(OPTION_HTML)) {
            final File outputDir = (File) commandLine.getParsedOptionValue(OPTION_HTML);
            final HtmlReportGenerator htmlReportGenerator =
                new HtmlReportGenerator(productDir, executionDataFile, outputDir, filterExecutableClasses);
            htmlReportGenerator.run();
        } else if (commandLine.hasOption(OPTION_XML)) {
            final File outputFile = (File) commandLine.getParsedOptionValue(OPTION_XML);
            final XmlReportGenerator xmlReportGenerator =
                new XmlReportGenerator(productDir, executionDataFile, outputFile, filterExecutableClasses);
            xmlReportGenerator.run();
        }
    }

}
