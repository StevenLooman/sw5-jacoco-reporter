package nl.ramsolutions.sw.magik.jacoco.generators;

import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.xml.XMLFormatter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

/**
 * XML Report generator.
 */
public class XmlReportGenerator extends BaseReportGenerator {

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
     * @param outputFile File to report file (XML).
     * @param discardExecutable Discard executable.
     * @param bundleName Name of the bundle.
     */
    public XmlReportGenerator(
            final List<Path> productPaths,
            final List<Path> sourcePaths,
            final File executionDataFile,
            final File outputFile,
            final boolean discardExecutable,
            final String bundleName) {
        super(productPaths, sourcePaths, executionDataFile, outputFile, discardExecutable, bundleName);
    }

    @Override
    protected void createReport(final IBundleCoverage bundleCoverage) throws IOException {
        final XMLFormatter xmlFormatter = new XMLFormatter();
        final File outputFile = this.getOutputFile();
        try (FileOutputStream output = new FileOutputStream(outputFile)) {
            final IReportVisitor visitor = xmlFormatter.createVisitor(output);

            final List<SessionInfo> infos = this.getExecFileLoader().getSessionInfoStore().getInfos();
            final Collection<ExecutionData> contents = this.getExecFileLoader().getExecutionDataStore().getContents();
            visitor.visitInfo(infos, contents);

            final ISourceFileLocator locator = this.getLocator();
            visitor.visitBundle(bundleCoverage, locator);

            visitor.visitEnd();
        }
    }

}
