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

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    void testGetPrimaryClassNodes() throws IOException {
        final Sw5LibReader libReader = TestData.getLibReader();
        final Collection<ClassNode> primaryClassNodes = libReader.getPrimaryClassNodes();
        assertThat(primaryClassNodes).hasSize(3);
        final Set<String> classNodeNames = primaryClassNodes.stream()
            .map(classNode -> classNode.name)
            .collect(Collectors.toSet());
        assertThat(classNodeNames).containsOnly(
            TestData.PRIMARY_CLASS_CHAR16_VECTOR,
            TestData.PRIMARY_CLASS_MIXED,
            TestData.PRIMARY_CLASS_PRIMARY);
    }

    @Test
    void testGetSubsidiaryClassNodes() throws IOException {
        final Sw5LibReader libReader = TestData.getLibReader();
        final Collection<ClassNode> subsidiaryClassNodes = libReader.getSubsidiaryClassNodes();
        assertThat(subsidiaryClassNodes).hasSize(2);
        final Set<String> classNodeNames = subsidiaryClassNodes.stream()
            .map(classNode -> classNode.name)
            .collect(Collectors.toSet());
        assertThat(classNodeNames).containsOnly(
            TestData.SUBSIDIARY_CLASS_CHAR16_VECTOR,
            TestData.SUBSIDIARY_CLASS_MIXED);
    }

    @Test
    void testGetClassByName() throws IOException {
        final Sw5LibReader libReader = TestData.getLibReader();
        final ClassNode classNode = libReader.getClassByName(TestData.PRIMARY_CLASS_CHAR16_VECTOR + ".class");
        assertThat(classNode).isNotNull();

        final ClassNode classNodeUnexisting =
            libReader.getClassByName(TestData.CLASS_DOES_NOT_EXIST + ".class");
        assertThat(classNodeUnexisting).isNull();
    }

}
