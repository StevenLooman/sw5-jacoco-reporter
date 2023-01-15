package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Sw5LibReader.
 */
class Sw5LibReaderTest {

    private static final Path PRODUCT_PATH = Path.of("src/test/resources/fixture_product");
    private static final List<Path> PRODUCT_PATHS = List.of(PRODUCT_PATH);
    private static final String EXECUTABLE_CLASS_CHAR16_VECTOR =
        "magik/fixture_product/fixture_module/char16_vector_35";
    private static final String EXECUTABLE_CLASS_OTHER = "magik/fixture_product/fixture_module/other_48";
    private static final String EXECUTABLE_CLASS_NAME_DOES_NOT_EXIST =
        "magik/fixture_product/fixture_module/does_not_exist_99";
    private static final String CLASS_CHAR16_VECTOR = "magik/fixture_product/fixture_module/char16_vector_36";
    private static final String CLASS_OTHER = "magik/fixture_product/fixture_module/other_49";

    static Sw5LibReader getLibReader() throws IOException {
        return new Sw5LibReader(PRODUCT_PATHS);
    }

    @Test
    void testGetExecutableClassNodes() throws IOException {
        final Sw5LibReader libReader = Sw5LibReaderTest.getLibReader();
        final Collection<ClassNode> executableClassNodes = libReader.getExecutableClassNodes();
        assertThat(executableClassNodes).hasSize(2);
        final Set<String> classNodeNames = executableClassNodes.stream()
            .map(classNode -> classNode.name)
            .collect(Collectors.toSet());
        assertThat(classNodeNames).containsOnly(
            EXECUTABLE_CLASS_CHAR16_VECTOR,
            EXECUTABLE_CLASS_OTHER);
    }

    @Test
    void testGetSubsidiaryClassNodes() throws IOException {
        final Sw5LibReader libReader = Sw5LibReaderTest.getLibReader();
        final Collection<ClassNode> executableClassNodes = libReader.getSubsidiaryClassNodes();
        assertThat(executableClassNodes).hasSize(2);
        final Set<String> classNodeNames = executableClassNodes.stream()
            .map(classNode -> classNode.name)
            .collect(Collectors.toSet());
        assertThat(classNodeNames).containsOnly(
            CLASS_CHAR16_VECTOR,
            CLASS_OTHER);
    }

    @Test
    void testGetClassByName() throws IOException {
        final Sw5LibReader libReader = Sw5LibReaderTest.getLibReader();
        final ClassNode classNode = libReader.getClassByName(EXECUTABLE_CLASS_CHAR16_VECTOR + ".class");
        assertThat(classNode).isNotNull();

        final ClassNode classNodeUnexisting = libReader.getClassByName(EXECUTABLE_CLASS_NAME_DOES_NOT_EXIST + ".class");
        assertThat(classNodeUnexisting).isNull();
    }

}
