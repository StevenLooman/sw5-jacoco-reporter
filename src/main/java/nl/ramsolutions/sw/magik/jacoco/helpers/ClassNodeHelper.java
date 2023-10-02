package nl.ramsolutions.sw.magik.jacoco.helpers;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.CheckForNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Utility class for {@link ClassNode}s.
 */
public final class ClassNodeHelper {

    private static final String ANNOTATION_CODE_TYPE = "Lcom/gesmallworld/magik/commons/runtime/annotations/CodeType;";
    private static final String ANNOTATION_CODE_TYPE_PRIMARY = "Primary";
    private static final String ANNOTATION_CODE_TYPE_SUBSIDIARY = "Subsidiary";

    private ClassNodeHelper() {
    }

    /**
     * Get {@link MethodNode} from {@link ClassNode} by name.
     * @param classNode {@link ClassNode} to get {@link MethodNode} from.
     * @param methodName Name of method.
     * @return {@link MethodNode} for name.
     */
    public static MethodNode getMethodNode(final ClassNode classNode, final String methodName) {
        return classNode.methods.stream()
            .filter(methodNode -> methodNode.name.equals(methodName))
            .findAny()
            .orElseThrow();
    }

    /**
     * Get {@link MethodNode} from {@link ClassNode} by name, safe.
     * @param classNode {@link ClassNode} to get {@link MethodNode} from.
     * @param methodName Name of method.
     * @return {@link MethodNode} for name.
     */
    @CheckForNull
    public static MethodNode getMethodNodeSafe(final ClassNode classNode, final String methodName) {
        return classNode.methods.stream()
            .filter(methodNode -> methodNode.name.equals(methodName))
            .findAny()
            .orElse(null);
    }

    /**
     * Test if this is the primary class node.
     * @param classNode
     * @return
     */
    public static boolean isPrimaryClassNode(final ClassNode classNode) {
        final List<AnnotationNode> visibleAnnotations =
            Objects.requireNonNullElse(classNode.visibleAnnotations, Collections.emptyList());
        return visibleAnnotations.stream()
            .anyMatch(annotation ->
                annotation.desc.equals(ANNOTATION_CODE_TYPE)
                && annotation.values.get(1).equals(ANNOTATION_CODE_TYPE_PRIMARY));
    }

    /**
     * Test if this is the subsidiary class node.
     * @param classNode
     * @return
     */
    public static boolean isSubsidiaryClassNode(final ClassNode classNode) {
        final List<AnnotationNode> visibleAnnotations =
            Objects.requireNonNullElse(classNode.visibleAnnotations, Collections.emptyList());
        return visibleAnnotations.stream()
            .anyMatch(annotation ->
                annotation.desc.equals(ANNOTATION_CODE_TYPE)
                && annotation.values.get(1).equals(ANNOTATION_CODE_TYPE_SUBSIDIARY));
    }

}
