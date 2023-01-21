package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import nl.ramsolutions.sw.magik.jacoco.TestData;
import nl.ramsolutions.sw.magik.jacoco.helpers.ClassNodeHelper;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Sw5LibMethodNameExtractor.
 */
class Sw5LibMethodNameExtractorTest {

    @Test
    void testExtractMethodNames() throws IOException {
        final Sw5LibReader libReader = TestData.getLibReader();
        final ClassNode classNode = libReader.getPrimaryClassNodes().stream()
            .filter(classNode_ -> classNode_.name.equals(TestData.PRIMARY_CLASS_CHAR16_VECTOR))
            .findAny()
            .orElseThrow();
        final MethodNode methodNode =
            ClassNodeHelper.getMethodNode(classNode, "execute");

        final String subsidiaryClassName = TestData.SUBSIDIARY_CLASS_CHAR16_VECTOR.replaceAll("/", ".");
        final Map<String, String> methodNames = Sw5LibMethodNameExtractor.extractMethodNames(methodNode);
        assertThat(methodNames)
            .containsOnly(
                Map.entry(
                    subsidiaryClassName + ".char16_vector__method1",
                    "char16_vector.method1()"),
                Map.entry(
                    subsidiaryClassName + ".char16_vector__method2",
                    "char16_vector.method2()"),
                Map.entry(
                    subsidiaryClassName + ".char16_vector__method3?",
                    "char16_vector.method3?()"));
    }

}
