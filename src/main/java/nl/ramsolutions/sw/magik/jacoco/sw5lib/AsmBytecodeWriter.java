package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Helper class to dump Java byte code of a class/method.
 */
public final class AsmBytecodeWriter {

    private AsmBytecodeWriter() {
    }

    public static void printMethods(final ClassNode classNode) {
        classNode.methods
            .forEach(AsmBytecodeWriter::printMethod);
    }

    public static void printMethod(final MethodNode methodNode) {
        System.out.println(methodNode.name);  // NOSONAR

        final InsnList inList = methodNode.instructions;
        for (int i = 0; i < inList.size(); i++) {
            final AbstractInsnNode abstractInsnNode = inList.get(i);
            final String insnString = AsmBytecodeWriter.insnToString(abstractInsnNode);
            System.out.print(insnString);  // NOSONAR
        }
    }

    private static String insnToString(final AbstractInsnNode insnNode) {
        final Printer printer = new Textifier();
        final TraceMethodVisitor traceMethodVisitor = new TraceMethodVisitor(printer);
        insnNode.accept(traceMethodVisitor);

        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);
        printer.print(printWriter);
        return stringWriter.toString();
    }

}
