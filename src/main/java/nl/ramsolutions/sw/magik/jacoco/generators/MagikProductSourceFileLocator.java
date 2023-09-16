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
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * {@link InputStreamSourceFileLocator} for Smallworld/Magik, or fallback to regular.
 */
public class MagikProductSourceFileLocator extends InputStreamSourceFileLocator {

    private static final int TAB_WIDTH = 8;
    private static final Charset DEFAULT_ENCODING = StandardCharsets.ISO_8859_1;
    private static final String PACKAGE_MAGIK_PREFIX = "magik/";
    private static final String PRODUCT_DEF = "product.def";

    private final Path productPath;
    private List<Path> productDefPaths;

    /**
     * Constructor.
     * @param productPath Directory to Smallworld product.
     */
    public MagikProductSourceFileLocator(final Path productPath) {
        super(null, TAB_WIDTH);
        this.productPath = productPath;
    }

    @CheckForNull
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

        return null;
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
        this.ensureProductDefPaths();

        final String[] parts = packageName.split("/");
        final String productName = parts[1];

        // Find all product.defs under productPath.
        Objects.requireNonNull(this.productDefPaths);
        for (final Path path : this.productDefPaths) {
            if (DefinitionFileReader.definitionFileHasName(path, productName)) {
                final Path parentPath = path.getParent();
                return parentPath;
            }
        }

        // Nothing found.
        return null;
    }

    private void ensureProductDefPaths() throws IOException {
        if (this.productDefPaths != null) {
            return;
        }

        this.productDefPaths = Files.find(
            this.productPath,
            Integer.MAX_VALUE,
            (path, attrs) -> {
                final String filename = path.getFileName().toString();
                return filename.equalsIgnoreCase(PRODUCT_DEF);
            })
            .collect(Collectors.toList());
    }

}
