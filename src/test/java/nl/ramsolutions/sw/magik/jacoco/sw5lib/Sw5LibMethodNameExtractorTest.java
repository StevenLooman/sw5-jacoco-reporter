package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import nl.ramsolutions.sw.magik.jacoco.TestData;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Sw5LibMethodNameExtractor.
 */
class Sw5LibMethodNameExtractorTest {

    static Sw5LibReader getLibReader() throws IOException {
        return new Sw5LibReader(TestData.PRODUCT_PATHS);
    }

    @Test
    void testExtractMethodName() throws IOException {
        final Sw5LibReader libReader = Sw5LibMethodNameExtractorTest.getLibReader();
        final ClassNode classNode = libReader.getExecutableClassNodes().stream()
            .filter(classNode_ -> classNode_.name.equals(TestData.EXECUTABLE_CLASS_CHAR16_VECTOR))
            .findAny()
            .orElseThrow();
        final MethodNode methodNode = classNode.methods.stream()
            .filter(method -> method.name.equals("execute"))
            .findFirst()
            .orElseThrow();

        final MethodInsnNode methodInsn = (MethodInsnNode) methodNode.instructions.get(14);
        final Entry<String, String> extractMethodName = Sw5LibMethodNameExtractor.extractMethodName(methodInsn);
        final String javaMethodName = "magik.fixture_product.fixture_module.char16_vector_37.char16_vector__method1";
        final String magikMethodName = "char16_vector.method1()";
        assertThat(extractMethodName).isEqualTo(Map.entry(javaMethodName, magikMethodName));
    }

}
