package nl.ramsolutions.sw.magik.jacoco.helpers;

import nl.ramsolutions.sw.magik.jacoco.TestData;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;

import static org.assertj.core.api.Assertions.assertThat;

class SmallworldProductsSourceFileLocatorTest {

    private static final String PACKAGE_NAME = "magik/fixture_product/fixture_module";
    private static final String FILENAME = "modules/fixture_module/source/char16_vector.magik";

    @Test
    void testGetSourceFile() throws IOException {
        final SmallworldProducts smallworldProducts = new SmallworldProducts(TestData.PRODUCT_PATHS);
        final SmallworldProductsSourceFileLocator locator = new SmallworldProductsSourceFileLocator(smallworldProducts);
        final Reader reader = locator.getSourceFile(PACKAGE_NAME, FILENAME);
        assertThat(reader).isNotNull();
    }

    @Test
    void testGetNonMagikSourceFile() throws IOException {
        final SmallworldProducts smallworldProducts = new SmallworldProducts(TestData.PRODUCT_PATHS);
        final SmallworldProductsSourceFileLocator locator = new SmallworldProductsSourceFileLocator(smallworldProducts);
        final Reader reader = locator.getSourceFile(
            "nl/ramsolutions/sw/magik/jacoco/helpers",
            "SmallworldProductsSourceFileLocator.java");
        assertThat(reader).isNull();
    }

}
