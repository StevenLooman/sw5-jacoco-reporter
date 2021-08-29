package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * Utility class to extract method name from INVOKESTATIC/createMethod() calls.
 */
final class Sw5LibMethodNameExtractor {

    private static final int LDC_EXPECTED_SIZE = 4;
    private static final int LDC_INDEX_MAGIK_EXEMPLAR = 0;
    private static final int LDC_INDEX_MAGIK_METHOD = 1;
    private static final int LDC_INDEX_JAVA_TYPE = 2;
    private static final int LDC_INDEX_JAVA_METHOD_NAME = 3;

    private Sw5LibMethodNameExtractor() {
    }

    static String keyForClassMethodName(String javaClassName, String javaMethodName) {
        return javaClassName.replace("/", ".") + "." + javaMethodName;
    }

    static String fullMagikMethodName(String exemplarName, String methodName) {
        return exemplarName + "." + methodName;
    }

    /**
     * Extract the exemplar/method name from a INVOKESTATIC/createMethod() call.
     * @param methodInsnNode {@link MethodInsnNode} to extract from.
     * @return Java name / Magik name entry.
     */
    static Map.Entry<String, String> extractMethodName(MethodInsnNode methodInsnNode) {
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
        final String key = Sw5LibMethodNameExtractor.keyForClassMethodName(javaTypeName, javaMethodName);
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
    private static List<LdcInsnNode> getLdcNodes(MethodInsnNode methodInsn) {
        AbstractInsnNode current = methodInsn.getPrevious();

        // Skip over ICONST instructions.
        while (current != null && current.getOpcode() != Opcodes.LDC) {
            current = current.getPrevious();
        }

        // Get all LDC instructions.
        List<LdcInsnNode> ldcNodes = new ArrayList<>();
        while (current != null && current.getOpcode() == Opcodes.LDC) {
            final LdcInsnNode ldcNode = (LdcInsnNode) current;
            ldcNodes.add(ldcNode);

            current = current.getPrevious();
        }
        Collections.reverse(ldcNodes);
        return ldcNodes;
    }

}
