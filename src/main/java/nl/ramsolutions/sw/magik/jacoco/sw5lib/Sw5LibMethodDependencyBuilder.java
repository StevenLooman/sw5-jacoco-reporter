package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import java.util.Map;
import java.util.stream.Collectors;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Smallworld/Magik dependency builder.
 */
final class Sw5LibMethodDependencyBuilder {

    private static final String ANNOTATION_PARENT = "Lcom/gesmallworld/magik/commons/runtime/annotations/Parent;";
    private static final String ANNOTATION_PARENT_VALUE_METHOD = "method";

    private Sw5LibMethodDependencyBuilder() {
    }

    /**
     * Build dependency map with key as child MethodNode and parent as value MethodNode.
     * @param classNode Class node to extract methods from.
     * @return Map with child + parent relations.
     */
    public static Map<MethodNode, MethodNode> buildMethodDependencyMap(final ClassNode classNode) {
        return classNode.methods.stream()
            // Filter methods which have Parent annotation.
            .filter(methodNode -> methodNode.visibleAnnotations != null)
            .filter(methodNode -> methodNode.visibleAnnotations.stream()
                .anyMatch(ann -> ann.desc.equals(ANNOTATION_PARENT)))
            // Create mapping between child- and parent-method.
            .collect(Collectors.toMap(
                childMethodNode -> childMethodNode,
                childMethodNode -> {
                    final AnnotationNode annotationNode = childMethodNode.visibleAnnotations.stream()
                        .filter(ann ->
                            ann.desc.equals(ANNOTATION_PARENT)
                            && !ann.values.isEmpty()
                            && ann.values.get(0).equals(ANNOTATION_PARENT_VALUE_METHOD))
                        .findAny()
                        .orElseThrow();
                    final String wantedMethodName = (String) annotationNode.values.get(1);
                    return classNode.methods.stream()
                        .filter(methodNode -> methodNode.name.equals(wantedMethodName))
                        .findAny()
                        .orElseThrow();
                }));
    }

}
