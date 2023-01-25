package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Utility class to extract proc name from INVOKEDYNAMIC proc calls.
 */
final class Sw5LibProcNameExtractor {

    private static final String PROC_DEFINITION_OWNER = "com/gesmallworld/magik/language/invokers/ConstantBuilder";
    private static final String PROC_DEFINITION_NAME = "proc";
    private static final String ANONYMOUS_PROC = "__anonymous_proc__";

    private Sw5LibProcNameExtractor() {
    }

    static String magikProcName(final String procName) {
        final String fixedName = procName.isBlank()
            ? ANONYMOUS_PROC
            : procName;
        return "@" + fixedName;
    }

    /**
     * Extract the exemplar/method name from a INVOKEDYNAMIC call.
     * @param invokeDynamicInsnNode {@link InvokeDynamicInsnNode} to extract from.
     * @return Java name / Magik name entry.
     */
    static Map.Entry<String, String> extractProcName(final InvokeDynamicInsnNode invokeDynamicInsnNode) {
        final Object[] bsmArgs = invokeDynamicInsnNode.bsmArgs;
        final Type javaType = (Type) bsmArgs[0];
        final String javaTypeName = javaType.getClassName();
        final String javaMethodName = (String) bsmArgs[1];
        final String procName = (String) bsmArgs[2];

        final String key = Sw5LibAnalyzer.keyForClassMethodName(javaTypeName, javaMethodName);
        final String magikProcName = Sw5LibProcNameExtractor.magikProcName(procName);
        return Map.entry(key, magikProcName);
    }

    /**
     * Extract Magik proc names.
     * @param methodNode MethodNode which might create procedures.
     * @return Map keyed on Java names, and the corresponding Magik names.
     */
    static Map<String, String> extractProcNames(final MethodNode methodNode) {
        final InsnList instructions = methodNode.instructions;
        return Arrays.stream(instructions.toArray())
            .filter(insn -> insn.getOpcode() == Opcodes.INVOKEDYNAMIC)
            .map(InvokeDynamicInsnNode.class::cast)
            .filter(invokeDynamicInsnNode ->
                invokeDynamicInsnNode.bsm.getOwner().equals(PROC_DEFINITION_OWNER)
                && invokeDynamicInsnNode.name.equals(PROC_DEFINITION_NAME)
            )
            .map(Sw5LibProcNameExtractor::extractProcName)
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue));
    }

}
