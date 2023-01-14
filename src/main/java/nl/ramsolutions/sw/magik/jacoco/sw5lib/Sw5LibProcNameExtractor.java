package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

import java.util.Map;

/**
 * Utility class to extract proc name from INVOKEDYNAMIC proc calls.
 */
final class Sw5LibProcNameExtractor {

    private static final String ANONYMOUS_PROC = "__anonymous_proc__";

    private Sw5LibProcNameExtractor() {
    }

    static String keyForClassMethodName(final String javaClassName, final String javaMethodName) {
        return javaClassName.replace("/", ".") + "." + javaMethodName;
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

        final String key = Sw5LibProcNameExtractor.keyForClassMethodName(javaTypeName, javaMethodName);
        final String magikProcName = Sw5LibProcNameExtractor.magikProcName(procName);
        return Map.entry(key, magikProcName);
    }

}
