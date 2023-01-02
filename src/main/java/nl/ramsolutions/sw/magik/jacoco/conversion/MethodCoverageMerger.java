package nl.ramsolutions.sw.magik.jacoco.conversion;

import nl.ramsolutions.sw.magik.jacoco.sw5lib.Sw5LibAnalyzer;
import nl.ramsolutions.sw.magik.jacoco.sw5lib.Sw5LibReader;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.internal.analysis.MethodCoverageImpl;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.CheckForNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Merges {@code IMethodCoverage}s, such as {{__loopbody__}}, in {@code IClassCoverage}.
 *
 * <p>
 * Given a {@code IClassCoverage}, iterates over the methods and sees if this is a "sub-method",
 * such as a __loopbody__ or a __proc__ method, and merges the methods.
 * </p>
 */
public class MethodCoverageMerger {

    private final Sw5LibReader libReader;

    public MethodCoverageMerger(final Sw5LibReader libReader) {
        this.libReader = libReader;
    }

    /**
     * Run the merge, return the new {@code IMethodCoverage}s.
     */
    public Collection<IMethodCoverage> run(final IClassCoverage classCoverage) {
        // Find relevant class for classCoverage.
        final ClassNode classNode = this.getClassNode(classCoverage);
        if (classNode == null) {
            throw new IllegalStateException();
        }

        // Gather needed data.
        final Sw5LibAnalyzer libAnalyzer = new Sw5LibAnalyzer(this.libReader);
        final Map<MethodNode, MethodNode> methodDependencyMap =
            libAnalyzer.buildMethodDependencyMap(classNode);
        final Map<MethodNode, IMethodCoverage> methodCoverageMap =
            this.buildMethodCoverageMap(classCoverage, classNode);

        // Merge {{IMethodCoverage}}s for all dependencies.
        return this.mergeMethods(classNode, methodDependencyMap, methodCoverageMap);
    }

    private Collection<IMethodCoverage> mergeMethods(
            final ClassNode classNode,
            final Map<MethodNode, MethodNode> methodDependencyMap,
            final Map<MethodNode, IMethodCoverage> methodCoverageMap) {
        final List<MethodNode> mergeOrder = new ArrayList<>(classNode.methods);
        Collections.reverse(mergeOrder);
        for (final MethodNode childNode : mergeOrder) {
            final MethodNode parentNode = methodDependencyMap.get(childNode);
            if (parentNode == null) {
                // Nothing to merge, carry on...
                continue;
            }

            // Merge parent + child.
            final IMethodCoverage parentMethodCoverage = methodCoverageMap.get(parentNode);
            final IMethodCoverage childMethodCoverage = methodCoverageMap.get(childNode);
            Objects.requireNonNull(parentMethodCoverage);
            Objects.requireNonNull(childMethodCoverage);
            final IMethodCoverage newMethodCoverage =
                this.mergeMethodCoverage(parentMethodCoverage, childMethodCoverage);

            // Store new MethodCoverage at parent (overwrite),
            // remove child.
            methodCoverageMap.put(parentNode, newMethodCoverage);
            methodCoverageMap.remove(childNode);
        }

        return methodCoverageMap.values();
    }

    @CheckForNull
    private ClassNode getClassNode(final IClassCoverage classCoverage) {
        final String className = classCoverage.getName() + ".class";
        return this.libReader.getClassByName(className);
    }

    private IMethodCoverage mergeMethodCoverage(
            final IMethodCoverage parentMethodCoverage,
            final IMethodCoverage childMethodCoverage) {
        final String name = parentMethodCoverage.getName();
        final String desc = parentMethodCoverage.getDesc();
        final String signature = parentMethodCoverage.getSignature();
        final MethodCoverageImpl newMethodCoverage = new MethodCoverageImpl(name, desc, signature);

        newMethodCoverage.increment(parentMethodCoverage);
        newMethodCoverage.increment(childMethodCoverage);
        return newMethodCoverage;
    }

    /**
     * Build map with key as {@link MethodNode} and value as {@link IMethodCoverage}.
     * @param classCoverage Class coverage
     * @param classNode Class node to extract methods form.
     * @return Map with {@link MethodNode} mapped to {@link IMethodCoverage}.
     */
    private Map<MethodNode, IMethodCoverage> buildMethodCoverageMap(
            final IClassCoverage classCoverage,
            final ClassNode classNode) {
        return classCoverage.getMethods().stream()
            .collect(Collectors.toMap(
                methodCoverage -> {
                    final String methodName = methodCoverage.getName();
                    return ClassNodeUtils.getMethodNodeFromClassNode(classNode, methodName);
                },
                Objects::requireNonNull
            ));
    }

}
