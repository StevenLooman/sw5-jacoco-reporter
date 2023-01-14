package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Sw5LibMethodDependencyBuilder.
 */
class Sw5LibMethodDependencyBuilderTest {

    private static final Path PRODUCT_PATH = Path.of("src/test/resources/fixture_product");
    private static final List<Path> PRODUCT_PATHS = List.of(PRODUCT_PATH);
    private static final String CLASS_NODE_NAME = "magik/fixture_product/fixture_module/char16_vector_32.class";

    static Sw5LibReader getLibReader() throws IOException {
        return new Sw5LibReader(PRODUCT_PATHS);
    }

    @Test
    void testBuildDependencyMap() throws IOException {
        final Sw5LibReader libReader = Sw5LibMethodDependencyBuilderTest.getLibReader();
        final ClassNode classNode = libReader.getClassByName(CLASS_NODE_NAME);

        final Map<MethodNode, MethodNode> buildMethodDependencyMap =
            Sw5LibMethodDependencyBuilder.buildMethodDependencyMap(classNode);
        MethodNode loopbodyMethodNode = classNode.methods.stream()
                .filter(methodNode -> methodNode.name.equals("__loopbody_"))
                .findAny()
                .orElseThrow();
        MethodNode parentNode = buildMethodDependencyMap.get(loopbodyMethodNode);
        assertThat(parentNode).isNotNull();
        assertThat(parentNode.name).isEqualTo("char16_vector__method1");
    }

}
