package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.CheckForNull;

import java.util.Arrays;
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
    private static final String METHOD_DEFINITION_OWNER = "com/gesmallworld/magik/language/utils/MagikObjectUtils";
    private static final String METHOD_DEFINITION_NAME = "createMethod";

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

    public Map<MethodNode, MethodNode> buildMethodDependencyMap(final ClassNode classNode) {
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
        final Map<String, String> methodNames = this.extractAllMethodNames();
        final String completeJavaName = this.keyForClassMethodName(javaClassName, javaMethodName);
        final String magikName = methodNames.get(completeJavaName);
        return magikName;
    }

    /**
     * Create a mapping from Java class/method names to Magik exemplar/method names.
     * @return Mapping from Java class/method to Magik exemplar/method names.
     */
    private Map<String, String> extractAllMethodNames() {
        if (this.methodNameMapping == null) {
            this.methodNameMapping = this.libReader.getExecutableClassNodes().stream()
                .map(this::getExecuteMethod)
                .map(this::extractMethodNames)
                .flatMap(mapping -> mapping.entrySet().stream())
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue));
        }

        return this.methodNameMapping;
    }

    /**
     * Convert Java class name/method name to used key.
     * @param javaClassName Java class name.
     * @param javaMethodName Java method name.
     * @return Key.
     */
    private String keyForClassMethodName(final String javaClassName, final String javaMethodName) {
        return Sw5LibMethodNameExtractor.keyForClassMethodName(javaClassName, javaMethodName);
    }

    private MethodNode getExecuteMethod(final ClassNode classNode) {
        return classNode.methods.stream()
            .filter(method -> method.visibleAnnotations.stream()
                .anyMatch(ann ->
                    ann.desc.equals(ANNOTATION_CODE_TYPE)
                    && ann.values.equals(ANNOTATION_VALUE_TOP_LEVEL)))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No execute() method found"));
    }

    private Map<String, String> extractMethodNames(final MethodNode executeMethod) {
        // Get all INVOKESTATIC instructions which define a method,
        // and extract methods from it.
        final InsnList instructions = executeMethod.instructions;
        return Arrays.stream(instructions.toArray())
            .filter(insn -> insn.getOpcode() == Opcodes.INVOKESTATIC)
            .map(MethodInsnNode.class::cast)
            .filter(methodInsn ->
                methodInsn.owner.equals(METHOD_DEFINITION_OWNER)
                && methodInsn.name.equals(METHOD_DEFINITION_NAME))
            .map(Sw5LibMethodNameExtractor::extractMethodName)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue));
    }

}
