package nl.ramsolutions.sw.magik.jacoco.conversion;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import nl.ramsolutions.sw.magik.jacoco.TestData;
import nl.ramsolutions.sw.magik.jaranalyzer.SwJarAnalyzerAnalyzer;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.junit.jupiter.api.Test;

/** Tests for {@link MethodCoverageMerger}. */
class MethodCoverageMergerTest {

  @Test
  void testMergeMethods() throws IOException {
    final SwJarAnalyzerAnalyzer libAnalyzer = TestData.getLibAnalyzer();
    final IBundleCoverage bundleCoverageOrig = TestData.getBundleCoverage();
    final MagikBundleCoverageConverter converter =
        new MagikBundleCoverageConverter(libAnalyzer, bundleCoverageOrig, false, false);

    final IBundleCoverage bundleCoverage = converter.convert();

    // Primary classes, with subsidiary merged into them: char16_vector, mixed, primary.
    final IPackageCoverage packageCoverage0 = List.copyOf(bundleCoverage.getPackages()).get(0);
    assertThat(packageCoverage0.getClasses()).hasSize(3);

    final IClassCoverage classCoverage0 =
        packageCoverage0.getClasses().stream()
            .filter(
                classCoverage ->
                    classCoverage.getName().equals(TestData.PRIMARY_CLASS_CHAR16_VECTOR))
            .findAny()
            .orElseThrow();
    // From Primary:
    //   - <init>
    //   - preload
    //   - execute
    // From Subsidiary:
    // - Method definitions are copied from subsidiary to primary:
    //   - char16_vector.method1()
    //   - char16_vector.method2()
    //   - char16_vector.method3?()
    //   - char16_vector.method4()
    //   - char16_vector.method1() (#2)
    // - loopbody method are merged into method definition:
    //   - char16_vector.method1()
    //   - char16_vector.method1() (#2)
    assertThat(classCoverage0.getMethods()).hasSize(8);
  }
}
