package nl.ramsolutions.sw.magik.jacoco.helpers;

import javax.annotation.CheckForNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Smallworld product.
 */
class SmallworldProduct {

    public static final String PRODUCT_DEF = "product.def";
    private static final Path PRODUCT_DEF_PATH = Path.of(PRODUCT_DEF);
    private static final String PACKAGE_MAGIK_PREFIX = "magik";

    private final Path productPath;

    SmallworldProduct(final Path productPath) {
        this.productPath = productPath;
    }

    public Path getProductPath() {
        return this.productPath;
    }

    public String getProductName() throws IOException {
        final Path definitionFilePath = this.productPath.resolve(PRODUCT_DEF_PATH);
        final List<String> lines = Files.readAllLines(definitionFilePath);
        final Optional<String> optDefLine = lines.stream()
            .map(String::trim)
            .filter(line -> !line.startsWith("#") && !line.isBlank())
            .findFirst();
        if (!optDefLine.isPresent()) {
            final String message = "Could not find product name in " + definitionFilePath;
            throw new IllegalStateException(message);
        }

        final String line = optDefLine.get();
        try (Scanner scanner = new Scanner(line)) {
            return scanner.next();
        }
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    public boolean containsPackage(final String packageName) throws IOException {
        final String[] parts = packageName.split("/");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid package name");
        }

        final String magik = parts[0];
        final String product = parts[1];
        // final String moduleName = parts[2];

        if (!magik.equals(PACKAGE_MAGIK_PREFIX)) {
            throw new IllegalArgumentException("Invalid package name");
        }

        final String productName = this.getProductName();
        return product.equalsIgnoreCase(productName);
    }

    @CheckForNull
    public Path getSourcePath(final String packageName, final String fileName) throws IOException {
        if (!this.containsPackage(packageName)) {
            throw new IllegalStateException("Invalid product for package");
        }

        final Path path = this.productPath.resolve(fileName);
        if (!Files.isRegularFile(path)) {
            return null;
        }

        return path;
    }

}
