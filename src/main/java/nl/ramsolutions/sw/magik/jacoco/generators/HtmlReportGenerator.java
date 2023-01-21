package nl.ramsolutions.sw.magik.jacoco.generators;

import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.html.HTMLFormatter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

/**
 * HTML report generaotr.
 */
public final class HtmlReportGenerator extends BaseReportGenerator {

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
     * @param reportDirectory File to report directory (HTML).
     * @param discardExecutable Discard executable.
     * @param bundleName Name of the bundle.
     */
    public HtmlReportGenerator(
            final List<Path> productPaths,
            final List<Path> sourcePaths,
            final File executionDataFile,
            final File reportDirectory,
            final boolean discardExecutable,
            final String bundleName) {
        super(productPaths, sourcePaths, executionDataFile, reportDirectory, discardExecutable, bundleName);
    }

    @Override
    protected void createReport(final IBundleCoverage bundleCoverage) throws IOException {
        final HTMLFormatter htmlFormatter = new HTMLFormatter();

        final MagikNames magikNames = this.getMagikNames();
        htmlFormatter.setLanguageNames(magikNames);

        final File outputFile = this.getOutputFile();
        final FileMultiReportOutput output = new FileMultiReportOutput(outputFile);
        final IReportVisitor visitor = htmlFormatter.createVisitor(output);

        final ExecFileLoader execFileLoader = this.getExecFileLoader();
        final List<SessionInfo> infos = execFileLoader.getSessionInfoStore().getInfos();
        final Collection<ExecutionData> contents = execFileLoader.getExecutionDataStore().getContents();
        visitor.visitInfo(infos, contents);

        final ISourceFileLocator locator = this.getLocator();
        visitor.visitBundle(bundleCoverage, locator);

        visitor.visitEnd();
    }

}
