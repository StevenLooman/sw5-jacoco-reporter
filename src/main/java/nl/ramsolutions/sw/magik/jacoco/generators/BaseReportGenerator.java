package nl.ramsolutions.sw.magik.jacoco.generators;

import nl.ramsolutions.sw.magik.jacoco.conversion.MagikBundleCoverageConverter;
import nl.ramsolutions.sw.magik.jacoco.sw5lib.Sw5LibAnalyzer;
import nl.ramsolutions.sw.magik.jacoco.sw5lib.Sw5LibReader;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.MultiSourceFileLocator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * Base report generator.
 */
public abstract class BaseReportGenerator {

    private static final String LIBS_DIR = "libs";
    private static final int TAB_WIDTH = 8;

    private final List<Path> productPaths;
    private final List<Path> sourcePaths;
    private final File outputFile;
    private final File executionDataFile;
    private final boolean discardExecutable;
    private final String bundleName;
    private final ExecFileLoader execFileLoader = new ExecFileLoader();
    private Sw5LibAnalyzer libAnalyzer;

    /**
     * Constructor.
     *
     * <p>
     * The product is expected to contain the {@literal libs} directory containing the compiled product.
     * </p>
     *
     * @param productPaths Paths to Smallworld product directories.
     * @param sourcePaths Paths to regular (Java) source directories.
     * @param executionDataFile File to {@literal jacoco.exec}.
     * @param outputFile File to report directory.
     * @param discardExecutable Discard executable.
     * @param bundleName Name of the bundle.
     */
    protected BaseReportGenerator(
            final List<Path> productPaths,
            final List<Path> sourcePaths,
            final File executionDataFile,
            final File outputFile,
            final boolean discardExecutable,
            final String bundleName) {
        this.productPaths = productPaths;
        this.sourcePaths = sourcePaths;
        this.executionDataFile = executionDataFile;
        this.outputFile = outputFile;
        this.discardExecutable = discardExecutable;
        this.bundleName = bundleName;
    }

    protected File getOutputFile() {
        return this.outputFile;
    }

    public List<Path> getSourcePaths() {
        return Collections.unmodifiableList(this.sourcePaths);
    }

    protected ExecFileLoader getExecFileLoader() {
        return this.execFileLoader;
    }

    protected ISourceFileLocator getLocator() {
        final MultiSourceFileLocator locator = new MultiSourceFileLocator(TAB_WIDTH);

        // Add Smallworld product source file locator.
        this.productPaths.stream()
            .map(MagikProductSourceFileLocator::new)
            .forEach(locator::add);

        // Add all regular/Java locators.
        this.sourcePaths.stream()
            .map(sourcePath -> new DirectorySourceFileLocator(sourcePath.toFile(), null, TAB_WIDTH))
            .forEach(locator::add);

        return locator;
    }

    protected MagikNames getMagikNames() {
        return new MagikNames();
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
        final IBundleCoverage bundleCoverage = coverageBuilder.getBundle(this.bundleName);

        // Merge method coverages (Magik), discard executable parts if needed.
        final MagikBundleCoverageConverter bundleCoverageConverter =
            new MagikBundleCoverageConverter(this.libAnalyzer, bundleCoverage, this.discardExecutable);
        return bundleCoverageConverter.convert();
    }

    private void loadExecutionData() throws IOException {
        this.execFileLoader.load(this.executionDataFile);
    }

    private void loadSw5Libs() throws IOException {
        final Sw5LibReader libReader = new Sw5LibReader(this.productPaths);
        this.libAnalyzer = new Sw5LibAnalyzer(libReader);
    }

}
