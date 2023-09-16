package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import nl.ramsolutions.sw.magik.jacoco.helpers.ClassNodeHelper;
import nl.ramsolutions.sw.magik.jacoco.helpers.MethodNodeHelper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.CheckForNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Smallworld/Magik dependency builder.
 */
final class Sw5LibDependencyBuilder {

    private static final String PRELOAD_METHOD = "preload";
    private static final String EXECUTE_METHOD = "execute";

    private Sw5LibDependencyBuilder() {
    }

    public static Map<ClassNode, ClassNode> buildClassDependencyMap(
        final Collection<ClassNode> primaryClassNodes,
        final Collection<ClassNode> subsidiaryClassNodes) {
        return primaryClassNodes.stream()
            .map(primaryClassNode -> {
                final String subsidiaryClassName = Sw5LibDependencyBuilder.getSubsidiaryClassName(primaryClassNode);
                final ClassNode subsidiaryClassNode = subsidiaryClassNodes.stream()
                    .filter(classNode -> classNode.name.equals(subsidiaryClassName))
                    .findAny()
                    .orElse(null);
                if (subsidiaryClassNode == null) {
                    return null;
                }

                return Map.entry(primaryClassNode, subsidiaryClassNode);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue));
    }

    private static String getSubsidiaryClassName(final ClassNode primaryClassNode) {
        final MethodNode methodNode = ClassNodeHelper.getMethodNode(primaryClassNode, PRELOAD_METHOD);
        final InsnList instructions = methodNode.instructions;
        final LdcInsnNode ldcNode = Arrays.stream(instructions.toArray())
            .filter(insn -> insn.getOpcode() == Opcodes.LDC)
            .map(LdcInsnNode.class::cast)
            .findFirst()
            .orElse(null);
        if (ldcNode == null) {
            return null;
        }

        final Type cst = (Type) ldcNode.cst;
        final String className = cst.getClassName();
        return className.replace(".", "/");
    }

    /**
     * Build dependency map with key as child MethodNode and value as parent MethodNode.
     *
     * <p>Note that the defined methods are not added as a dependency of the {@code execute()} method.</p>
     * @param providerClassNode Provider ClassNode, can both be primary and  subsidiary.
     * @param supplierClassNode Supplier ClassNode, always the subsidiary if present.
     * @return Map with method dependencies.
     */
    public static Map<MethodNode, MethodNode> buildMethodDependencyMap(
            final ClassNode providerClassNode,
            @CheckForNull final ClassNode supplierClassNode) {
        if (supplierClassNode == null) {
            return Collections.emptyMap();
        }

        final Map<MethodNode, MethodNode> dependencyMap = new HashMap<>();

        // Find all relations from subsidiary --> subsidiary.
        final Map<MethodNode, MethodNode> subsidiaryDependencyMap = supplierClassNode.methods.stream()
            .filter(MethodNodeHelper::hasParentAnnotation)
            .collect(Collectors.toMap(
                methodNode -> methodNode,
                methodNode -> {
                    final String wantedMethodName = MethodNodeHelper.getParentMethodName(methodNode);
                    return ClassNodeHelper.getMethodNode(supplierClassNode, wantedMethodName);
                }));
        dependencyMap.putAll(subsidiaryDependencyMap);

        // All other entries not from subsidiary --> subsidiary, must be from execute() method.
        final MethodNode executeMethodNode = ClassNodeHelper.isPrimaryClassNode(providerClassNode)
            ? ClassNodeHelper.getMethodNodeSafe(providerClassNode, EXECUTE_METHOD)
            : null;
        if (executeMethodNode != null) {
            final String supplierClassNodeName = supplierClassNode.name.replace("/", ".");
            final Map<String, String> methodNamesMap = Sw5LibMethodNameExtractor.extractMethodNames(executeMethodNode);
            final Map<MethodNode, MethodNode> executableDependencyMap = supplierClassNode.methods.stream()
                .filter(methodNode -> !subsidiaryDependencyMap.containsKey(methodNode))
                .filter(methodNode -> {
                    // Is not a defined method at subsidiary.
                    final String key = supplierClassNodeName + "." + methodNode.name;
                    return !methodNamesMap.containsKey(key);
                })
                .collect(Collectors.toMap(
                    methodNode -> methodNode,
                    methodNode -> executeMethodNode));
            dependencyMap.putAll(executableDependencyMap);
        }

        return dependencyMap;
    }

}
