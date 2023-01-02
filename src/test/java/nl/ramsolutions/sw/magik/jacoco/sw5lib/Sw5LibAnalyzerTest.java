package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

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
        final Map<String, String> extractAllMethodNames = libAnalyzer.extractAllMethodNames();
        assertThat(extractAllMethodNames).containsOnly(
            Map.entry(
                "magik.fixture_product.fixture_module.char16_vector_32.char16_vector__method1",
                "char16_vector.method1()"),
            Map.entry(
                "magik.fixture_product.fixture_module.char16_vector_32.char16_vector__method2",
                "char16_vector.method2()"),
            Map.entry(
                "magik.fixture_product.fixture_module.char16_vector_32.char16_vector__method3?",
                "char16_vector.method3?()"),
            Map.entry(
                "magik.fixture_product.fixture_module.other_39.float__plus_100",
                "float.plus_100()"),
            Map.entry(
                "magik.fixture_product.fixture_module.other_39.integer__plus_100",
                "integer.plus_100()"));
    }

}
