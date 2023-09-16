package nl.ramsolutions.sw.magik.jacoco;

import nl.ramsolutions.sw.magik.jacoco.generators.CoberturaXmlReportGenerator;
import nl.ramsolutions.sw.magik.jacoco.generators.HtmlReportGenerator;
import nl.ramsolutions.sw.magik.jacoco.generators.JacocoXmlReportGenerator;
import nl.ramsolutions.sw.magik.jacoco.generators.SonarXmlReportGenerator;
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
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Main entry point.
 */
public final class Main {

    private static final String DEFAULT_BUNDLE_NAME = "Smallworld product";

    private static final Options OPTIONS;
    private static final Option OPTION_HELP = Option.builder()
        .longOpt("help")
        .desc("Show this help")
        .build();
    private static final Option OPTION_PRODUCT_PATH = Option.builder()
        .longOpt("product-path")
        .desc("Smallworld Product path")
        .numberOfArgs(Option.UNLIMITED_VALUES)
        .required()
        .type(PatternOptionBuilder.FILE_VALUE)
        .build();
    private static final Option OPTION_SOURCE_PATH = Option.builder()
        .longOpt("source-path")
        .desc("Regular (Java) source path")
        .numberOfArgs(Option.UNLIMITED_VALUES)
        .type(PatternOptionBuilder.FILE_VALUE)
        .build();
    private static final Option OPTION_JACOCO_FILE = Option.builder()
        .longOpt("jacoco-file")
        .desc("Path to jacoco.exec")
        .hasArg()
        .required()
        .type(PatternOptionBuilder.FILE_VALUE)
        .build();
    private static final Option OPTION_DISCARD_EXECUTABLE = Option.builder()
        .longOpt("discard-executable")
        .desc("Discard executable classes").build();
    private static final Option OPTION_HTML = Option.builder()
        .longOpt("html")
        .desc("Output HTML report to directory")
        .hasArg()
        .type(PatternOptionBuilder.FILE_VALUE)
        .build();
    private static final Option OPTION_JACOCO_XML = Option.builder()
        .longOpt("jacoco-xml")
        .desc("Output JaCoCo XML report to file")
        .hasArg()
        .type(PatternOptionBuilder.FILE_VALUE)
        .build();
    private static final Option OPTION_SONAR_XML = Option.builder()
        .longOpt("sonar-xml")
        .desc("Output Sonar XML report to file")
        .hasArg()
        .type(PatternOptionBuilder.FILE_VALUE)
        .build();
    private static final Option OPTION_COBERTURA_XML = Option.builder()
        .longOpt("cobertura-xml")
        .desc("Output Cobertura XML report to file")
        .hasArg()
        .type(PatternOptionBuilder.FILE_VALUE)
        .build();
    private static final Option OPTION_BUNDLE_NAME = Option.builder()
        .longOpt("bundle-name")
        .desc("Name of the bundle, defaults to 'Smallworld product'")
        .hasArg()
        .type(PatternOptionBuilder.STRING_VALUE)
        .build();

    static {
        OPTIONS = new Options();
        OPTIONS.addOption(OPTION_HELP);
        OPTIONS.addOption(OPTION_PRODUCT_PATH);
        OPTIONS.addOption(OPTION_SOURCE_PATH);
        OPTIONS.addOption(OPTION_JACOCO_FILE);
        OPTIONS.addOption(OPTION_DISCARD_EXECUTABLE);
        OPTIONS.addOption(OPTION_HTML);
        OPTIONS.addOption(OPTION_JACOCO_XML);
        OPTIONS.addOption(OPTION_SONAR_XML);
        OPTIONS.addOption(OPTION_COBERTURA_XML);
        OPTIONS.addOption(OPTION_BUNDLE_NAME);
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
            || !commandLine.hasOption(OPTION_HTML)
               && !commandLine.hasOption(OPTION_JACOCO_XML)
               && !commandLine.hasOption(OPTION_SONAR_XML)
               && !commandLine.hasOption(OPTION_COBERTURA_XML);
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

        final List<Path> productPaths = Stream.of(commandLine.getOptionValues(OPTION_PRODUCT_PATH))
            .map(Path::of)
            .collect(Collectors.toList());
        final List<Path> sourcePaths = commandLine.hasOption(OPTION_SOURCE_PATH)
            ? Stream.of(commandLine.getOptionValues(OPTION_SOURCE_PATH))
                .map(Path::of)
                .collect(Collectors.toList())
            : Collections.emptyList();
        final File executionDataFile = (File) commandLine.getParsedOptionValue(OPTION_JACOCO_FILE);
        final boolean discardExecutable = commandLine.hasOption(OPTION_DISCARD_EXECUTABLE);
        final String bundleName = commandLine.hasOption(OPTION_BUNDLE_NAME)
            ? commandLine.getOptionValue(OPTION_BUNDLE_NAME)
            : DEFAULT_BUNDLE_NAME;

        if (commandLine.hasOption(OPTION_HTML)) {
            final File outputDir = (File) commandLine.getParsedOptionValue(OPTION_HTML);
            final HtmlReportGenerator htmlReportGenerator = new HtmlReportGenerator(
                productPaths,
                sourcePaths,
                executionDataFile,
                outputDir,
                discardExecutable,
                bundleName);
            htmlReportGenerator.run();
        } else if (commandLine.hasOption(OPTION_JACOCO_XML)) {
            final File outputFile = (File) commandLine.getParsedOptionValue(OPTION_JACOCO_XML);
            final JacocoXmlReportGenerator xmlReportGenerator = new JacocoXmlReportGenerator(
                productPaths,
                sourcePaths,
                executionDataFile,
                outputFile,
                discardExecutable,
                bundleName);
            xmlReportGenerator.run();
        } else if (commandLine.hasOption(OPTION_SONAR_XML)) {
            final File outputFile = (File) commandLine.getParsedOptionValue(OPTION_SONAR_XML);
            final SonarXmlReportGenerator sonarXmlReportGenerator = new SonarXmlReportGenerator(
                productPaths,
                sourcePaths,
                executionDataFile,
                outputFile,
                discardExecutable,
                bundleName);
            sonarXmlReportGenerator.run();
        } else if (commandLine.hasOption(OPTION_COBERTURA_XML)) {
            final File outputFile = (File) commandLine.getParsedOptionValue(OPTION_COBERTURA_XML);
            final CoberturaXmlReportGenerator coberturaXmlReportGenerator = new CoberturaXmlReportGenerator(
                productPaths,
                sourcePaths,
                executionDataFile,
                outputFile,
                discardExecutable,
                bundleName);
            coberturaXmlReportGenerator.run();
        }
    }

}
