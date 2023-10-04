package nl.ramsolutions.sw.magik.jacoco.conversion;

import nl.ramsolutions.sw.magik.jacoco.TestData;
import nl.ramsolutions.sw.magik.jacoco.sw5lib.Sw5LibAnalyzer;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for MagikBundleCoverageConverter.
 */
@SuppressWarnings("checkstyle:MagicNumber")
class MagikBundleCoverageConverterTest {

    @Test
    void testFixtureBundleCoverage() throws IOException {
        // Ensure valid data.
        final IBundleCoverage bundleCoverage = TestData.getBundleCoverage();
        assertThat(bundleCoverage).isNotNull();
        assertThat(bundleCoverage.getPackages()).hasSize(1);

        final IPackageCoverage packageCoverage0 = List.copyOf(bundleCoverage.getPackages()).get(0);
        assertThat(packageCoverage0).isNotNull();
        assertThat(packageCoverage0.getName()).isEqualTo("magik/fixture_product/fixture_module");
        assertThat(packageCoverage0.getClasses()).hasSize(5);  // 3 primary, 2 subsidiary.

        final IClassCoverage primaryClassCoverage0 = packageCoverage0.getClasses().stream()
            .filter(classCoverage -> classCoverage.getName().equals(TestData.PRIMARY_CLASS_CHAR16_VECTOR))
            .findAny()
            .orElseThrow();
        assertThat(primaryClassCoverage0.getMethods()).hasSize(3);  // <init>, preload, execute.

        final IClassCoverage subsidiaryClassCoverage0 = packageCoverage0.getClasses().stream()
            .filter(classCoverage -> classCoverage.getName().equals(TestData.SUBSIDIARY_CLASS_CHAR16_VECTOR))
            .findAny()
            .orElseThrow();
        assertThat(subsidiaryClassCoverage0.getMethods()).hasSize(4);  // 3 methods + loopbody.

        final List<IMethodCoverage> methodCoverages = List.copyOf(subsidiaryClassCoverage0.getMethods());
        final IMethodCoverage methodCoverage0 = methodCoverages.get(0);
        assertThat(methodCoverage0).isNotNull();
        assertThat(methodCoverage0.getName()).isEqualTo("char16_vector__method1");
    }

    @Test
    void testMergeMethods() throws IOException {
        final Sw5LibAnalyzer libAnalyzer = TestData.getLibAnalyzer();
        final IBundleCoverage bundleCoverageOrig = TestData.getBundleCoverage();
        final MagikBundleCoverageConverter converter =
            new MagikBundleCoverageConverter(libAnalyzer, bundleCoverageOrig, false, false);

        final IBundleCoverage bundleCoverage = converter.convert();

        final IPackageCoverage packageCoverage0 = List.copyOf(bundleCoverage.getPackages()).get(0);
        assertThat(packageCoverage0.getClasses()).hasSize(3);  // Subsidiary are merged into primary: 5 --> 3.

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

    @Test
    void testMergeRemoveExecutable() throws IOException {
        final Sw5LibAnalyzer libAnalyzer = TestData.getLibAnalyzer();
        final IBundleCoverage bundleCoverageOrig = TestData.getBundleCoverage();
        final MagikBundleCoverageConverter converter =
            new MagikBundleCoverageConverter(libAnalyzer, bundleCoverageOrig, true, true);

        final IPackageCoverage packageCoverageOrig0 = List.copyOf(bundleCoverageOrig.getPackages()).get(0);
        assertThat(packageCoverageOrig0.getClasses()).hasSize(5);  // 4 classes.

        final IBundleCoverage bundleCoverage = converter.convert();
        final IPackageCoverage packageCoverage0 = List.copyOf(bundleCoverage.getPackages()).get(0);
        assertThat(packageCoverage0.getClasses()).hasSize(3);  // 3 classes, classes are merged.
    }

}
