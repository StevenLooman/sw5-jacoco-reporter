package nl.ramsolutions.sw.magik.jacoco.conversion;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Utility class for {@link ClassNode}s.
 */
public final class ClassNodeUtils {

    private ClassNodeUtils() {
    }

    /**
     * Get {@link MethodNode} from {@link ClassNode} by name.
     * @param classNode {@link ClassNode} to get {@link MethodNode} from.
     * @param methodName Name of method.
     * @return {@link MethodNode} for name.
     */
    static MethodNode getMethodNodeFromClassNode(final ClassNode classNode, final String methodName) {
        return classNode.methods.stream()
                .filter(methodNode -> methodNode.name.equals(methodName))
                .findAny()
                .orElseThrow();
    }

}
