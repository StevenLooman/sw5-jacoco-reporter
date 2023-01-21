package nl.ramsolutions.sw.magik.jacoco;

import java.nio.file.Path;
import java.util.List;

public final class TestData {

    public static final Path PRODUCT_PATH = Path.of("src/test/resources/fixture_product");
    public static final List<Path> PRODUCT_PATHS = List.of(PRODUCT_PATH);

    public static final String EXECUTABLE_CLASS_CHAR16_VECTOR =
        "magik/fixture_product/fixture_module/char16_vector_36";
    public static final String EXECUTABLE_CLASS_MIXED = "magik/fixture_product/fixture_module/mixed_54";
    public static final String EXECUTABLE_CLASS_PRIMARY = "magik/fixture_product/fixture_module/primary_58";
    public static final String EXECUTABLE_CLASS_NAME_DOES_NOT_EXIST =
        "magik/fixture_product/fixture_module/does_not_exist_99";
    public static final String CLASS_CHAR16_VECTOR = "magik/fixture_product/fixture_module/char16_vector_37";
    public static final String CLASS_MIXED = "magik/fixture_product/fixture_module/mixed_55";
    // public static final String CLASS_PRIMARY = "magik/fixture_product/fixture_module/primary_54";
    
}
