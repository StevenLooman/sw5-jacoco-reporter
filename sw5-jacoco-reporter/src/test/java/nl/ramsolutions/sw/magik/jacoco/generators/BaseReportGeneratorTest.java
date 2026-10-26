package nl.ramsolutions.sw.magik.jacoco.generators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.ramsolutions.sw.magik.jacoco.TestData;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.tools.ExecFileLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests for {@link BaseReportGenerator}. */
class BaseReportGeneratorTest {

  /** Report generator which captures the {@link IBundleCoverage}, instead of writing a report. */
  private static final class CapturingReportGenerator extends BaseReportGenerator {

    private IBundleCoverage bundleCoverage;

    CapturingReportGenerator(final List<File> executionDataFiles) {
      super(
          TestData.PRODUCT_PATHS,
          Collections.emptyList(),
          executionDataFiles,
          null,
          true,
          true,
          "Test");
    }

    @Override
    protected void createReport(final IBundleCoverage bundleCoverage) {
      this.bundleCoverage = bundleCoverage;
    }

    IBundleCoverage getBundleCoverage() throws IOException {
      this.run();
      return this.bundleCoverage;
    }
  }

  @TempDir private Path tempDir;

  private static Collection<ExecutionData> readFixtureExecutionData() throws IOException {
    final ExecFileLoader execFileLoader = new ExecFileLoader();
    execFileLoader.load(TestData.JACOCO_EXEC_FILE);
    return execFileLoader.getExecutionDataStore().getContents();
  }

  private static ExecutionData readFixtureExecutionData(final String className) throws IOException {
    return BaseReportGeneratorTest.readFixtureExecutionData().stream()
        .filter(data -> data.getName().equals(className))
        .findFirst()
        .orElseThrow();
  }

  private File writeExecFile(final String name, final Collection<ExecutionData> executionData)
      throws IOException {
    final File file = this.tempDir.resolve(name).toFile();
    try (OutputStream output = new FileOutputStream(file)) {
      final ExecutionDataWriter writer = new ExecutionDataWriter(output);
      writer.visitSessionInfo(new SessionInfo(name, 0, 1));
      executionData.forEach(writer::visitClassExecution);
    }
    return file;
  }

  /**
   * Split the fixture execution data over two exec files, alternating the hit probes.
   *
   * @return The two exec files, together holding all hits of the fixture.
   */
  private List<File> writeSplitExecFiles() throws IOException {
    final List<ExecutionData> first = new ArrayList<>();
    final List<ExecutionData> second = new ArrayList<>();
    int hitCount = 0;
    for (final ExecutionData data : BaseReportGeneratorTest.readFixtureExecutionData()) {
      final boolean[] probes = data.getProbes();
      final boolean[] firstProbes = new boolean[probes.length];
      final boolean[] secondProbes = new boolean[probes.length];
      for (int i = 0; i < probes.length; ++i) {
        if (probes[i]) {
          if (hitCount % 2 == 0) {
            firstProbes[i] = true;
          } else {
            secondProbes[i] = true;
          }
          hitCount++;
        }
      }
      first.add(new ExecutionData(data.getId(), data.getName(), firstProbes));
      second.add(new ExecutionData(data.getId(), data.getName(), secondProbes));
    }
    return List.of(
        this.writeExecFile("first.exec", first), this.writeExecFile("second.exec", second));
  }

  private static Map<String, Integer> coveredLinesPerClass(final IBundleCoverage bundleCoverage) {
    final Map<String, Integer> coveredLines = new HashMap<>();
    bundleCoverage.getPackages().stream()
        .flatMap(packageCoverage -> packageCoverage.getClasses().stream())
        .forEach(
            classCoverage ->
                coveredLines.put(
                    classCoverage.getName(), classCoverage.getLineCounter().getCoveredCount()));
    return coveredLines;
  }

  private static int totalCoveredLines(final IBundleCoverage bundleCoverage) {
    return bundleCoverage.getLineCounter().getCoveredCount();
  }

