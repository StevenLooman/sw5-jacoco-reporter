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

/**
 * {@link InputStreamSourceFileLocator} for Smallworld/Magik.
 */
public class MagikDirectorySourceFileLocator extends InputStreamSourceFileLocator {

    private static final int TAB_WIDTH = 8;
    private static final Charset DEFAULT_ENCODING = StandardCharsets.ISO_8859_1;
    private static final String PACKAGE_MAGIK_PREFIX = "magik/";

    private final File directory;

    /**
     * Constructor.
     * @param directory Directory to Smallworld product.
     * @param tabWidth Width of tab.
     */
    public MagikDirectorySourceFileLocator(final File directory, final int tabWidth) {
        super(null, tabWidth);
        this.directory = directory;
    }

    /**
     * Constructor.
     * @param directory Directory to Smallworld product.
     */
    public MagikDirectorySourceFileLocator(final File directory) {
        this(directory, TAB_WIDTH);
    }

    @Override
    public Reader getSourceFile(final String packageName, final String fileName) throws IOException {
        if (packageName.startsWith(PACKAGE_MAGIK_PREFIX)) {
            final InputStream in = this.getSourceStream(fileName);
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
        final File file = new File(this.directory, path);

        if (!file.isFile()) {
            return null;
        }

        return new FileInputStream(file);
    }

}
