package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.CheckForNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class analyzer.
 *
 * <p>
 * Extracts exemplar/method definitions from a class node.
 * </p>
 */
public final class Sw5LibAnalyzer {

    private static final String ANNOTATION_CODE_TYPE = "Lcom/gesmallworld/magik/commons/runtime/annotations/CodeType;";
    private static final List<String> ANNOTATION_VALUE_TOP_LEVEL = List.of("value", "TopLevel");

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

    public Map<MethodNode, MethodNode> getMethodDependencyMap(final ClassNode classNode) {
        return Sw5LibMethodDependencyBuilder.buildMethodDependencyMap(classNode);
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
        final String completeJavaName = Sw5LibMethodNameExtractor.keyForClassMethodName(javaClassName, javaMethodName);
        final String magikName = methodNames.get(completeJavaName);
        return magikName;
    }

    /**
     * Create a mapping from Java class/method names to Magik exemplar/method names.
     * @return Mapping from Java class/method to Magik exemplar/method names.
     */
    private Map<String, String> getMethodNameMapping() {
        if (this.methodNameMapping == null) {
            final Map<String, String> methodMapping = this.libReader.getExecutableClassNodes().stream()
                .map(this::getExecuteMethod)
                .map(Sw5LibMethodNameExtractor::extractMethodNames)
                .flatMap(mapping -> mapping.entrySet().stream())
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue));

            final Map<String, String> procMapping = this.libReader.getExecutableClassNodes().stream()
                .map(this::getExecuteMethod)
                .map(Sw5LibProcNameExtractor::extractProcNames)
                .flatMap(mapping -> mapping.entrySet().stream())
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue));

            this.methodNameMapping = new HashMap<>(methodMapping.size() + procMapping.size());
            this.methodNameMapping.putAll(methodMapping);
            this.methodNameMapping.putAll(procMapping);
        }

        return this.methodNameMapping;
    }

    private MethodNode getExecuteMethod(final ClassNode classNode) {
        return classNode.methods.stream()
            .filter(this::isExecuteMethod)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No execute() method found"));
    }

    private boolean isExecuteMethod(final MethodNode methodNode) {
        return methodNode.visibleAnnotations.stream()
            .anyMatch(ann ->
                ann.desc.equals(ANNOTATION_CODE_TYPE)
                && ann.values.equals(ANNOTATION_VALUE_TOP_LEVEL));
    }

}
