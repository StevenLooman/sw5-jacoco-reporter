package nl.ramsolutions.sw.magik.jacoco.generators;

import nl.ramsolutions.sw.magik.jacoco.generators.cobertura.CoberturaXmlFormatter;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CoberturaXmlReportGenerator extends BaseReportGenerator {

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
     * @param discardNonMagik Discard non-Magik code.
     * @param bundleName Name of the bundle.
     */
    public CoberturaXmlReportGenerator(
            final List<Path> productPaths,
            final List<Path> sourcePaths,
            final File executionDataFile,
            final File outputFile,
            final boolean discardExecutable,
            final boolean discardNonMagik,
            final String bundleName) {
        super(productPaths, sourcePaths, executionDataFile, outputFile, discardExecutable, discardNonMagik, bundleName);
    }

    @Override
    protected void createReport(final IBundleCoverage bundleCoverage) throws IOException {
        final CoberturaXmlFormatter coberturaXmlFormatter = new CoberturaXmlFormatter();
        final File outputFile = this.getOutputFile();
        final List<Path> sourcePaths = Stream.concat(
                this.getProductPaths().stream(),
                this.getSourcePaths().stream())
            .collect(Collectors.toList());
        try (FileOutputStream output = new FileOutputStream(outputFile)) {
            final IReportVisitor visitor = coberturaXmlFormatter.createVisitor(output, sourcePaths);

            final ExecFileLoader execFileLoader = this.getExecFileLoader();
            final List<SessionInfo> infos = execFileLoader.getSessionInfoStore().getInfos();
            final Collection<ExecutionData> contents = execFileLoader.getExecutionDataStore().getContents();
            visitor.visitInfo(infos, contents);

            final ISourceFileLocator locator = this.getLocator();
            visitor.visitBundle(bundleCoverage, locator);

            visitor.visitEnd();
        }
    }

}
