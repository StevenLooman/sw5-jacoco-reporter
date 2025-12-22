package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import nl.ramsolutions.sw.magik.jacoco.helpers.MethodNodeHelper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/** Utility class to extract data from INVOKESTATIC/createMethod() calls. */
final class Sw5LibMethodDefinitionExtractor {

  private static final int LDC_EXPECTED_SIZE = 4;
  private static final int LDC_INDEX_MAGIK_EXEMPLAR = 0;
  private static final int LDC_INDEX_MAGIK_METHOD = 1;
  private static final int LDC_INDEX_JAVA_TYPE = 2;
  private static final int LDC_INDEX_JAVA_METHOD_NAME = 3;
  private static final String METHOD_DEFINITION_OWNER =
      "com/gesmallworld/magik/language/utils/MagikObjectUtils";
  private static final String METHOD_DEFINITION_NAME = "createMethod";
  private static final int CREATE_METHOD_FLAG_ITER = 2;
  private static final int CREATE_METHOD_FLAG_ABSTRACT = 3;
  private static final int CREATE_METHOD_FLAG_PRIVATE = 4;

  private Sw5LibMethodDefinitionExtractor() {}

  /**
   * Extract {@link MethodDefinition}s.
   *
   * @param classNode Primary class node.
   * @return Collection with all {@link MethodDefinition} instances.
   */
  static Collection<MethodDefinition> extractMethodDefinitions(final ClassNode classNode) {
    // Get all static `MagikObjectUtils.createMethod()` calls which define a method.
    final MethodNode executeMethodNode = MethodNodeHelper.getExecuteMethod(classNode);
    return Arrays.stream(executeMethodNode.instructions.toArray())
        .filter(insn -> insn.getOpcode() == Opcodes.INVOKESTATIC)
        .map(MethodInsnNode.class::cast)
        .filter(Sw5LibMethodDefinitionExtractor::isCreateMethodCall)
        .map(
            methodInsnNode ->
                Sw5LibMethodDefinitionExtractor.extractMethodDefinition(classNode, methodInsnNode))
        .toList();
  }

  /**
   * Check if a {@link MethodInsnNode} is a static `MagikObjectUtils.createMethod()` call.
   *
   * @param methodInsn {@link MethodInsnNode} to check.
   * @return true if it is a `MagikObjectUtils.createMethod()` call.
   */
  private static boolean isCreateMethodCall(final MethodInsnNode methodInsn) {
    return methodInsn.owner.equals(METHOD_DEFINITION_OWNER)
        && methodInsn.name.equals(METHOD_DEFINITION_NAME);
  }

  /**
   * Extract the exemplar/method name from a static `MagikObjectUtils.createMethod()` call.
   *
   * @param classNode Primary class node.
   * @param methodInsnNode {@link MethodInsnNode} to extract from.
   * @return {@link MethodDefinition}.
   */
  private static MethodDefinition extractMethodDefinition(
      final ClassNode classNode, final MethodInsnNode methodInsnNode) {
    final List<LdcInsnNode> ldcNodes = Sw5LibMethodDefinitionExtractor.getLdcNodes(methodInsnNode);
    if (ldcNodes.size() != LDC_EXPECTED_SIZE) {
      throw new IllegalStateException();
    }

    final Path path = Path.of(classNode.sourceFile);

    // Extract `MagikObjectUtils.createMethod()` arguments.
    final Type javaType = (Type) ldcNodes.get(LDC_INDEX_JAVA_TYPE).cst;
    final String javaTypeName = javaType.getClassName();
    final String javaMethodName = (String) ldcNodes.get(LDC_INDEX_JAVA_METHOD_NAME).cst;
    final String magikExemplarName = (String) ldcNodes.get(LDC_INDEX_MAGIK_EXEMPLAR).cst;
    final String magikMethodName = (String) ldcNodes.get(LDC_INDEX_MAGIK_METHOD).cst;

    // com/gesmallworld/magik/language/utils/MagikObjectUtils.createMethod(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/String;II)Ljava/lang/Object;
    // No flags.
    // com/gesmallworld/magik/language/utils/MagikObjectUtils.createMethod(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/String;IIZZZI)Ljava/lang/Object;
    // IIZZZI: 3 3 1 0 0 0  iterator
    //         1 1 0 1 0 0  abstract
    //         1 1 0 0 1 0  private
    //         3 3 1 1 1 0  iterator/abstract/private
    final List<Integer> flagValues =
        Sw5LibMethodDefinitionExtractor.getIconstValues(methodInsnNode);
    final EnumSet<MethodDefinition.Flag> flags = EnumSet.noneOf(MethodDefinition.Flag.class);
    if (flagValues.size() == 6) {
      if (flagValues.get(CREATE_METHOD_FLAG_ITER) == 1) {
        flags.add(MethodDefinition.Flag.ITERATOR);
      }
      if (flagValues.get(CREATE_METHOD_FLAG_ABSTRACT) == 1) {
        flags.add(MethodDefinition.Flag.ABSTRACT);
      }
      if (flagValues.get(CREATE_METHOD_FLAG_PRIVATE) == 1) {
        flags.add(MethodDefinition.Flag.PRIVATE);
      }
    }

    return new MethodDefinition(
        path, javaTypeName, javaMethodName, magikExemplarName, magikMethodName, flags);
  }

  /**
   * Get {@link LdcInsnNode}s before {@link MethodInsnNode}.
   *
   * <p>Skips over any instructions before {@link LdcInsnNode}s.
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
   * Get {@link InsnNode}s ICONST_0 .. ICONST_5 values before {@link MethodInsnNode}, i.e., the
   * flags.
   *
   * @param methodInsn {@link MethodInsnNode} to extract from.
   * @return Collection with flags to `MagikObjectUtils.createMethod()`.
   */
  private static List<Integer> getIconstValues(final MethodInsnNode methodInsn) {
    AbstractInsnNode current = methodInsn.getPrevious();

    // Get all ICONST_0 .. ICONST_5 instructions.
    final List<InsnNode> insnNodes = new ArrayList<>();
    while (current != null
        && current.getOpcode() >= Opcodes.ICONST_0
        && current.getOpcode() <= Opcodes.ICONST_5) {
      final InsnNode insnNode = (InsnNode) current;
      insnNodes.add(insnNode);

      current = current.getPrevious();
    }

    Collections.reverse(insnNodes);
    return insnNodes.stream()
        .map(insnNode -> insnNode.getOpcode() - Opcodes.ICONST_0)
        .collect(Collectors.toList());
  }
}
