package nl.ramsolutions.sw.magik.jacoco.generators;

import nl.ramsolutions.sw.magik.jacoco.conversion.MagikBundleCoverageConverter;
import nl.ramsolutions.sw.magik.jacoco.sw5lib.Sw5LibReader;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.tools.ExecFileLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Base report generator.
 */
public abstract class BaseReportGenerator {

    private static final String LIBS_DIR = "libs";
    private static final String DEFAULT_NAME = "Smallworld product";

    private final List<Path> productPaths;
    private final File outputFile;
    private final File executionDataFile;
    private final boolean filterExecutableClasses;
    private final ExecFileLoader execFileLoader = new ExecFileLoader();
    private Sw5LibReader libReader;

    /**
     * Constructor.
     *
     * <p>
     * The product is expected to contain the {@literal libs} directory containing the compiled product.
     * </p>
     *
     * @param productDirectory File to product directory.
     * @param executionDataFile File to {@literal jacoco.exec}.
     * @param outputFile File to report directory.
     * @param filterExecutableClasses Filter executable classes.
     */
    protected BaseReportGenerator(
            final List<Path> productPaths,
            final File executionDataFile,
            final File outputFile,
            final boolean filterExecutableClasses) {
        this.productPaths = productPaths;
        this.executionDataFile = executionDataFile;
        this.outputFile = outputFile;
        this.filterExecutableClasses = filterExecutableClasses;
    }

    protected File getOutputFile() {
        return this.outputFile;
    }

    protected ExecFileLoader getExecFileLoader() {
        return this.execFileLoader;
    }

    protected MagikDirectorySourceFileLocator getLocator() {
        return new MagikDirectorySourceFileLocator(this.productPaths);
    }

    protected MagikNames getMagikNames() {
        return new MagikNames(this.libReader);
    }

    /**
     * Run the report generation.
     * @throws IOException -
     */
    public void run() throws IOException {
        this.loadExecutionData();
        this.loadSw5Libs();

        final IBundleCoverage bundleCoverage = this.analyzeStructure();
        this.createReport(bundleCoverage);
    }

    /**
     * Create the report.
     * @param bundleCoverage {@link IBundleCoverage}, converted to Magik style.
     * @throws IOException -
     */
    protected abstract void createReport(IBundleCoverage bundleCoverage) throws IOException;

    private IBundleCoverage analyzeStructure() throws IOException {
        // Analyze classes (JaCoCo).
        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final ExecutionDataStore dataStore = this.execFileLoader.getExecutionDataStore();
        final Analyzer analyzer = new Analyzer(dataStore, coverageBuilder);
        for (final Path productPath : this.productPaths) {
            final File libsDirectory = new File(productPath.toFile(), LIBS_DIR);
            analyzer.analyzeAll(libsDirectory);
        }
        final String name = DEFAULT_NAME;  // TODO: Make this configurable through a command line param?
        final IBundleCoverage bundleCoverage = coverageBuilder.getBundle(name);

        // Merge method coverages (Magik), filter executable classes if needed.
        final MagikBundleCoverageConverter bundleCoverageConverter =
            new MagikBundleCoverageConverter(this.libReader, bundleCoverage, this.filterExecutableClasses);
        return bundleCoverageConverter.convert();
    }

    private void loadExecutionData() throws IOException {
        this.execFileLoader.load(executionDataFile);
    }

    private void loadSw5Libs() throws IOException {
        this.libReader = new Sw5LibReader(this.productPaths);
    }

}
