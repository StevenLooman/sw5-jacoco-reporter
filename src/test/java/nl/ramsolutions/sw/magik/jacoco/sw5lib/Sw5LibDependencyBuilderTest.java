package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import nl.ramsolutions.sw.magik.jacoco.TestData;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Sw5LibDependencyBuilder.
 */
class Sw5LibDependencyBuilderTest {

    @SafeVarargs
    private void assertMappingContainsOnly(
            final Map<MethodNode, MethodNode> map,
            final Map.Entry<String, String>... entries) {
        final Map<String, String> namesMap = map.entrySet().stream()
            .map(entry -> {
                final MethodNode key = entry.getKey();
                final MethodNode value = entry.getValue();
                return Map.entry(key.name, value.name);
            })
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue));
        assertThat(namesMap)
            .containsOnly(entries);
    }

    @Test
    void testBuildMethodDependencyMapChar16Vector1() throws IOException {
        final Sw5LibReader libReader = TestData.getLibReader();
        final ClassNode primaryClassNode =
            libReader.getClassByName(TestData.PRIMARY_CLASS_CHAR16_VECTOR + ".class");
        final ClassNode subsidiaryClassNode =
            libReader.getClassByName(TestData.SUBSIDIARY_CLASS_CHAR16_VECTOR + ".class");

        final Map<MethodNode, MethodNode> methodDependencyMap =
            Sw5LibDependencyBuilder.buildMethodDependencyMap(primaryClassNode, subsidiaryClassNode);
        this.assertMappingContainsOnly(
            methodDependencyMap,
            Map.entry("__loopbody_", "char16_vector__method1"));
    }

    @Test
    void testBuildMethodDependencyMapChar16Vector2() throws IOException {
        final Sw5LibReader libReader = TestData.getLibReader();
        final ClassNode subsidiaryClassNode =
            libReader.getClassByName(TestData.SUBSIDIARY_CLASS_CHAR16_VECTOR + ".class");

        final Map<MethodNode, MethodNode> methodDependencyMap =
            Sw5LibDependencyBuilder.buildMethodDependencyMap(subsidiaryClassNode, subsidiaryClassNode);
        this.assertMappingContainsOnly(
            methodDependencyMap,
            Map.entry("__loopbody_", "char16_vector__method1"));
    }

    @Test
    void testBuildMethodDependencyMapMixed1() throws IOException {
        final Sw5LibReader libReader = TestData.getLibReader();
        final ClassNode primaryClassNode = libReader.getClassByName(TestData.PRIMARY_CLASS_MIXED + ".class");
        final ClassNode subsidiaryClassNode = libReader.getClassByName(TestData.SUBSIDIARY_CLASS_MIXED + ".class");

        final Map<MethodNode, MethodNode> methodDependencyMap =
            Sw5LibDependencyBuilder.buildMethodDependencyMap(primaryClassNode, subsidiaryClassNode);
        this.assertMappingContainsOnly(
            methodDependencyMap,
            Map.entry("proc__name_", "execute"),
            Map.entry("proc___2", "execute"),
            Map.entry("__loopbody_", "execute"),
            Map.entry("__loopbody_2", "execute"),
            Map.entry("proc___3", "execute"),
            Map.entry("proc___4", "execute"),
            Map.entry("proc___5", "execute"),

            Map.entry("proc___", "symbol__with_suffix"),
            Map.entry("__loopbody_3", "__loopbody_2"),
            Map.entry("proc___6", "proc___5"));
    }

    @Test
    void testBuildMethodDependencyMapMixed2() throws IOException {
        final Sw5LibReader libReader = TestData.getLibReader();
        final ClassNode subsidiaryClassNode = libReader.getClassByName(TestData.SUBSIDIARY_CLASS_MIXED + ".class");

        final Map<MethodNode, MethodNode> methodDependencyMap =
            Sw5LibDependencyBuilder.buildMethodDependencyMap(subsidiaryClassNode, subsidiaryClassNode);
        this.assertMappingContainsOnly(
            methodDependencyMap,
            Map.entry("proc___", "symbol__with_suffix"),
            Map.entry("__loopbody_3", "__loopbody_2"),
            Map.entry("proc___6", "proc___5"));
    }

    @Test
    void testBuildMethodDependencyMapPrimary() throws IOException {
        final Sw5LibReader libReader = TestData.getLibReader();
        final ClassNode primaryClassNode = libReader.getClassByName(TestData.PRIMARY_CLASS_PRIMARY + ".class");
        final ClassNode subsidiaryClassNode = null;

        final Map<MethodNode, MethodNode> methodDependencyMap =
            Sw5LibDependencyBuilder.buildMethodDependencyMap(primaryClassNode, subsidiaryClassNode);
        assertThat(methodDependencyMap)
            .isEmpty();
    }

}
