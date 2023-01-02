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

/**
 * Base report generator.
 */
public abstract class BaseReportGenerator {

    private final File productDirectory;
    private final File outputFile;
    private final File executionDataFile;
    private final boolean filterPrimaryClasses;
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
     * @param filterPrimaryClasses Filter primary classes.
     */
    protected BaseReportGenerator(
            final File productDirectory,
            final File executionDataFile,
            final File outputFile,
            final boolean filterPrimaryClasses) {
        this.productDirectory = productDirectory;
        this.executionDataFile = executionDataFile;
        this.outputFile = outputFile;
        this.filterPrimaryClasses = filterPrimaryClasses;
    }

    protected File getOutputFile() {
        return this.outputFile;
    }

    protected ExecFileLoader getExecFileLoader() {
        return this.execFileLoader;
    }

    protected MagikDirectorySourceFileLocator getLocator() {
        return new MagikDirectorySourceFileLocator(this.productDirectory);
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
        final File libsDirectory = new File(productDirectory, "libs");
        analyzer.analyzeAll(libsDirectory);
        final String title = this.productDirectory.getName();
        final IBundleCoverage bundleCoverage = coverageBuilder.getBundle(title);

        // Merge method coverages (Magik), filter primary classes if needed.
        final MagikBundleCoverageConverter bundleCoverageConverter = new MagikBundleCoverageConverter(this.libReader);
        return bundleCoverageConverter.convert(bundleCoverage, this.filterPrimaryClasses);
    }

    private void loadExecutionData() throws IOException {
        this.execFileLoader.load(executionDataFile);
    }

    private void loadSw5Libs() throws IOException {
        this.libReader = new Sw5LibReader(this.productDirectory.toPath());
    }

}
