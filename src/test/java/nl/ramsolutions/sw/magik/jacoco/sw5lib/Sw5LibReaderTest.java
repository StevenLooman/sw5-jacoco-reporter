package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import nl.ramsolutions.sw.magik.jacoco.TestData;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Sw5LibReader.
 */
class Sw5LibReaderTest {

    static Sw5LibReader getLibReader() throws IOException {
        return new Sw5LibReader(TestData.PRODUCT_PATHS);
    }

    @Test
    void testGetExecutableClassNodes() throws IOException {
        final Sw5LibReader libReader = Sw5LibReaderTest.getLibReader();
        final Collection<ClassNode> executableClassNodes = libReader.getExecutableClassNodes();
        assertThat(executableClassNodes).hasSize(3);
        final Set<String> classNodeNames = executableClassNodes.stream()
            .map(classNode -> classNode.name)
            .collect(Collectors.toSet());
        assertThat(classNodeNames).containsOnly(
            TestData.EXECUTABLE_CLASS_CHAR16_VECTOR,
            TestData.EXECUTABLE_CLASS_MIXED,
            TestData.EXECUTABLE_CLASS_PRIMARY);
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
            TestData.CLASS_CHAR16_VECTOR,
            TestData.CLASS_MIXED);
    }

    @Test
    void testGetClassByName() throws IOException {
        final Sw5LibReader libReader = Sw5LibReaderTest.getLibReader();
        final ClassNode classNode = libReader.getClassByName(TestData.EXECUTABLE_CLASS_CHAR16_VECTOR + ".class");
        assertThat(classNode).isNotNull();

        final ClassNode classNodeUnexisting =
            libReader.getClassByName(TestData.EXECUTABLE_CLASS_NAME_DOES_NOT_EXIST + ".class");
        assertThat(classNodeUnexisting).isNull();
    }

}
