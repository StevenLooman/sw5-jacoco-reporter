package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Sw5LibReader.
 */
class Sw5LibReaderTest {

    private static final Path PRODUCT_DIR = Path.of("src/test/resources/fixture_product");

    static Sw5LibReader getLibReader() throws IOException {
        return new Sw5LibReader(PRODUCT_DIR);
    }

    @Test
    void testGetExecutableClassNodes() throws IOException {
        final Sw5LibReader libReader = Sw5LibReaderTest.getLibReader();
        final Collection<ClassNode> executableClassNodes = libReader.getExecutableClassNodes();
        assertThat(executableClassNodes).hasSize(2);
        Set<String> classNodeNames = executableClassNodes.stream()
                .map(classNode -> classNode.name)
                .collect(Collectors.toSet());
        assertThat(classNodeNames).containsOnly(
            "magik/fixture_product/fixture_module/char16_vector_31",
            "magik/fixture_product/fixture_module/other_38");
    }

    @Test
    void testGetSubsidiaryClassNodes() throws IOException {
        final Sw5LibReader libReader = Sw5LibReaderTest.getLibReader();
        final Collection<ClassNode> executableClassNodes = libReader.getSubsidiaryClassNodes();
        assertThat(executableClassNodes).hasSize(2);
        Set<String> classNodeNames = executableClassNodes.stream()
                .map(classNode -> classNode.name)
                .collect(Collectors.toSet());
        assertThat(classNodeNames).containsOnly(
            "magik/fixture_product/fixture_module/char16_vector_32",
            "magik/fixture_product/fixture_module/other_39");
    }

    @Test
    void testGetClassByName() throws IOException {
        final Sw5LibReader libReader = Sw5LibReaderTest.getLibReader();
        ClassNode classNode = libReader.getClassByName("magik/fixture_product/fixture_module/char16_vector_31.class");
        assertThat(classNode).isNotNull();

        ClassNode classNodeUnexisting =
            libReader.getClassByName("magik/fixture_product/fixture_module/does_not_exist_99.class");
        assertThat(classNodeUnexisting).isNull();
    }

}
