package nl.ramsolutions.sw.magik.jacoco.generators;

import nl.ramsolutions.sw.magik.jacoco.TestData;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;

import static org.assertj.core.api.Assertions.assertThat;

public class MagikDirectorySourceFileLocatorTest {

    private static final String PACKAGE_NAME = "magik/fixture_product/fixture_module";
    private static final String FILENAME = "modules/fixture_module/source/char16_vector.magik";

    @Test
    void testGetSourceFile() throws IOException {
        final MagikDirectorySourceFileLocator locator = new MagikDirectorySourceFileLocator(TestData.PRODUCT_PATHS);
        final Reader reader = locator.getSourceFile(PACKAGE_NAME, FILENAME);
        assertThat(reader).isNotNull();
    }

}
