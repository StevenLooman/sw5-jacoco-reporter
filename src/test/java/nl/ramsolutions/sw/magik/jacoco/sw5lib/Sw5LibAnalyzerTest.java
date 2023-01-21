package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import nl.ramsolutions.sw.magik.jacoco.TestData;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Sw5LibAnalyzer.
 */
class Sw5LibAnalyzerTest {

    @Test
    void test() throws IOException {
        final Sw5LibAnalyzer libAnalyzer = TestData.getLibAnalyzer();

        final String magikMethod1 = libAnalyzer.getMagikMethodName(
            TestData.SUBSIDIARY_CLASS_CHAR16_VECTOR,
            "char16_vector__method1");
        assertThat(magikMethod1).isEqualTo("char16_vector.method1()");

        final String magikMethod2 = libAnalyzer.getMagikMethodName(
            TestData.SUBSIDIARY_CLASS_CHAR16_VECTOR,
            "char16_vector__method2");
        assertThat(magikMethod2).isEqualTo("char16_vector.method2()");

        final String magikMethod3 = libAnalyzer.getMagikMethodName(
            TestData.SUBSIDIARY_CLASS_CHAR16_VECTOR,
            "char16_vector__method3?");
        assertThat(magikMethod3).isEqualTo("char16_vector.method3?()");

        final String magikMethod4 = libAnalyzer.getMagikMethodName(
            TestData.SUBSIDIARY_CLASS_MIXED,
            "float__plus_100");
        assertThat(magikMethod4).isEqualTo("float.plus_100()");

        final String magikMethod5 = libAnalyzer.getMagikMethodName(
            TestData.SUBSIDIARY_CLASS_MIXED,
            "integer__plus_100");
        assertThat(magikMethod5).isEqualTo("integer.plus_100()");
    }

}
