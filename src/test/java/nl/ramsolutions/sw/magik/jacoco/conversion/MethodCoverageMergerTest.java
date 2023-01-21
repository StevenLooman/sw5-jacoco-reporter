package nl.ramsolutions.sw.magik.jacoco.conversion;

import nl.ramsolutions.sw.magik.jacoco.TestData;
import nl.ramsolutions.sw.magik.jacoco.sw5lib.Sw5LibAnalyzer;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for MethodCoverageMerger.
 */
@SuppressWarnings("checkstyle:MagicNumber")
class MethodCoverageMergerTest {

    @Test
    void testMergeMethods() throws IOException {
        final Sw5LibAnalyzer libAnalyzer = TestData.getLibAnalyzer();
        final IBundleCoverage bundleCoverageOrig = TestData.getBundleCoverage();
        final MagikBundleCoverageConverter converter =
            new MagikBundleCoverageConverter(libAnalyzer, bundleCoverageOrig, false);

        final IBundleCoverage bundleCoverage = converter.convert();

        final IPackageCoverage packageCoverage0 = List.copyOf(bundleCoverage.getPackages()).get(0);
        final IClassCoverage classCoverage0 = packageCoverage0.getClasses().stream()
            .filter(classCoverage -> classCoverage.getName().equals(TestData.PRIMARY_CLASS_CHAR16_VECTOR))
            .findAny()
            .orElseThrow();
        // From Primary: <init>, preload, execute.
        // From Subsidiary:
        // - Method definitions are copied from subsidiary to primary (+4).
        // - __loopbody method is merged into method definition (-1).
        assertThat(classCoverage0.getMethods()).hasSize(6);
    }

}
