package nl.ramsolutions.sw.magik.jacoco.helpers;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public final class MethodNodeHelper {

    private static final String ANNOTATION_CODE_TYPE = "Lcom/gesmallworld/magik/commons/runtime/annotations/CodeType;";
    private static final List<String> ANNOTATION_VALUE_TOP_LEVEL = List.of("value", "TopLevel");
    private static final String ANNOTATION_PARENT = "Lcom/gesmallworld/magik/commons/runtime/annotations/Parent;";
    private static final String ANNOTATION_PARENT_VALUE_METHOD = "method";

    private MethodNodeHelper() {
    }

    public static MethodNode getExecuteMethod(final ClassNode classNode) {
        return classNode.methods.stream()
            .filter(MethodNodeHelper::isExecuteMethod)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No execute() method found"));
    }

    public static boolean isExecuteMethod(final MethodNode methodNode) {
        final List<AnnotationNode> annotationNodes = methodNode.visibleAnnotations;
        if (annotationNodes == null) {
            return false;
        }

        return annotationNodes.stream()
            .anyMatch(ann ->
                ann.desc.equals(ANNOTATION_CODE_TYPE)
                && ann.values.equals(ANNOTATION_VALUE_TOP_LEVEL));
    }

    public static boolean hasParentAnnotation(final MethodNode methodNode) {
        final List<AnnotationNode> annotationNodes = methodNode.visibleAnnotations;
        if (annotationNodes == null) {
            return false;
        }

        return methodNode.visibleAnnotations.stream()
            .anyMatch(ann -> ann.desc.equals(ANNOTATION_PARENT));
    }

    public static String getParentMethodName(final MethodNode methodNode) {
        final AnnotationNode parentMethodAnnotationNode = methodNode.visibleAnnotations.stream()
            .filter(ann ->
                ann.desc.equals(ANNOTATION_PARENT)
                && !ann.values.isEmpty()
                && ann.values.get(0).equals(ANNOTATION_PARENT_VALUE_METHOD))
            .findAny()
            .orElseThrow();
        return (String) parentMethodAnnotationNode.values.get(1);
    }

}
