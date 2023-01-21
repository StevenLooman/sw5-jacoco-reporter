package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import nl.ramsolutions.sw.magik.jacoco.helpers.MethodNodeHelper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.CheckForNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Lib analyzer.
 *
 * <p>
 * Extracts exemplar/method definitions from class nodes.
 * </p>
 */
public final class Sw5LibAnalyzer {

    private final Sw5LibReader libReader;
    private Map<String, String> methodNameMapping;

    /**
     * Constructor.
     *
     * @param libReader Library reader used for analysis.
     */
    public Sw5LibAnalyzer(final Sw5LibReader libReader) {
        this.libReader = libReader;
    }

    /**
     * Get a {@link ClassNode} by its name.
     * @param className {@link ClassNode} name.
     * @return {@link ClassNode}, if found.
     */
    @CheckForNull
    public ClassNode getClassByName(final String className) {
        return this.libReader.getClassByName(className);
    }

    public Map<MethodNode, MethodNode> getMethodDependencyMap(
            final ClassNode providerNode,
            final ClassNode supplierNode) {
        // Move this from Sw5LibMethodDependencyBuilder to here?
        return Sw5LibDependencyBuilder.buildMethodDependencyMap(providerNode, supplierNode);
    }

    /**
     * Get the primary/subsidiary class mapping.
     * @return Mapping keyed on primary class, valued on subsidiary class.
     */
    public Map<ClassNode, ClassNode> getClassDependencyMap() {
        final Collection<ClassNode> primaryClassNodes = this.libReader.getPrimaryClassNodes();
        final Collection<ClassNode> subsidiaryClassNodes = this.libReader.getSubsidiaryClassNodes();
        return Sw5LibDependencyBuilder.buildClassDependencyMap(primaryClassNodes, subsidiaryClassNodes);
    }

    /**
     * Get the Magik method from a Java class/method combination.
     * @param javaClassName Name of Java class.
     * @param javaMethodName Name of Java method.
     * @return Magik method name, if known.
     */
    @CheckForNull
    public String getMagikMethodName(final String javaClassName, final String javaMethodName) {
        final Map<String, String> methodNames = this.getMethodNameMapping();
        final String completeJavaName = Sw5LibAnalyzer.keyForClassMethodName(javaClassName, javaMethodName);
        final String magikName = methodNames.get(completeJavaName);
        if (magikName == null) {
            final String msg = "Could not find mapped method, key: " + completeJavaName;
            throw new IllegalStateException(msg);
        }

        return magikName;
    }

    /**
     * Create a mapping from Java class/method names to Magik exemplar/method names.
     * @return Mapping from Java class/method to Magik exemplar/method names.
     */
    private Map<String, String> getMethodNameMapping() {
        if (this.methodNameMapping == null) {
            final Map<String, String> methodMapping = this.libReader.getPrimaryClassNodes().stream()
                .map(MethodNodeHelper::getExecuteMethod)
                .map(Sw5LibMethodNameExtractor::extractMethodNames)
                .flatMap(mapping -> mapping.entrySet().stream())
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue));

            final Map<String, String> procMappingExec = this.libReader.getPrimaryClassNodes().stream()
                .map(MethodNodeHelper::getExecuteMethod)
                .map(Sw5LibProcNameExtractor::extractProcNames)
                .flatMap(mapping -> mapping.entrySet().stream())
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue));
            final Map<String, String> procMappingSub = this.libReader.getSubsidiaryClassNodes().stream()
                .flatMap(classNode -> classNode.methods.stream())
                .map(Sw5LibProcNameExtractor::extractProcNames)
                .flatMap(mapping -> mapping.entrySet().stream())
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue));

            this.methodNameMapping = new HashMap<>();
            this.methodNameMapping.putAll(methodMapping);
            this.methodNameMapping.putAll(procMappingExec);
            this.methodNameMapping.putAll(procMappingSub);
        }

        return this.methodNameMapping;
    }

    static String keyForClassMethodName(final String javaClassName, final String javaMethodName) {
        return javaClassName.replace("/", ".") + "." + javaMethodName;
    }

}
