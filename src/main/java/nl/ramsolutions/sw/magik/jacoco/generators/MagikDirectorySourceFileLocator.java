package nl.ramsolutions.sw.magik.jacoco.generators;

import org.jacoco.report.InputStreamSourceFileLocator;

import javax.annotation.CheckForNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * {@link InputStreamSourceFileLocator} for Smallworld/Magik.
 */
public class MagikDirectorySourceFileLocator extends InputStreamSourceFileLocator {

    private static final int TAB_WIDTH = 8;
    private static final Charset DEFAULT_ENCODING = StandardCharsets.ISO_8859_1;
    private static final String PACKAGE_MAGIK_PREFIX = "magik/";
    private static final String PRODUCT_DEF = "product.def";

    private final List<Path> productPaths;

    /**
     * Constructor.
     * @param productPaths Directory to Smallworld products.
     * @param tabWidth Width of tab.
     */
    public MagikDirectorySourceFileLocator(final List<Path> productPaths, final int tabWidth) {
        super(null, tabWidth);
        this.productPaths = List.copyOf(productPaths);
    }

    /**
     * Constructor.
     * @param productPaths Directory to Smallworld products.
     */
    public MagikDirectorySourceFileLocator(final List<Path> productPaths) {
        this(productPaths, TAB_WIDTH);
    }

    @Override
    public Reader getSourceFile(final String packageName, final String fileName) throws IOException {
        if (packageName.startsWith(PACKAGE_MAGIK_PREFIX)) {
            final Path path = this.resolvePackagePath(packageName);
            if (path == null) {
                return null;
            }

            final Path filePath = path.resolve(fileName);
            if (!Files.exists(filePath)) {
                return null;
            }

            final String filePathStr = filePath.toString();
            final InputStream in = this.getSourceStream(filePathStr);
            if (in == null) {
                return null;
            }

            return new InputStreamReader(in, DEFAULT_ENCODING);
        }

        return super.getSourceFile(packageName, fileName);
    }

    @CheckForNull
    @Override
    protected InputStream getSourceStream(final String path) throws IOException {
        final File file = new File(path);
        if (!file.isFile()) {
            return null;
        }

        return new FileInputStream(file);
    }

    private Path resolvePackagePath(final String packageName) throws IOException {
        final String[] parts = packageName.split("/");
        final String productName = parts[1];
        for (final Path productPath : this.productPaths) {
            // Find all product.defs under productPath.
            final List<Path> productDefPaths = Files.find(
                productPath,
                Integer.MAX_VALUE,
                (path, attrs) -> {
                    final String filename = path.getFileName().toString();
                    return filename.equalsIgnoreCase(PRODUCT_DEF);
                })
                .collect(Collectors.toList());
            for (final Path path : productDefPaths) {
                if (MagikDirectorySourceFileLocator.definitionFileHasName(path, productName)) {
                    return path.getParent();
                }
            }
        }

        // Nothing found.
        return null;
    }

    private static boolean definitionFileHasName(final Path definitionFilePath, final String name) throws IOException {
        final List<String> lines = Files.readAllLines(definitionFilePath);
        final Optional<String> optDefLine = lines.stream()
            .map(line -> line.trim())
            .filter(line -> !line.startsWith("#") && !line.isBlank())
            .findFirst();
        if (!optDefLine.isPresent()) {
            return false;
        }

        final String line = optDefLine.get();
        try (Scanner scanner = new Scanner(line)) {
            final String definitionName = scanner.next();
            return definitionName.equalsIgnoreCase(name);
        }
    }

}
