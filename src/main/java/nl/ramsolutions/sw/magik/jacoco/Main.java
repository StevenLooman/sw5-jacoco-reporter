package nl.ramsolutions.sw.magik.jacoco;

import java.io.File;
import java.io.IOException;
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

/**
 * Main entry point.
 */
public final class Main {

    private static final Options OPTIONS;
    private static final String OPTION_HELP = "help";
    private static final String OPTION_PRODUCT_DIR = "product-dir";
    private static final String OPTION_JACOCO_FILE = "jacoco-file";
    private static final String OPTION_FILTER_PRIMARY = "filter-primary";
    private static final String OPTION_HTML = "html";
    private static final String OPTION_XML = "xml";

    static {
        OPTIONS = new Options();
        OPTIONS.addOption(Option.builder()
            .longOpt(OPTION_HELP)
            .desc("Show this help")
            .build());
        OPTIONS.addOption(Option.builder()
            .longOpt(OPTION_FILTER_PRIMARY)
            .desc("Filter primary classes").build());
        OPTIONS.addOption(Option.builder()
            .longOpt(OPTION_PRODUCT_DIR)
            .desc("Product directory")
            .numberOfArgs(Option.UNLIMITED_VALUES)
            .required()
            .type(PatternOptionBuilder.EXISTING_FILE_VALUE)
            .build());
        OPTIONS.addOption(Option.builder()
            .longOpt(OPTION_JACOCO_FILE)
            .desc("Path to jacoco.exec")
            .hasArg()
            .required()
            .type(PatternOptionBuilder.EXISTING_FILE_VALUE)
            .build());
        OPTIONS.addOption(Option.builder()
            .longOpt(OPTION_HTML)
            .desc("Output HTML report to directory")
            .hasArg()
            .type(PatternOptionBuilder.EXISTING_FILE_VALUE)
            .build());
        OPTIONS.addOption(Option.builder()
            .longOpt(OPTION_XML)
            .desc("Output XML report to file")
            .hasArg()
            .type(PatternOptionBuilder.EXISTING_FILE_VALUE)
            .build());
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
        final boolean filterPrimaryClasses = commandLine.hasOption(OPTION_FILTER_PRIMARY);

        if (commandLine.hasOption(OPTION_HTML)) {
            final File outputDir = (File) commandLine.getParsedOptionValue(OPTION_HTML);
            final HtmlReportGenerator htmlReportGenerator =
                new HtmlReportGenerator(productDir, executionDataFile, outputDir, filterPrimaryClasses);
            htmlReportGenerator.run();
        } else if (commandLine.hasOption(OPTION_XML)) {
            final File outputFile = (File) commandLine.getParsedOptionValue(OPTION_XML);
            final XmlReportGenerator xmlReportGenerator =
                new XmlReportGenerator(productDir, executionDataFile, outputFile, filterPrimaryClasses);
            xmlReportGenerator.run();
        }
    }

}
