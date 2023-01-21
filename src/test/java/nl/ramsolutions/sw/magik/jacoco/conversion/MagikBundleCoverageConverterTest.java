package nl.ramsolutions.sw.magik.jacoco.conversion;

import nl.ramsolutions.sw.magik.jacoco.TestData;
import nl.ramsolutions.sw.magik.jacoco.sw5lib.Sw5LibReader;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.tools.ExecFileLoader;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for MagikBundleCoverageConverter.
 */
@SuppressWarnings("checkstyle:MagicNumber")
class MagikBundleCoverageConverterTest {

    static Sw5LibReader getLibReader() throws IOException {
        return new Sw5LibReader(TestData.PRODUCT_PATHS);
    }

    /**
     * Get {@link IBundleCoverage}.
     * @return {@link IBundleCoverage}
     * @throws IOException -
     */
    static IBundleCoverage getBundleCoverage() throws IOException {
        final ExecFileLoader execFileLoader = new ExecFileLoader();
        final Path execFile = TestData.PRODUCT_PATH.resolve("jacoco.exec");
        execFileLoader.load(execFile.toFile());

        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final ExecutionDataStore dataStore = execFileLoader.getExecutionDataStore();
        final Analyzer analyzer = new Analyzer(dataStore, coverageBuilder);
        final File libsDirectory = new File(TestData.PRODUCT_PATH.toFile(), "libs");
        analyzer.analyzeAll(libsDirectory);
        return coverageBuilder.getBundle("Title");
    }

    @Test
    void testFixtureBundleCoverage() throws IOException {
        // Ensure valid data.
        final IBundleCoverage bundleCoverage = MagikBundleCoverageConverterTest.getBundleCoverage();
        assertThat(bundleCoverage).isNotNull();
        assertThat(bundleCoverage.getPackages()).hasSize(1);

        final IPackageCoverage packageCoverage0 = List.copyOf(bundleCoverage.getPackages()).get(0);
        assertThat(packageCoverage0).isNotNull();
        assertThat(packageCoverage0.getName()).isEqualTo("magik/fixture_product/fixture_module");
        assertThat(packageCoverage0.getClasses()).hasSize(4);

        final IClassCoverage classCoverage0 = packageCoverage0.getClasses().stream()
            .filter(classCoverage -> classCoverage.getName().equals(TestData.CLASS_CHAR16_VECTOR))
            .findAny()
            .orElseThrow();
        assertThat(classCoverage0).isNotNull();
        assertThat(classCoverage0.getName()).isEqualTo(TestData.CLASS_CHAR16_VECTOR);
        assertThat(classCoverage0.getMethods()).hasSize(4);  // 3 methods + loopbody

        final List<IMethodCoverage> methodCoverages = List.copyOf(classCoverage0.getMethods());
        final IMethodCoverage methodCoverage0 = methodCoverages.get(0);
        assertThat(methodCoverage0).isNotNull();
        assertThat(methodCoverage0.getName()).isEqualTo("char16_vector__method1");
    }

    @Test
    void testMergeMethods() throws IOException {
        final Sw5LibReader libReader = MagikBundleCoverageConverterTest.getLibReader();
        final IBundleCoverage bundleCoverageOrig = MagikBundleCoverageConverterTest.getBundleCoverage();
        final MagikBundleCoverageConverter converter =
            new MagikBundleCoverageConverter(libReader, bundleCoverageOrig, false);

        final IBundleCoverage bundleCoverage = converter.convert();

        final IPackageCoverage packageCoverage0 = List.copyOf(bundleCoverage.getPackages()).get(0);
        assertThat(packageCoverage0.getClasses()).hasSize(4);  // 4 classes, nothing merged.

        final IClassCoverage classCoverage0 = packageCoverage0.getClasses().stream()
            .filter(classCoverage -> classCoverage.getName().equals(TestData.CLASS_CHAR16_VECTOR))
            .findAny()
            .orElseThrow();
        assertThat(classCoverage0.getMethods()).hasSize(3);  // __loopbody method is merged.
    }

    @Test
    void testMergeRemoveExecutable() throws IOException {
        final Sw5LibReader libReader = MagikBundleCoverageConverterTest.getLibReader();
        final IBundleCoverage bundleCoverageOrig = MagikBundleCoverageConverterTest.getBundleCoverage();
        final MagikBundleCoverageConverter converter =
            new MagikBundleCoverageConverter(libReader, bundleCoverageOrig, true);

        final IPackageCoverage packageCoverageOrig0 = List.copyOf(bundleCoverageOrig.getPackages()).get(0);
        assertThat(packageCoverageOrig0.getClasses()).hasSize(4);  // 4 classes.

        final IBundleCoverage bundleCoverage = converter.convert();
        final IPackageCoverage packageCoverage0 = List.copyOf(bundleCoverage.getPackages()).get(0);
        assertThat(packageCoverage0.getClasses()).hasSize(2);  // 2 classes, executable classes are removed.
    }

}
