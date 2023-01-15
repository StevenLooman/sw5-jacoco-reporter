package nl.ramsolutions.sw.magik.jacoco.conversion;

import nl.ramsolutions.sw.magik.jacoco.sw5lib.Sw5LibAnalyzer;
import nl.ramsolutions.sw.magik.jacoco.sw5lib.Sw5LibReader;
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

    private final Sw5LibReader libReader;
    private final Sw5LibAnalyzer libAnalyzer;
    private final IBundleCoverage bundleCoverage;
    private final MethodCoverageMerger methodCoverageMerger;
    private final boolean discardExecutableClasses;

    /**
     * Constructor.
     * @param libReader Lib reader.
     * @param bundleCoverage Bundle coverage.
     * @param discardExecutableClasses Discard executable classes.
     */
    public MagikBundleCoverageConverter(
            final Sw5LibReader libReader,
            final IBundleCoverage bundleCoverage,
            final boolean discardExecutableClasses) {
        this.libReader = libReader;
        this.bundleCoverage = bundleCoverage;
        this.methodCoverageMerger = new MethodCoverageMerger(this.libReader);
        this.discardExecutableClasses = discardExecutableClasses;

        this.libAnalyzer = new Sw5LibAnalyzer(this.libReader);
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
        final List<IClassCoverage> classCoverages = packageCoverage.getClasses().stream()
            .filter(this::filterClassCoverage)
            .map(this::convert)
            .collect(Collectors.toList());
        final List<ISourceFileCoverage> sourceFileCoverages = packageCoverage.getSourceFiles().stream()
            .map(this::convert)
            .collect(Collectors.toList());
        final PackageCoverageImpl newPackageCoverage =
            new PackageCoverageImpl(name, classCoverages, sourceFileCoverages);

        newPackageCoverage.increment(classCoverages);

        return newPackageCoverage;
    }

    private ClassCoverageImpl convert(final IClassCoverage classCoverage) {
        // Create new class coverage.
        final String name = classCoverage.getName();
        final long id = classCoverage.getId();
        final boolean noMatch = classCoverage.isNoMatch();
        final ClassCoverageImpl newClassCoverage = new ClassCoverageImpl(name, id, noMatch);

        // Merge "sub-methods" into methods. Convert methods.
        final Collection<IMethodCoverage> mergedMethodCoverages = this.methodCoverageMerger.run(classCoverage);
        mergedMethodCoverages.stream()
            .map(mc -> this.convert(classCoverage, mc))
            .forEach(newClassCoverage::addMethod);

        // Copy other things.
        final String[] interfaceNames = classCoverage.getInterfaceNames();
        newClassCoverage.setInterfaces(interfaceNames);
        final String sourceFileName = classCoverage.getSourceFileName();
        newClassCoverage.setSourceFileName(sourceFileName);

        return newClassCoverage;
    }

    private MethodCoverageImpl convert(final IClassCoverage classCoverage, final IMethodCoverage methodCoverage) {
        final String javaClassName = classCoverage.getName();
        final String javaMethodName = methodCoverage.getName();
        final String name =
            this.libAnalyzer.getMagikMethodName(javaClassName, javaMethodName);  // methodCoverage.getName();
        final String desc = "";  // methodCoverage.getDesc();
        final String signature = methodCoverage.getSignature();
        final MethodCoverageImpl newMethodCoverage = new MethodCoverageImpl(name, desc, signature);

        newMethodCoverage.increment(methodCoverage);

        return newMethodCoverage;
    }

    private ISourceFileCoverage convert(final ISourceFileCoverage sourceFileCoverage) {
        if (!this.discardExecutableClasses) {
            return sourceFileCoverage;
        }

        // Create a copy of the SourceFileCoverage, but strip everything present on the executable class.
        final String name = sourceFileCoverage.getName();
        final String packageName = sourceFileCoverage.getPackageName();
        final SourceFileCoverageImpl newSourceFileCoverage = new SourceFileCoverageImpl(name, packageName);

        // Find subsidiary/non-executable ClassCoverage.
        final IClassCoverage classCoverage = this.getSubsidiaryClassCoverage(sourceFileCoverage);
        if (classCoverage == null) {
            // No lines to add.
            return newSourceFileCoverage;
        }

        // Only copy lines which are not in the non-executable ClassCoverage.
        for (int nr = sourceFileCoverage.getFirstLine(); nr < sourceFileCoverage.getLastLine(); ++nr) {
            final ILine classCoverageLine = classCoverage.getLine(nr);
            if (classCoverageLine == null) {
                continue;
            }

            final ICounter branchCounter = classCoverageLine.getBranchCounter();
            final ICounter instructionCounter = classCoverageLine.getInstructionCounter();
            newSourceFileCoverage.increment(instructionCounter, branchCounter, nr);
        }

        return newSourceFileCoverage;
    }

    private boolean filterClassCoverage(final IClassCoverage classCoverage) {
        if (this.discardExecutableClasses) {
            return !this.isExecutableClass(classCoverage);
        }

        return true;
    }

    @CheckForNull
    private IClassCoverage getSubsidiaryClassCoverage(final ISourceFileCoverage sourceFileCoverage) {
        final String name = sourceFileCoverage.getName();
        return this.bundleCoverage.getPackages().stream()
            .flatMap(packageCoverage -> packageCoverage.getClasses().stream())
            .filter(cc -> cc.getSourceFileName().equals(name))
            .filter(cc -> !this.isExecutableClass(cc))
            .findFirst()
            .orElse(null);
    }

    /**
     * Test if this class is an executable class.
     *
     * <p>
     * Executable class are executed upon loading
     * of the libraries/jars when Smallworld loads the module. The defined Magik methods on exemplars are stored
     * on non-executable classes.
     * </p>
     * @param classCoverage
     * @return
     */
    private boolean isExecutableClass(final IClassCoverage classCoverage) {
        final Collection<ClassNode> executableMagikClassNodes = this.libReader.getExecutableClassNodes();
        final String className = classCoverage.getName() + ".class";
        final ClassNode classNode = this.libReader.getClassByName(className);
        return executableMagikClassNodes.contains(classNode);
    }

}
