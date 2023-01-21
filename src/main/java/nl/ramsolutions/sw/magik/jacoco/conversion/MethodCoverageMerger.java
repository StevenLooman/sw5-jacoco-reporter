package nl.ramsolutions.sw.magik.jacoco.conversion;

import nl.ramsolutions.sw.magik.jacoco.helpers.ClassNodeHelper;
import nl.ramsolutions.sw.magik.jacoco.sw5lib.Sw5LibAnalyzer;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.internal.analysis.MethodCoverageImpl;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Merges {@link IMethodCoverage}s, such as {@code __loopbody__}, in {@link IClassCoverage}.
 *
 * <p>
 * Given a {@link IClassCoverage}, iterates over the methods and sees if this is a "sub-method",
 * such as a {@code __loopbody__} or a {@code __proc__} method, and merges the methods.
 * </p>
 */
public class MethodCoverageMerger {

    private final Sw5LibAnalyzer libAnalyzer;

    public MethodCoverageMerger(final Sw5LibAnalyzer libAnalyzer) {
        this.libAnalyzer = libAnalyzer;
    }

    /**
     * Run the merge, return the new {@link IMethodCoverage}s.
     * @param primaryClassCoverage Class coverage.
     */
    public Collection<IMethodCoverage> run(
            final IClassCoverage primaryClassCoverage,
            @Nullable final IClassCoverage subsidiaryClassCoverage) {
        // Find ClassNode for IClassCoverage.
        final ClassNode providerClassNode = this.getClassNode(primaryClassCoverage);
        if (providerClassNode == null) {
            final String msg = "Could not find provider node, class: " + primaryClassCoverage.getName();
            throw new IllegalStateException(msg);
        }

        final ClassNode supplierClassNode = subsidiaryClassCoverage != null
            ? this.getClassNode(subsidiaryClassCoverage)
            : null;
        if (supplierClassNode == null) {
            // ClassCoverage is most likely for a primary class without a subsidiary class.
            // No need to merge anything.
            return primaryClassCoverage.getMethods();
        }

        // Merge IMethodCoverage for primary/subsidiary and all dependencies.
        final Map<MethodNode, MethodNode> methodDependencyMap =
            this.libAnalyzer.getMethodDependencyMap(providerClassNode, supplierClassNode);
        final Map<MethodNode, IMethodCoverage> methodCoverageMap = this.buildMethodCoverageMap(
            primaryClassCoverage, providerClassNode,
            subsidiaryClassCoverage, supplierClassNode);
        final Collection<IMethodCoverage> mergedMethods =
            this.mergeMethods(providerClassNode, supplierClassNode, methodDependencyMap, methodCoverageMap);

        return mergedMethods;
    }

    private Collection<IMethodCoverage> mergeMethods(
            final ClassNode primaryClassNode,
            final ClassNode subsidiaryClassNode,
            final Map<MethodNode, MethodNode> methodDependencyMap,
            final Map<MethodNode, IMethodCoverage> methodCoverageMap) {
        // Note that the methodCoverageMap is mutated in place!

        // Determine order to merge.
        final List<MethodNode> mergeOrder = new ArrayList<>();
        mergeOrder.addAll(primaryClassNode.methods);
        mergeOrder.addAll(subsidiaryClassNode.methods);
        Collections.reverse(mergeOrder);

        // Merge all methods.
        for (final MethodNode childNode : mergeOrder) {
            final MethodNode parentNode = methodDependencyMap.get(childNode);
            if (parentNode == null) {
                // Nothing to merge, carry on...
                // MethodCoverage is already in methodCoverageMap, so nothing needs to be done.
                continue;
            }

            // Merge parent + child.
            final IMethodCoverage parentMethodCoverage = methodCoverageMap.get(parentNode);
            final IMethodCoverage childMethodCoverage = methodCoverageMap.get(childNode);
            Objects.requireNonNull(parentMethodCoverage);
            Objects.requireNonNull(childMethodCoverage);
            final IMethodCoverage newMethodCoverage =
                this.mergeMethodCoverage(parentMethodCoverage, childMethodCoverage);

            // Store new MethodCoverage at parent (overwrite), remove child.
            methodCoverageMap.put(parentNode, newMethodCoverage);
            methodCoverageMap.remove(childNode);
        }

        return methodCoverageMap.values();
    }

    @CheckForNull
    private ClassNode getClassNode(final IClassCoverage classCoverage) {
        final String className = classCoverage.getName() + ".class";
        return this.libAnalyzer.getClassByName(className);
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
            final IClassCoverage primaryClassCoverage,
            final ClassNode providerClassNode,
            final IClassCoverage subsidiaryClassCoverage,
            final ClassNode supplierClassNode) {
        return Stream.concat(
            primaryClassCoverage.getMethods().stream()
                .map(methodCoverage -> {
                    final String methodName = methodCoverage.getName();
                    final MethodNode methodNode = ClassNodeHelper.getMethodNode(providerClassNode, methodName);
                    return Map.entry(methodNode, methodCoverage);
                }),
                subsidiaryClassCoverage.getMethods().stream()
                .map(methodCoverage -> {
                    final String methodName = methodCoverage.getName();
                    final MethodNode methodNode = ClassNodeHelper.getMethodNode(supplierClassNode, methodName);
                    return Map.entry(methodNode, methodCoverage);
                }))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue));
    }

}
