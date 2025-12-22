package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import nl.ramsolutions.sw.magik.jacoco.helpers.MethodNodeHelper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodNode;

/** Utility class to extract proc name from INVOKEDYNAMIC proc calls. */
final class Sw5LibProcDefinitionExtractor {

  private static final String PROC_DEFINITION_OWNER =
      "com/gesmallworld/magik/language/invokers/ConstantBuilder";
  private static final String PROC_DEFINITION_NAME = "proc";

  private Sw5LibProcDefinitionExtractor() {}

  /**
   * Extract Magik proc names.
   *
   * @param classNode ClassNode which might create procedures.
   * @return Map keyed on Java names, and the corresponding Magik names.
   */
  static Collection<ProcDefinition> extractProcDefinitions(final ClassNode classNode) {
    final MethodNode executeMethodNode = MethodNodeHelper.getExecuteMethodSafe(classNode);
    if (executeMethodNode == null) {
      return Collections.emptyList();
    }

    return Arrays.stream(executeMethodNode.instructions.toArray())
        .filter(insn -> insn.getOpcode() == Opcodes.INVOKEDYNAMIC)
        .map(InvokeDynamicInsnNode.class::cast)
        .filter(Sw5LibProcDefinitionExtractor::isCreateProcCall)
        .map(invokeDynamicInsnNode -> extractProcDefinition(classNode, invokeDynamicInsnNode))
        .toList();
  }

  private static boolean isCreateProcCall(InvokeDynamicInsnNode invokeDynamicInsnNode) {
    return invokeDynamicInsnNode.bsm.getOwner().equals(PROC_DEFINITION_OWNER)
        && invokeDynamicInsnNode.name.equals(PROC_DEFINITION_NAME);
  }

  /**
   * Extract the exemplar/method name from a INVOKEDYNAMIC call.
   *
   * @param invokeDynamicInsnNode {@link InvokeDynamicInsnNode} to extract from.
   * @return Java name / Magik name entry.
   */
  private static ProcDefinition extractProcDefinition(
      final ClassNode classNode, final InvokeDynamicInsnNode invokeDynamicInsnNode) {
    final Path sourceFile = Path.of(classNode.sourceFile);
    final Object[] bsmArgs = invokeDynamicInsnNode.bsmArgs;
    final Type javaType = (Type) bsmArgs[0];
    final String javaTypeName = javaType.getClassName();
    final String javaMethodName = (String) bsmArgs[1];
    final String procName = (String) bsmArgs[2];
    return new ProcDefinition(sourceFile, javaTypeName, javaMethodName, procName);
  }
}
