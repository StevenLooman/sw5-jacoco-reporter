package nl.ramsolutions.sw.magik.jacoco.generators.cobertura;

import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.report.IReportGroupVisitor;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.internal.xml.XMLElement;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

public class CoberturaXmlVisitor implements IReportVisitor {

    private static final String OUTPUT_ENCODING = "UTF-8";
    private static final String PUBID = "-//COBERTURA//DTD Report 04//EN";
    private static final String SYSTEM = "coverage-04.dtd";
    private static final String LINE_RATE = "line-rate";
    private static final String BRANCH_RATE = "branch-rate";
    private static final String COMPLEXITY = "complexity";

    private final OutputStream output;
    private final List<Path> sourcePaths;
    private XMLElement rootElement;

    CoberturaXmlVisitor(final OutputStream output, final List<Path> sourcePaths) {
        this.output = output;
        this.sourcePaths = sourcePaths;
    }

    public void visitInfo(
            final List<SessionInfo> sessionInfos, final Collection<ExecutionData> executionData) throws IOException {
        // Don't need this.
    }

    public void visitBundle(final IBundleCoverage bundle, final ISourceFileLocator locator) throws IOException {
        this.createRootElement(bundle);

        this.writeBundle(bundle);
    }

    public IReportGroupVisitor visitGroup(final String name) throws IOException {
        throw new IllegalStateException("Unexpected visit group");
    }

    private void createRootElement(final IBundleCoverage bundleCoverage) throws IOException {
        this.rootElement = new XMLElement("coverage", PUBID, SYSTEM, true, OUTPUT_ENCODING, this.output);

        final int timestamp = 0;  // TODO
        this.rootElement.attr("timestamp", timestamp);

        final ICounter lineCounter = bundleCoverage.getLineCounter();
        final double lineRate = lineCounter.getCoveredRatio();
        this.rootElement.attr(LINE_RATE, Double.toString(lineRate));

        final ICounter branchCounter = bundleCoverage.getBranchCounter();
        final double branchRate = branchCounter.getCoveredRatio();
        this.rootElement.attr(BRANCH_RATE, Double.toString(branchRate));

        final ICounter complexityCounter = bundleCoverage.getComplexityCounter();
        final int complexity = complexityCounter.getTotalCount();
        this.rootElement.attr(COMPLEXITY, complexity);

        final XMLElement sourcesEl = this.rootElement.element("sources");
        if (!this.sourcePaths.isEmpty()) {
            this.sourcePaths.forEach(sourcePath -> this.writeSourcePath(sourcesEl, sourcePath));
        } else {
            final Path dotPath = Path.of(".");
            this.writeSourcePath(sourcesEl, dotPath);
        }
    }

