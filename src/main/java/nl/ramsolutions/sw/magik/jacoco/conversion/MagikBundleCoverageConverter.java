package nl.ramsolutions.sw.magik.jacoco.conversion;

import nl.ramsolutions.sw.magik.jacoco.helpers.ClassNodeHelper;
import nl.ramsolutions.sw.magik.jacoco.sw5lib.Sw5LibAnalyzer;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.internal.analysis.BundleCoverageImpl;
import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.jacoco.core.internal.analysis.MethodCoverageImpl;
import org.jacoco.core.internal.analysis.PackageCoverageImpl;
import org.jacoco.core.internal.analysis.SourceFileCoverageImpl;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.CheckForNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Convert native JaCoCo/JVM coverage data to Magik coverage data.
 *
 * <p>
 * Conversion includes:
 * - Methods are merged (__loopbody__ into parent method, etc).
 * - Methods are renamed to Magik-names, where applicable.
 * - Executable classes are discarded, when enabled.
 * </p>
 */
public class MagikBundleCoverageConverter {

    private static final Set<String> PRIMARY_CLASS_METHODS = Set.of(
        "<init>",
        "preload",
        "execute");

    private final Sw5LibAnalyzer libAnalyzer;
    private final IBundleCoverage bundleCoverage;
    private final boolean discardExecutable;

    /**
     * Constructor.
     * @param libAnalyzer Lib reader.
     * @param bundleCoverage Bundle coverage.
     * @param discardExecutable Discard executable.
     */
    public MagikBundleCoverageConverter(
            final Sw5LibAnalyzer libAnalyzer,
            final IBundleCoverage bundleCoverage,
            final boolean discardExecutable) {
        this.libAnalyzer = libAnalyzer;
        this.bundleCoverage = bundleCoverage;
        this.discardExecutable = discardExecutable;
    }

    /**
     * Run the conversion of the {@link IBundleCoverage}.
     * @return Filtered and converted {@link IBundleCoverage}.
     */
    public IBundleCoverage convert() {
        final String name = this.bundleCoverage.getName();
        final List<IPackageCoverage> newPackages = this.bundleCoverage.getPackages().stream()
            .map(this::convert)
            .collect(Collectors.toList());
        final BundleCoverageImpl newBundleCoverage = new BundleCoverageImpl(name, newPackages);

        newBundleCoverage.increment(newPackages);

        return newBundleCoverage;
    }

    private PackageCoverageImpl convert(final IPackageCoverage packageCoverage) {
        final String name = packageCoverage.getName();
        final Collection<IClassCoverage> classCoverages = packageCoverage.getClasses();
        final List<IClassCoverage> newClassCoverages = classCoverages.stream()
            .map(classCoverage -> this.convert(classCoverages, classCoverage))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        final List<ISourceFileCoverage> newSourceFileCoverages = packageCoverage.getSourceFiles().stream()
            .map(sourceFileCoverage -> this.convert(sourceFileCoverage, newClassCoverages))
            .collect(Collectors.toList());
        final PackageCoverageImpl newPackageCoverage =
            new PackageCoverageImpl(name, newClassCoverages, newSourceFileCoverages);

        newPackageCoverage.increment(classCoverages);

        return newPackageCoverage;
    }

    @CheckForNull
    private IClassCoverage convert(
            final Collection<IClassCoverage> classCoverages,
            final IClassCoverage classCoverage) {
        final ClassNode classNode = this.getClassNode(classCoverage);
        if (ClassNodeHelper.isSubsidiaryClassNode(classNode)) {
            // Subsidiary classes are merged later on.
            return null;
        }

        if (!ClassNodeHelper.isPrimaryClassNode(classNode)) {
            // Let regular classes pass through.
            return classCoverage;
        }

        // Create new class coverage.
        final String name = classCoverage.getName();
        final long id = classCoverage.getId();
        final boolean noMatch = classCoverage.isNoMatch();
        final ClassCoverageImpl newClassCoverage = new ClassCoverageImpl(name, id, noMatch);

        // Skip the interface, but set source filename.
        final String sourceFileName = classCoverage.getSourceFileName();
        newClassCoverage.setSourceFileName(sourceFileName);

        // Get Subsidiary class.
        final Map<ClassNode, ClassNode> classNodeDependencies = this.libAnalyzer.getClassDependencyMap();
        final ClassNode subsidiaryClassNode = classNodeDependencies.get(classNode);
        final IClassCoverage subsidiaryClassCoverage = subsidiaryClassNode != null
            ? classCoverages.stream()
                .filter(cc -> cc.getName().equals(subsidiaryClassNode.name))
                .findAny()
                .orElse(null)
            : null;

        // Merge Primary and Subsidiary class methods.
        final MethodCoverageMerger methodCoverageMerger = new MethodCoverageMerger(this.libAnalyzer);
        final Collection<IMethodCoverage> mergedMethodCoverages =
            methodCoverageMerger.run(classCoverage, subsidiaryClassCoverage);
        mergedMethodCoverages.stream()
            .map(methodCoverage -> this.convert(classCoverage, subsidiaryClassCoverage, methodCoverage))
            .filter(Objects::nonNull)
            .forEach(newClassCoverage::addMethod);

        return newClassCoverage;
    }

