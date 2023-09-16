package nl.ramsolutions.sw.magik.jacoco.generators.sonar;

import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.report.IReportGroupVisitor;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.internal.xml.XMLElement;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

public class SonarXmlVisitor implements IReportVisitor {

    private static final String OUTPUT_ENCODING = "UTF-8";
    private static final String PUBID = "-//SONARQUBE//DTD Report 1.0//EN";
    private static final String SYSTEM = "generic-coverage.dtd";

    private final OutputStream output;
    private XMLElement rootElement;

    SonarXmlVisitor(final OutputStream output) {
        this.output = output;
    }

    public void visitInfo(
            final List<SessionInfo> sessionInfos, final Collection<ExecutionData> executionData) throws IOException {
        // Don't need this.
    }

    public void visitBundle(final IBundleCoverage bundle, final ISourceFileLocator locator) throws IOException {
        final String bundleName = bundle.getName();
        this.createRootElement(bundleName);

        this.writeBundle(bundle);
    }

    public IReportGroupVisitor visitGroup(final String name) throws IOException {
        throw new IllegalStateException("Unexpected visit group");
    }

    private void createRootElement(final String bundleName) throws IOException {
        this.rootElement = new XMLElement("coverage", PUBID, SYSTEM, true, OUTPUT_ENCODING, this.output);
        this.rootElement.attr("version", 1);
        this.rootElement.attr("bundle", bundleName);
    }

    private void writeBundle(final IBundleCoverage bundleCoverage) {
        bundleCoverage.getPackages().forEach(this::writePackage);
    }

    private void writePackage(final IPackageCoverage packageCoverage) {
        packageCoverage.getSourceFiles().forEach(this::writeSourceFile);
    }

    private void writeSourceFile(final ISourceFileCoverage sourceFileCoverage) {
        try {
            final XMLElement fileEl = this.rootElement.element("file");
            final String name = sourceFileCoverage.getName();
            fileEl.attr("path", name);

            final int firstLine = sourceFileCoverage.getFirstLine();
            final int lastLine = sourceFileCoverage.getLastLine();
            IntStream.range(firstLine, lastLine + 1)
                .filter(lineNo -> sourceFileCoverage.getLine(lineNo).getStatus() != ICounter.EMPTY)
                .forEach(lineNo -> {
                    final ILine line = sourceFileCoverage.getLine(lineNo);
                    this.writeLine(fileEl, lineNo, line);
                });
        } catch (final IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private void writeLine(final XMLElement fileEl, final int lineNo, final ILine line) {
        try {
            final XMLElement lineToCoverEl = fileEl.element("lineToCover");
            lineToCoverEl.attr("lineNumber", lineNo);

            final ICounter instructionCounter = line.getInstructionCounter();
            final String covered = instructionCounter.getTotalCount() > 0 && instructionCounter.getCoveredCount() > 0
                ? "true"
                : "false";
            lineToCoverEl.attr("covered", covered);

            final int instructionsToCover = instructionCounter.getTotalCount();
            lineToCoverEl.attr("instructionsToCover", instructionsToCover);

            final ICounter branchesCounter = line.getBranchCounter();
            final int branchesToCover = branchesCounter.getTotalCount();
            final int coveredBranches = branchesCounter.getCoveredCount();
            lineToCoverEl.attr("branchesToCover", branchesToCover);
            lineToCoverEl.attr("coveredBranches", coveredBranches);
        } catch (final IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public void visitEnd() throws IOException {
        this.rootElement.close();
    }

}
