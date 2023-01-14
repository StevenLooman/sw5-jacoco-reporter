package nl.ramsolutions.sw.magik.jacoco.generators;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MagikDirectorySourceFileLocatorTest {

    private static final Path PRODUCT_PATH = Path.of("src/test/resources/fixture_product");
    private static final List<Path> PRODUCT_PATHS = List.of(PRODUCT_PATH);
    private static final String PACKAGE_NAME = "magik/fixture_product/fixture_module";
    private static final String FILENAME = "modules/fixture_module/source/char16_vector.magik";

    @Test
    void testGetSourceFile() throws IOException {
        final MagikDirectorySourceFileLocator locator = new MagikDirectorySourceFileLocator(PRODUCT_PATHS);
        final Reader reader = locator.getSourceFile(PACKAGE_NAME, FILENAME);
        assertThat(reader).isNotNull();
    }

}
