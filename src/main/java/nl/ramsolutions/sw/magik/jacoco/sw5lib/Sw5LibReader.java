package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import nl.ramsolutions.sw.magik.jacoco.helpers.ClassNodeHelper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.CheckForNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Magik lib reader.
 */
public class Sw5LibReader {

    private static final String DIRECTORY_LIBS = "libs";

    private final Map<String, ClassNode> namedClasses = new HashMap<>();
    private final List<Path> productPaths;

    /**
     * Constructor.
     * @param productPaths Product directories.
     */
    public Sw5LibReader(final List<Path> productPaths) throws IOException {
        this.productPaths = List.copyOf(productPaths);
        this.readProductLibs();
    }

    public List<Path> getProductPaths() {
        return Collections.unmodifiableList(this.productPaths);
    }

    /**
     * Get primary Magik classes, i.e., source files without method definitions.
     * @return Primary {@link ClassNode}s.
     */
    public Collection<ClassNode> getPrimaryClassNodes() {
        return this.namedClasses.values().stream()
            .filter(ClassNodeHelper::isPrimaryClassNode)
            .collect(Collectors.toSet());
    }

    /**
     * Get all Subsidiary {@link ClassNode}s.
     * @return Subsidiary {@link ClassNode}s.
     */
    public Collection<ClassNode> getSubsidiaryClassNodes() {
        return this.namedClasses.values().stream()
            .filter(ClassNodeHelper::isSubsidiaryClassNode)
            .collect(Collectors.toSet());
    }

    /**
     * Get a {@link ClassNode} by its name.
     * @param className {@link ClassNode} name.
     * @return {@link ClassNode}, if found.
     */
    @CheckForNull
    public ClassNode getClassByName(final String className) {
        return this.namedClasses.get(className);
    }

    private void readProductLibs() throws IOException {
        final BiPredicate<Path, BasicFileAttributes> pred = (path, basicFileAttributes) -> {
            final String filename = path.getFileName().toString();
            return filename.toLowerCase().endsWith(".jar");
        };
        for (final Path productDir : this.productPaths) {
            final Path libsDir = productDir.resolve(DIRECTORY_LIBS);
            final Stream<Path> libPaths = Files.find(libsDir, Integer.MAX_VALUE, pred);
            libPaths.forEach(this::readNamedClassesSafe);
            libPaths.close();
        }
    }

    private void readNamedClassesSafe(final Path archive) {
        try {
            this.readNamedClasses(archive);
        } catch (final IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private void readNamedClasses(final Path archive) throws IOException {
        final File file = archive.toFile();
        try (ZipFile zipFile = new ZipFile(file)) {
            final Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                final String name = entry.getName();
                if (!name.startsWith("magik/")
                    && !name.endsWith(".class")) {
                    continue;
                }

                final int size = (int) entry.getSize();
                final byte[] buffer = new byte[size];
                try (InputStream inputStream = zipFile.getInputStream(entry)) {
                    // Get bytecode.
                    int start = 0;
                    int read = 0;
                    while (size - start != 0) {
                        final int left = size - start;
                        read = inputStream.read(buffer, start, left);
                        start += read;
                    }

                    // Read class.
                    final ClassReader classReader = new ClassReader(buffer);
                    final ClassNode classNode = new ClassNode(Opcodes.ASM8);
                    classReader.accept(classNode, 0);

                    // Store it.
                    this.namedClasses.put(name, classNode);
                }
            }
        }
    }

}
