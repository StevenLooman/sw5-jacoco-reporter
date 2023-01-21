package nl.ramsolutions.sw.magik.jacoco;

import nl.ramsolutions.sw.magik.jacoco.sw5lib.Sw5LibAnalyzer;
import nl.ramsolutions.sw.magik.jacoco.sw5lib.Sw5LibReader;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.tools.ExecFileLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Test data definitions.
 */
public final class TestData {

    public static final Path PRODUCT_PATH = Path.of("src/test/resources/fixture_product");
    public static final List<Path> PRODUCT_PATHS = List.of(PRODUCT_PATH);

    public static final String CLASS_DOES_NOT_EXIST = "magik/fixture_product/fixture_module/does_not_exist_99";

    public static final String PRIMARY_CLASS_CHAR16_VECTOR = "magik/fixture_product/fixture_module/char16_vector_39";
    public static final String PRIMARY_CLASS_MIXED = "magik/fixture_product/fixture_module/mixed_63";
    public static final String PRIMARY_CLASS_PRIMARY = "magik/fixture_product/fixture_module/primary_67";

    public static final String SUBSIDIARY_CLASS_CHAR16_VECTOR = "magik/fixture_product/fixture_module/char16_vector_40";
    public static final String SUBSIDIARY_CLASS_MIXED = "magik/fixture_product/fixture_module/mixed_64";

    private TestData() {
    }

    /**
     * Get Sw5LibReader for fixture data.
     * @return
     * @throws IOException
     */
    public static Sw5LibReader getLibReader() throws IOException {
        return new Sw5LibReader(TestData.PRODUCT_PATHS);
    }

    /**
     * Get Sw5LibAnalyzer for fixture data.
     * @return
     * @throws IOException
     */
    public static Sw5LibAnalyzer getLibAnalyzer() throws IOException {
        final Sw5LibReader libReader = TestData.getLibReader();
        return new Sw5LibAnalyzer(libReader);
    }

    /**
     * Get {@link IBundleCoverage} fixture data.
     * @return {@link IBundleCoverage}
     * @throws IOException -
     */
    public static IBundleCoverage getBundleCoverage() throws IOException {
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

}
