package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Sw5LibMethodNameExtractor.
 */
class Sw5LibMethodNameExtractorTest {

    private static final Path PRODUCT_DIR = Path.of("src/test/resources/fixture_product");
    private static final String EXEUCATBLE_CLASS_NAME = "magik/fixture_product/fixture_module/char16_vector_31";

    static Sw5LibReader getLibReader() throws IOException {
        return new Sw5LibReader(PRODUCT_DIR);
    }

    @Test
    void testExtractMethodName() throws IOException {
        final Sw5LibReader libReader = Sw5LibMethodNameExtractorTest.getLibReader();
        final ClassNode classNode = libReader.getExecutableClassNodes().stream()
                .filter(classNode_ -> classNode_.name.equals(EXEUCATBLE_CLASS_NAME))
                .findAny()
                .orElseThrow();
        final MethodNode methodNode = classNode.methods.stream()
                .filter(method -> method.name.equals("execute"))
                .findFirst()
                .orElseThrow();

        final MethodInsnNode methodInsn = (MethodInsnNode) methodNode.instructions.get(14);
        final Entry<String, String> extractMethodName = Sw5LibMethodNameExtractor.extractMethodName(methodInsn);
        final String javaMethodName = "magik.fixture_product.fixture_module.char16_vector_32.char16_vector__method1";
        final String magikMethodName = "char16_vector.method1()";
        assertThat(extractMethodName).isEqualTo(Map.entry(javaMethodName, magikMethodName));
    }

}
