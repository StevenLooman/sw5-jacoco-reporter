package nl.ramsolutions.sw.magik.jacoco.generators;

import nl.ramsolutions.sw.magik.jacoco.TestData;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlReportGeneratorTest {

    @Test
    void testGeneratorRunsWithoutFailure() throws IOException {
        final List<Path> productPaths = TestData.PRODUCT_PATHS;
        final List<Path> sourcePaths = Collections.emptyList();
        final File executionDataFile = TestData.JACOCO_EXEC_FILE;
        final File outputDir = Files.createTempDirectory("html").toFile();
        final boolean discardExecutable = true;
        final String bundleName = "TestHtml";
        final HtmlReportGenerator htmlReportGenerator = new HtmlReportGenerator(
            productPaths,
            sourcePaths,
            executionDataFile,
            outputDir,
            discardExecutable,
            bundleName);
        htmlReportGenerator.run();

        // Assume that if it exists, all went well.
        final Path indexHtml = outputDir.toPath().resolve("index.html");
        assertThat(indexHtml).exists();
    }

}
