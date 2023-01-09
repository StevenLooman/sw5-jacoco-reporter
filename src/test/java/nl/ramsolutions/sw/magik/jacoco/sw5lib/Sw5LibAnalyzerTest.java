package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Sw5LibAnalyzer.
 */
class Sw5LibAnalyzerTest {

    private static final Path PRODUCT_DIR = Path.of("src/test/resources/fixture_product");

    static Sw5LibReader getLibReader() throws IOException {
        return new Sw5LibReader(PRODUCT_DIR);
    }

    @Test
    void test() throws IOException {
        final Sw5LibReader libReader = Sw5LibAnalyzerTest.getLibReader();
        final Sw5LibAnalyzer libAnalyzer = new Sw5LibAnalyzer(libReader);

        final String magikMethod1 = libAnalyzer.getMagikMethodName(
            "magik.fixture_product.fixture_module.char16_vector_32",
            "char16_vector__method1");
        assertThat(magikMethod1).isEqualTo("char16_vector.method1()");

        final String magikMethod2 = libAnalyzer.getMagikMethodName(
            "magik.fixture_product.fixture_module.char16_vector_32",
            "char16_vector__method2");
        assertThat(magikMethod2).isEqualTo("char16_vector.method2()");

        final String magikMethod3 = libAnalyzer.getMagikMethodName(
            "magik.fixture_product.fixture_module.char16_vector_32",
            "char16_vector__method3?");
        assertThat(magikMethod3).isEqualTo("char16_vector.method3?()");

        final String magikMethod4 = libAnalyzer.getMagikMethodName(
            "magik.fixture_product.fixture_module.other_39",
            "float__plus_100");
        assertThat(magikMethod4).isEqualTo("float.plus_100()");

        final String magikMethod5 = libAnalyzer.getMagikMethodName(
            "magik.fixture_product.fixture_module.other_39",
            "integer__plus_100");
        assertThat(magikMethod5).isEqualTo("integer.plus_100()");
    }

}