  @Test
  void testMultipleExecFilesAreMerged() throws IOException {
    final IBundleCoverage singleCoverage =
        new CapturingReportGenerator(List.of(TestData.JACOCO_EXEC_FILE)).getBundleCoverage();
    final List<File> splitFiles = this.writeSplitExecFiles();
    final IBundleCoverage firstCoverage =
        new CapturingReportGenerator(List.of(splitFiles.get(0))).getBundleCoverage();
    final IBundleCoverage mergedCoverage =
        new CapturingReportGenerator(splitFiles).getBundleCoverage();

    // Sanity check: one part alone does not hold all coverage.
    assertThat(totalCoveredLines(firstCoverage)).isLessThan(totalCoveredLines(singleCoverage));

    assertThat(coveredLinesPerClass(mergedCoverage))
        .isEqualTo(coveredLinesPerClass(singleCoverage));
  }

  @Test
  void testSessionInfosOfAllExecFilesAreKept() throws IOException {
    final CapturingReportGenerator generator =
        new CapturingReportGenerator(this.writeSplitExecFiles());
    generator.getBundleCoverage();

    final List<SessionInfo> infos = generator.getExecFileLoader().getSessionInfoStore().getInfos();
    assertThat(infos).extracting(SessionInfo::getId).containsExactly("first.exec", "second.exec");
  }

  @Test
  void testIncompatibleExecFileIsReported() throws IOException {
    final ExecutionData data =
        BaseReportGeneratorTest.readFixtureExecutionData(TestData.SUBSIDIARY_CLASS_MIXED);
    // Probe count differs, as if recorded with a different version. Without hits, it is not
    // written.
    final boolean[] incompatibleProbes = new boolean[data.getProbes().length + 1];
    incompatibleProbes[0] = true;
    final ExecutionData incompatibleData =
        new ExecutionData(data.getId(), data.getName(), incompatibleProbes);
    final File incompatibleFile =
        this.writeExecFile("incompatible.exec", List.of(incompatibleData));
    final CapturingReportGenerator generator =
        new CapturingReportGenerator(List.of(TestData.JACOCO_EXEC_FILE, incompatibleFile));

    assertThatThrownBy(generator::run)
        .isInstanceOf(IOException.class)
        .hasMessageContaining(incompatibleFile.toString());
  }

  @Test
  void testNoMatchClassesAreWarned() throws IOException {
    final ExecutionData data =
        BaseReportGeneratorTest.readFixtureExecutionData(TestData.SUBSIDIARY_CLASS_MIXED);
    final ExecutionData staleData =
        new ExecutionData(data.getId() + 1, data.getName(), data.getProbes());
    final File staleFile = this.writeExecFile("stale.exec", List.of(staleData));
    final CapturingReportGenerator generator = new CapturingReportGenerator(List.of(staleFile));

    final ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
    final PrintStream originalErr = System.err;
    final IBundleCoverage bundleCoverage;
    try {
      System.setErr(new PrintStream(errBuffer, true, StandardCharsets.UTF_8));
      bundleCoverage = generator.getBundleCoverage();
    } finally {
      System.setErr(originalErr);
    }

    final String err = errBuffer.toString(StandardCharsets.UTF_8);
    assertThat(err).contains("do not match").contains(data.getName());
    assertThat(bundleCoverage).isNotNull();
  }

  @Test
  void testSingleExecFileHasNoNoMatchWarning() throws IOException {
    final CapturingReportGenerator generator =
        new CapturingReportGenerator(List.of(TestData.JACOCO_EXEC_FILE));

    final ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
    final PrintStream originalErr = System.err;
    try {
      System.setErr(new PrintStream(errBuffer, true, StandardCharsets.UTF_8));
      generator.run();
    } finally {
      System.setErr(originalErr);
    }

    assertThat(errBuffer.toString(StandardCharsets.UTF_8)).doesNotContain("do not match");
  }
}
