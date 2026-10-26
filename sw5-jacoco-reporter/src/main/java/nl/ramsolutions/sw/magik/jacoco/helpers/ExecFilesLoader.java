package nl.ramsolutions.sw.magik.jacoco.helpers;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.jacoco.core.tools.ExecFileLoader;

/** Loads multiple {@literal jacoco.exec} files, merging their execution data. */
public final class ExecFilesLoader {

  private ExecFilesLoader() {}

  /**
   * Find the {@literal jacoco.exec} files which do not exist, or are not regular files.
   *
   * @param executionDataFiles Files to {@literal jacoco.exec}.
   * @return Missing files, in the given order.
   */
  public static List<File> findMissingFiles(final List<File> executionDataFiles) {
    return executionDataFiles.stream()
        .filter(executionDataFile -> !executionDataFile.isFile())
        .collect(Collectors.toList());
  }

  /**
   * Load the execution data from all {@literal jacoco.exec} files.
   *
   * <p>The execution data of all files is merged: a probe is hit if it is hit in any file.
   *
   * @param execFileLoader {@link ExecFileLoader} to load into.
   * @param executionDataFiles Files to {@literal jacoco.exec}.
   * @throws IOException If a file cannot be read, or holds data incompatible with earlier files.
   */
  public static void load(final ExecFileLoader execFileLoader, final List<File> executionDataFiles)
      throws IOException {
    for (final File executionDataFile : executionDataFiles) {
      try {
        execFileLoader.load(executionDataFile);
      } catch (final IllegalStateException exception) {
        throw new IOException(
            "Incompatible execution data in "
                + executionDataFile
                + ", was it recorded with another JaCoCo agent version or other libs? "
                + exception.getMessage(),
            exception);
      }
    }
  }
}
