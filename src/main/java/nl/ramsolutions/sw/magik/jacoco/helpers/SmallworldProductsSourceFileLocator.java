package nl.ramsolutions.sw.magik.jacoco.helpers;

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
import java.nio.file.Path;

/**
 * {@link InputStreamSourceFileLocator} for Smallworld/Magik products.
 */
public class SmallworldProductsSourceFileLocator extends InputStreamSourceFileLocator {

    private static final int TAB_WIDTH = 8;
    private static final Charset DEFAULT_ENCODING = StandardCharsets.ISO_8859_1;
    private static final String PACKAGE_MAGIK_PREFIX = "magik/";

    private final SmallworldProducts smallworldProducts;

    /**
     * Constructor.
     * @param productPath Directory to Smallworld product.
     */
    public SmallworldProductsSourceFileLocator(final SmallworldProducts smallworldProducts) {
        super(DEFAULT_ENCODING.name(), TAB_WIDTH);
        this.smallworldProducts = smallworldProducts;
    }

    @CheckForNull
    @Override
    public Reader getSourceFile(final String packageName, final String fileName) throws IOException {
        if (packageName.startsWith(PACKAGE_MAGIK_PREFIX)) {
            final Path filePath = this.smallworldProducts.getSourcePath(packageName, fileName);
            if (filePath == null) {
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

}
