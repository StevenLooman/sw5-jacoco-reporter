package nl.ramsolutions.sw.magik.jacoco.conversion;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import nl.ramsolutions.sw.magik.jacoco.sw5lib.Sw5LibReader;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.internal.analysis.BundleCoverageImpl;
import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.jacoco.core.internal.analysis.PackageCoverageImpl;
import org.objectweb.asm.tree.ClassNode;

/**
 * Convert native JaCoCo/JVM coverage data to Magik coverage data.
 *
 * <p>
 * Conversion includes:
 * - Methods are merged (__loopbody_ into parent method, etc)
 * - Methods are renamed to Magik-names, where applicable
 * - Primary classes are filtered, when enabled
 * </p>
 */
public class MagikBundleCoverageConverter {

    private final Sw5LibReader libReader;
    private final MethodCoverageMerger methodCoverageMerger;

    /**
     * Constructor.
     * @param libReader Lib reader.
     */
    public MagikBundleCoverageConverter(final Sw5LibReader libReader) {
        this.libReader = libReader;
        this.methodCoverageMerger = new MethodCoverageMerger(this.libReader);
    }

    /**
     * Run the conversion of the {@link IBundleCoverage}.
     * @param bundleCoverage Original {@link IBundleCoverage}.
     * @param filterPrimaryClasses Switch to filter primary classes.
     * @return Converted {@link IBundleCoverage}.
     */
    public IBundleCoverage convert(final IBundleCoverage bundleCoverage, final boolean filterPrimaryClasses) {
        final List<IPackageCoverage> packages = bundleCoverage.getPackages().stream()
            .map(packageCoverage -> this.convert(packageCoverage, filterPrimaryClasses))
            .collect(Collectors.toList());

        final String name = bundleCoverage.getName();
        return new BundleCoverageImpl(name, packages);
    }

    private IPackageCoverage convert(final IPackageCoverage packageCoverage, final boolean filterPrimaryClasses) {
        final String name = packageCoverage.getName();
        final Collection<IClassCoverage> classes = packageCoverage.getClasses().stream()
                .map(this::convert)
                .collect(Collectors.toList());
        final Collection<IClassCoverage> filteredClasses = filterPrimaryClasses
                ? this.filterPrimaryClassCoverages(classes)
                : classes;
        final Collection<ISourceFileCoverage> sourceFiles = packageCoverage.getSourceFiles().stream()
                .map(this::convert)
                .collect(Collectors.toList());
        return new PackageCoverageImpl(name, filteredClasses, sourceFiles);
    }

    private IClassCoverage convert(final IClassCoverage classCoverage) {
        // Create new class coverage.
        final String name = classCoverage.getName();
        final long id = classCoverage.getId();
        final boolean noMatch = classCoverage.isNoMatch();
        final ClassCoverageImpl newClassCoverage = new ClassCoverageImpl(name, id, noMatch);

        // Merge "sub-methods" into methods.
        final Collection<IMethodCoverage> methodCoverages = this.methodCoverageMerger.run(classCoverage);
        methodCoverages.forEach(newClassCoverage::addMethod);

        final String[] interfaceNames = classCoverage.getInterfaceNames();
        newClassCoverage.setInterfaces(interfaceNames);

        // Store source file name.
        final String sourceFileName = classCoverage.getSourceFileName();
        newClassCoverage.setSourceFileName(sourceFileName);

        return newClassCoverage;
    }

    private ISourceFileCoverage convert(final ISourceFileCoverage sourceFileCoverage) {
        return sourceFileCoverage;
    }

    private Collection<IClassCoverage> filterPrimaryClassCoverages(final Collection<IClassCoverage> classes) {
        Collection<ClassNode> executableMagikClassNodes = this.libReader.getExecutableClassNodes();
        return classes.stream()
                .filter(classCoverage -> {
                    final ClassNode classNode =
                        this.libReader.getClassByName(classCoverage.getName() + ".class");
                    return !executableMagikClassNodes.contains(classNode);
                })
                .collect(Collectors.toSet());
    }

}
