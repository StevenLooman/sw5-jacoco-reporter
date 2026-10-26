package nl.ramsolutions.sw.magik.jacoco.helpers;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.List;
import nl.ramsolutions.sw.magik.jacoco.TestData;
import org.junit.jupiter.api.Test;

/** Tests for {@link ExecFilesLoader}. */
class ExecFilesLoaderTest {

  @Test
  void testExistingFileIsNotMissing() {
    final List<File> missingFiles =
        ExecFilesLoader.findMissingFiles(List.of(TestData.JACOCO_EXEC_FILE));

    assertThat(missingFiles).isEmpty();
  }

  @Test
  void testNonExistingFilesAreMissing() {
    final File missingFile1 = TestData.PRODUCT_PATH.resolve("missing1.exec").toFile();
    final File missingFile2 = TestData.PRODUCT_PATH.resolve("missing2.exec").toFile();
    final List<File> missingFiles =
        ExecFilesLoader.findMissingFiles(
            List.of(missingFile1, TestData.JACOCO_EXEC_FILE, missingFile2));

    assertThat(missingFiles).containsExactly(missingFile1, missingFile2);
  }

  @Test
  void testDirectoryIsMissing() {
    final File directory = TestData.PRODUCT_PATH.toFile();
    final List<File> missingFiles = ExecFilesLoader.findMissingFiles(List.of(directory));

    assertThat(missingFiles).containsExactly(directory);
  }
}
