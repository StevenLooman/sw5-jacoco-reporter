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
     * @param productDirectory File to product directory.
     * @param executionDataFile File to {@literal jacoco.exec}.
     * @param reportDirectory File to report directory (HTML).
     * @param filterPrimaryClasses Filter primary classes.
     */
    public HtmlReportGenerator(
            final File productDirectory,
            final File executionDataFile,
            final File reportDirectory,
            final boolean filterPrimaryClasses) {
        super(productDirectory, executionDataFile, reportDirectory, filterPrimaryClasses);
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
