package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class to extract defined Magik method names via INVOKESTATIC/createMethod() calls.
 */
final class Sw5LibMethodNameExtractor {

    private static final int LDC_EXPECTED_SIZE = 4;
    private static final int LDC_INDEX_MAGIK_EXEMPLAR = 0;
    private static final int LDC_INDEX_MAGIK_METHOD = 1;
    private static final int LDC_INDEX_JAVA_TYPE = 2;
    private static final int LDC_INDEX_JAVA_METHOD_NAME = 3;
    private static final String METHOD_DEFINITION_OWNER = "com/gesmallworld/magik/language/utils/MagikObjectUtils";
    private static final String METHOD_DEFINITION_NAME = "createMethod";

    private Sw5LibMethodNameExtractor() {
    }

    private static String fullMagikMethodName(final String exemplarName, final String methodName) {
        if (methodName.startsWith("[")) {
            return String.format("%s%s", exemplarName, methodName);
        }

        return String.format("%s.%s", exemplarName, methodName);
    }

    /**
     * Extract the exemplar/method name from a static MagikObjectUtils.createMethod() call.
     * @param methodInsnNode {@link MethodInsnNode} to extract from.
     * @return Java name / Magik name entry.
     */
    private static Map.Entry<String, String> extractMethodName(final MethodInsnNode methodInsnNode) {
        final List<LdcInsnNode> ldcNodes = Sw5LibMethodNameExtractor.getLdcNodes(methodInsnNode);
        if (ldcNodes.size() != LDC_EXPECTED_SIZE) {
            throw new IllegalStateException();
        }

        // Extract createMethod arguments.
        final String magikExemplar = (String) ldcNodes.get(LDC_INDEX_MAGIK_EXEMPLAR).cst;
        final String magikMethod = (String) ldcNodes.get(LDC_INDEX_MAGIK_METHOD).cst;
        final Type javaType = (Type) ldcNodes.get(LDC_INDEX_JAVA_TYPE).cst;
        final String javaMethodName = (String) ldcNodes.get(LDC_INDEX_JAVA_METHOD_NAME).cst;

        // Build key + full magik method name.
        final String javaTypeName = javaType.getClassName();
        final String key = Sw5LibAnalyzer.keyForClassMethodName(javaTypeName, javaMethodName);
        final String fullMagikMethod = Sw5LibMethodNameExtractor.fullMagikMethodName(magikExemplar, magikMethod);
        return Map.entry(key, fullMagikMethod);
    }

    /**
     * Get {@link LdcInsnNode}s before {@link MethodInsnNode}.
     *
     * <p>
     * Skips over any instructions before {@link LdcInsnNode}s.
     * </p>
     *
     * @param methodInsn {@link MethodInsnNode} to extract from.
     * @return Collection with {@link LdcInsnNode}s.
     */
    private static List<LdcInsnNode> getLdcNodes(final MethodInsnNode methodInsn) {
        AbstractInsnNode current = methodInsn.getPrevious();

        // Skip over ICONST instructions.
        while (current != null && current.getOpcode() != Opcodes.LDC) {
            current = current.getPrevious();
        }

        // Get all LDC instructions.
        final List<LdcInsnNode> ldcNodes = new ArrayList<>();
        while (current != null && current.getOpcode() == Opcodes.LDC) {
            final LdcInsnNode ldcNode = (LdcInsnNode) current;
            ldcNodes.add(ldcNode);

            current = current.getPrevious();
        }
        Collections.reverse(ldcNodes);
        return ldcNodes;
    }

    /**
     * Extract Magik method names.
     * @param executeMethodNode Execute method from primary class.
     * @return Map keyed on Java names, and the corresponding Magik names.
     */
    static Map<String, String> extractMethodNames(final MethodNode executeMethodNode) {
        // Get all static MagikObjectUtils.createMethod() calls which define a method,
        // and extract method names from those.
        final InsnList instructions = executeMethodNode.instructions;
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