    /**
     * Convert method coverage.
     * @param primaryClassCoverage Class Coverage for the (merged) Primary class.
     * @param methodCoverage Method Coverage.
     * @return Converted Method Coverage or null.
     */
    private IMethodCoverage convert(
            final IClassCoverage primaryClassCoverage,
            final IClassCoverage subsidiaryClassCoverage,
            final IMethodCoverage methodCoverage) {
        final String methodName = methodCoverage.getName();
        final boolean isPrimaryClassMethod = PRIMARY_CLASS_METHODS.contains(methodName);
        if (this.discardExecutable
            && isPrimaryClassMethod) {
            return null;
        }

        if (isPrimaryClassMethod) {
            // Nothing to discard. Regard this as a regular method.
            return methodCoverage;
        }

        // This is a Magik method, which always lives on the subsidiary class.
        final String javaClassName = subsidiaryClassCoverage.getName();
        final String javaMethodName = methodCoverage.getName();
        final String name =
            this.libAnalyzer.getMagikMethodName(javaClassName, javaMethodName);  // methodCoverage.getName();
        final String desc = "";  // methodCoverage.getDesc();
        final String signature = methodCoverage.getSignature();
        final MethodCoverageImpl newMethodCoverage = new MethodCoverageImpl(name, desc, signature);

        newMethodCoverage.increment(methodCoverage);

        return newMethodCoverage;
    }

    private ISourceFileCoverage convert(
            final ISourceFileCoverage sourceFileCoverage,
            final Collection<IClassCoverage> classCoverages) {
        if (!this.discardExecutable) {
            // We are interested in everything. Return it whole.
            return sourceFileCoverage;
        }

        // Create a copy of the SourceFileCoverage, but strip everything present from the executable part.
        final String name = sourceFileCoverage.getName();
        final String packageName = sourceFileCoverage.getPackageName();
        final SourceFileCoverageImpl newSourceFileCoverage = new SourceFileCoverageImpl(name, packageName);

        // Find newly created ClassCoverage for this file.
        final String sourceFileName = sourceFileCoverage.getName();
        final IClassCoverage relatedClassCoverage = classCoverages.stream()
            .filter(classCoverage -> classCoverage.getSourceFileName().equals(sourceFileName))
            .findAny()
            .orElse(null);
        if (relatedClassCoverage == null) {
            // No lines to add.
            return newSourceFileCoverage;
        }

        // Only copy lines which are not (indirectly) in the primary ClassCoverage.
        for (int nr = sourceFileCoverage.getFirstLine(); nr < sourceFileCoverage.getLastLine(); ++nr) {
            final ILine classCoverageLine = relatedClassCoverage.getLine(nr);
            if (classCoverageLine == null) {
                continue;
            }

            final ICounter branchCounter = classCoverageLine.getBranchCounter();
            final ICounter instructionCounter = classCoverageLine.getInstructionCounter();
            newSourceFileCoverage.increment(instructionCounter, branchCounter, nr);
        }

        return newSourceFileCoverage;
    }

    @CheckForNull
    private IClassCoverage getSubsidiaryClassCoverage(final ISourceFileCoverage sourceFileCoverage) {
        final String name = sourceFileCoverage.getName();
        return this.bundleCoverage.getPackages().stream()
            .flatMap(packageCoverage -> packageCoverage.getClasses().stream())
            .filter(classCoverage -> classCoverage.getSourceFileName().equals(name))
            .filter(classCoverage -> !this.isPrimaryClassCoverage(classCoverage))
            .findFirst()
            .orElse(null);
    }

    private ClassNode getClassNode(final IClassCoverage classCoverage) {
        final String className = classCoverage.getName() + ".class";
        return this.libAnalyzer.getClassByName(className);
    }

    private boolean isPrimaryClassCoverage(final IClassCoverage classCoverage) {
        final ClassNode classNode = getClassNode(classCoverage);
        return ClassNodeHelper.isPrimaryClassNode(classNode);
    }

}