    private void writeSourcePath(final XMLElement sourcesEl, final Path path) {
        try {
            final XMLElement sourceEl = sourcesEl.element("source");
            final String pathStr = path.toString();
            sourceEl.text(pathStr);
        } catch (final IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private void writeBundle(final IBundleCoverage bundleCoverage) {
        bundleCoverage.getPackages().forEach(this::writePackage);
    }

    private void writePackage(final IPackageCoverage packageCoverage) {
        try {
            final XMLElement packageEl = this.rootElement.element("package");
            final String packageName = packageCoverage.getName();
            packageEl.attr("name", packageName);

            final ICounter lineCounter = packageCoverage.getLineCounter();
            final double lineRate = lineCounter.getCoveredRatio();
            packageEl.attr(LINE_RATE, Double.toString(lineRate));

            final ICounter branchCounter = packageCoverage.getBranchCounter();
            final double branchRate = branchCounter.getCoveredRatio();
            packageEl.attr(BRANCH_RATE, Double.toString(branchRate));

            final ICounter complexityCounter = packageCoverage.getComplexityCounter();
            final int complexity = complexityCounter.getTotalCount();
            packageEl.attr(COMPLEXITY, complexity);

            final XMLElement classesEl = packageEl.element("classes");
            packageCoverage.getClasses().forEach(classCoverage -> this.writeClass(classesEl, classCoverage));
        } catch (final IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private void writeClass(final XMLElement classesEl, final IClassCoverage classCoverage) {
        try {
            final XMLElement classEl = classesEl.element("class");
            final String className = classCoverage.getName();
            classEl.attr("name", className);

            final String filename = classCoverage.getSourceFileName();
            classEl.attr("filename", filename);

            final ICounter lineCounter = classCoverage.getLineCounter();
            final double lineRate = lineCounter.getCoveredRatio();
            classEl.attr(LINE_RATE, Double.toString(lineRate));

            final ICounter branchCounter = classCoverage.getBranchCounter();
            final double branchRate = branchCounter.getCoveredRatio();
            classEl.attr(BRANCH_RATE, Double.toString(branchRate));

            final ICounter complexityCounter = classCoverage.getComplexityCounter();
            final int complexity = complexityCounter.getTotalCount();
            classEl.attr(COMPLEXITY, complexity);

            final XMLElement methodsEl = classEl.element("methods");
            classCoverage.getMethods().forEach(methodCoverage -> this.writeMethod(methodsEl, methodCoverage));

            final XMLElement linesEl = classEl.element("lines");
            final int firstLine = classCoverage.getFirstLine();
            final int lastLine = classCoverage.getLastLine();
            IntStream.range(firstLine, lastLine + 1)
                .filter(lineNo -> classCoverage.getLine(lineNo).getStatus() != ICounter.EMPTY)
                .forEach(lineNo -> {
                    final ILine line = classCoverage.getLine(lineNo);
                    this.writeLine(linesEl, lineNo, line);
                });
        } catch (final IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private void writeMethod(final XMLElement methodsEl, final IMethodCoverage methodCoverage) {
        try {
            final XMLElement methodEl = methodsEl.element("method");
            final String methodName = methodCoverage.getName();
            methodEl.attr("name", methodName);

            final String signature = "";
            methodEl.attr("signature", signature);

            final ICounter lineCounter = methodCoverage.getLineCounter();
            final double lineRate = lineCounter.getCoveredRatio();
            methodEl.attr(LINE_RATE, Double.toString(lineRate));

            final ICounter branchCounter = methodCoverage.getBranchCounter();
            final double branchRate = branchCounter.getCoveredRatio();
            methodEl.attr(BRANCH_RATE, Double.toString(branchRate));

            final ICounter complexityCounter = methodCoverage.getComplexityCounter();
            final int complexity = complexityCounter.getTotalCount();
            methodEl.attr(COMPLEXITY, complexity);

            final XMLElement linesEl = methodEl.element("lines");
            final int firstLine = methodCoverage.getFirstLine();
            final int lastLine = methodCoverage.getLastLine();
            IntStream.range(firstLine, lastLine + 1)
                .filter(lineNo -> methodCoverage.getLine(lineNo).getStatus() != ICounter.EMPTY)
                .forEach(lineNo -> {
                    final ILine line = methodCoverage.getLine(lineNo);
                    this.writeLine(linesEl, lineNo, line);
                });

        } catch (final IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private void writeLine(final XMLElement linesEl, final int lineNo, final ILine line) {
        try {
            final XMLElement lineEl = linesEl.element("line");
            lineEl.attr("number", lineNo);

            final ICounter instructionCounter = line.getInstructionCounter();
            final int hits = instructionCounter.getCoveredCount() > 0
                ? 1
                : 0;
            lineEl.attr("hits", hits);

            final ICounter branchCounter = line.getBranchCounter();
            final String branch = branchCounter.getTotalCount() > 0
                ? "true"
                : "false";
            lineEl.attr("branch", branch);
        } catch (final IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public void visitEnd() throws IOException {
        this.rootElement.close();
    }

}
