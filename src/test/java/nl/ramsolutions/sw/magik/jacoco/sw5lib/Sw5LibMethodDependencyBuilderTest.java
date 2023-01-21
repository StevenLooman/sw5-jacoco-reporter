package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import nl.ramsolutions.sw.magik.jacoco.TestData;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Sw5LibMethodDependencyBuilder.
 */
class Sw5LibMethodDependencyBuilderTest {

    static Sw5LibReader getLibReader() throws IOException {
        return new Sw5LibReader(TestData.PRODUCT_PATHS);
    }

    @Test
    void testBuildDependencyMap() throws IOException {
        final Sw5LibReader libReader = Sw5LibMethodDependencyBuilderTest.getLibReader();
        final ClassNode classNode = libReader.getClassByName(TestData.CLASS_CHAR16_VECTOR);

        final Map<MethodNode, MethodNode> nethodDependencyMap =
            Sw5LibDependencyBuilder.buildMethodDependencyMap(classNode, classNode);
        final MethodNode loopbodyMethodNode = classNode.methods.stream()
            .filter(methodNode -> methodNode.name.equals("__loopbody_"))
            .findAny()
            .orElseThrow();
        final MethodNode parentNode = nethodDependencyMap.get(loopbodyMethodNode);
        assertThat(parentNode).isNotNull();
        assertThat(parentNode.name).isEqualTo("char16_vector__method1");
    }

}
