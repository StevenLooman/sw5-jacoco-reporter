package nl.ramsolutions.sw.magik.jacoco.generators;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import nl.ramsolutions.sw.magik.jacoco.VersionInfo;
import nl.ramsolutions.sw.magik.jacoco.conversion.MagikBundleCoverageConverter;
import nl.ramsolutions.sw.magik.jacoco.helpers.ExecFilesLoader;
import nl.ramsolutions.sw.magik.jacoco.helpers.SmallworldProducts;
import nl.ramsolutions.sw.magik.jacoco.helpers.SmallworldProductsSourceFileLocator;
import nl.ramsolutions.sw.magik.jaranalyzer.MethodDefinition;
import nl.ramsolutions.sw.magik.jaranalyzer.SwJarAnalyzerAnalyzer;
import nl.ramsolutions.sw.magik.jaranalyzer.SwJarAnalyzerReader;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.MultiSourceFileLocator;

/** Base report generator. */
public abstract class BaseReportGenerator {

  private static final String LIBS_DIR = "libs";
  private static final int TAB_WIDTH = 8;

  private final List<Path> productPaths;
  private final List<Path> sourcePaths;
  private final File outputFile;
  private final List<File> executionDataFiles;
  private final boolean discardExecutable;
  private final boolean discardNonMagik;
  private final String bundleName;
  private final ExecFileLoader execFileLoader = new ExecFileLoader();
  private SwJarAnalyzerAnalyzer libAnalyzer;

  /**
   * Constructor.
   *
   * <p>The product is expected to contain the {@literal libs} directory containing the compiled
   * product.
   *
   * @param productPaths Paths to Smallworld product directories.
   * @param sourcePaths Paths to regular (Java) source directories.
   * @param executionDataFiles Files to {@literal jacoco.exec}, coverage is merged.
   * @param outputFile File to report directory.
   * @param discardExecutable Discard executable.
   * @param discardNonMagik Discard non-Magik code.
   * @param bundleName Name of the bundle.
   */
  protected BaseReportGenerator(
      final List<Path> productPaths,
      final List<Path> sourcePaths,
      final List<File> executionDataFiles,
      final File outputFile,
      final boolean discardExecutable,
      final boolean discardNonMagik,
      final String bundleName) {
    this.productPaths = productPaths;
    this.sourcePaths = sourcePaths;
    this.executionDataFiles = executionDataFiles;
    this.outputFile = outputFile;
    this.discardExecutable = discardExecutable;
    this.discardNonMagik = discardNonMagik;
    this.bundleName = bundleName;
  }

  protected File getOutputFile() {
    return this.outputFile;
  }

  protected List<Path> getProductPaths() {
    return Collections.unmodifiableList(this.productPaths);
  }

  protected List<Path> getSourcePaths() {
    return Collections.unmodifiableList(this.sourcePaths);
  }

  @CheckForNull
  protected ExecFileLoader getExecFileLoader() {
    return this.execFileLoader;
  }

  protected ISourceFileLocator getLocator() {
    final MultiSourceFileLocator locator = new MultiSourceFileLocator(TAB_WIDTH);

    // Add Smallworld product source file locator.
    final SmallworldProducts smallworldProducts = new SmallworldProducts(this.productPaths);
    final SmallworldProductsSourceFileLocator productSourceFileLocator =
        new SmallworldProductsSourceFileLocator(smallworldProducts);
    locator.add(productSourceFileLocator);

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
   *
   * @throws IOException -
   */
  public void run() throws IOException {
    this.reportVersions();

    this.loadExecutionData();
    this.loadLibs();

    this.reportDuplicates();

    final IBundleCoverage bundleCoverage = this.analyzeStructure();
    this.createReport(bundleCoverage);
  }

  /**
   * Report the versions in use.
   *
   * <p>The {@literal jacoco.exec} file must be recorded with a JaCoCo agent of the same version as
   * the JaCoCo library used here, as probes recorded by another version can be interpreted
   * incorrectly.
   */
  private void reportVersions() {
    VersionInfo.printTo(System.err);
  }

  /** Report duplicate {@link MethodDefinition}s. */
  private void reportDuplicates() {
    final Map<String, List<MethodDefinition>> duplicates =
        this.libAnalyzer.getDuplicateMethodDefinitions();
    if (!duplicates.isEmpty()) {
      System.err.println("Warning: Duplicate method definitions found:");
      duplicates.forEach(
          (methodName, methodDefs) -> {
            System.err.println(
                "  "
                    + methodName
                    + " defined in files: "
                    + methodDefs.stream()
                        .map(MethodDefinition::getSourceFile)
                        .map(Object::toString)
                        .collect(Collectors.joining(", ")));
          });
    }
  }

  /**
   * Report classes for which the execution data does not match the class in the {@literal libs}.
   *
   * <p>The coverage of these classes is lost, as JaCoCo matches the execution data to the class by
   * a checksum of the class. This happens when the {@literal libs} are recompiled after recording
   * the {@literal jacoco.exec} file(s).
   *
   * @param coverageBuilder {@link CoverageBuilder} which analyzed the classes.
   */
  private void reportNoMatchClasses(final CoverageBuilder coverageBuilder) {
    final Collection<IClassCoverage> noMatchClasses = coverageBuilder.getNoMatchClasses();
    if (!noMatchClasses.isEmpty()) {
      System.err.println(
          "Warning: Execution data of classes do not match the classes in libs, "
              + "coverage of these classes is lost. "
              + "Were the libs recompiled after recording?");
      noMatchClasses.stream()
          .map(IClassCoverage::getName)
          .sorted()
          .forEach(className -> System.err.println("  " + className));
    }
  }

  /**
   * Create the report.
   *
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
      final File libsDirectory = productPath.resolve(LIBS_DIR).toFile();
      analyzer.analyzeAll(libsDirectory);
    }
    final IBundleCoverage bundleCoverage = coverageBuilder.getBundle(this.bundleName);
    this.reportNoMatchClasses(coverageBuilder);

    // Merge method coverages (Magik), discard executable parts if needed.
    final MagikBundleCoverageConverter bundleCoverageConverter =
        new MagikBundleCoverageConverter(
            this.libAnalyzer, bundleCoverage, this.discardExecutable, this.discardNonMagik);
    return bundleCoverageConverter.convert();
  }

  private void loadExecutionData() throws IOException {
    ExecFilesLoader.load(this.execFileLoader, this.executionDataFiles);
  }

  private void loadLibs() throws IOException {
    final SwJarAnalyzerReader libReader = new SwJarAnalyzerReader(this.productPaths);
    this.libAnalyzer = new SwJarAnalyzerAnalyzer(libReader);
  }
}
