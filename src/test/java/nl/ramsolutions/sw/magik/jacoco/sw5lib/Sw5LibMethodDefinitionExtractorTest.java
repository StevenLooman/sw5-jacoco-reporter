package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import nl.ramsolutions.sw.magik.jacoco.TestData;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ClassNode;

/** Tests for {@link Sw5LibMethodDefinitionExtractor}. */
class Sw5LibMethodDefinitionExtractorTest {

  @Test
  void testExtractMethodDefinitions() throws IOException {
    final Sw5LibReader libReader = TestData.getLibReader();
    final ClassNode classNode =
        libReader.getPrimaryClassNodes().stream()
            .filter(classNode_ -> classNode_.name.equals(TestData.PRIMARY_CLASS_CHAR16_VECTOR))
            .findAny()
            .orElseThrow();

    final String subsidiaryClassName = TestData.SUBSIDIARY_CLASS_CHAR16_VECTOR.replaceAll("/", ".");
    final Collection<MethodDefinition> methodDefinitions =
        Sw5LibMethodDefinitionExtractor.extractMethodDefinitions(classNode);
    assertThat(methodDefinitions)
        .containsOnly(
            new MethodDefinition(
                TestData.PATH_CHAR16_VECTOR,
                subsidiaryClassName,
                "char16_vector__method1",
                "char16_vector",
                "method1()",
                EnumSet.noneOf(MethodDefinition.Flag.class)),
            new MethodDefinition(
                TestData.PATH_CHAR16_VECTOR,
                subsidiaryClassName,
                "char16_vector__method2",
                "char16_vector",
                "method2()",
                EnumSet.noneOf(MethodDefinition.Flag.class)),
            new MethodDefinition(
                TestData.PATH_CHAR16_VECTOR,
                subsidiaryClassName,
                "char16_vector__method3?",
                "char16_vector",
                "method3?()",
                EnumSet.noneOf(MethodDefinition.Flag.class)),
            new MethodDefinition(
                TestData.PATH_CHAR16_VECTOR,
                subsidiaryClassName,
                "char16_vector__method4",
                "char16_vector",
                "method4()",
                EnumSet.of(MethodDefinition.Flag.ABSTRACT)),
            new MethodDefinition(
                TestData.PATH_CHAR16_VECTOR,
                subsidiaryClassName,
                "char16_vector__method12",
                "char16_vector",
                "method1()",
                EnumSet.noneOf(MethodDefinition.Flag.class)));
  }
}
