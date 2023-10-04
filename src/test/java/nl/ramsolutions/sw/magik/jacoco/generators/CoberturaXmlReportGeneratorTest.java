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

class CoberturaXmlReportGeneratorTest {

    @Test
    void testGeneratorRunsWithoutFailure() throws IOException {
        final List<Path> productPaths = TestData.PRODUCT_PATHS;
        final List<Path> sourcePaths = Collections.emptyList();
        final File executionDataFile = TestData.JACOCO_EXEC_FILE;
        final File outputFile = Files.createTempFile("cobertura", ".xml").toFile();
        final boolean discardExecutable = true;
        final boolean discardNonMagik = true;
        final String bundleName = "TestCobertura";
        final CoberturaXmlReportGenerator coberturaXmlReportGenerator = new CoberturaXmlReportGenerator(
            productPaths,
            sourcePaths,
            executionDataFile,
            outputFile,
            discardExecutable,
            discardNonMagik,
            bundleName);
        coberturaXmlReportGenerator.run();

        // Assume that if it exists, all went well.
        assertThat(outputFile).exists();
    }

}
