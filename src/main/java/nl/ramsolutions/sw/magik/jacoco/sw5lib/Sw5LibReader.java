package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.annotation.CheckForNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

/**
 * Magik lib reader.
 */
public class Sw5LibReader {

    private static final String DIRECTORY_LIBS = "libs";
    private static final String ANNOTATION_CODE_TYPE = "Lcom/gesmallworld/magik/commons/runtime/annotations/CodeType;";
    private static final String ANNOTATION_CODE_TYPE_SUBSIDIARY = "Subsidiary";
    private static final String INTERFACE_EXECUTABLE_MAGIK = "com/gesmallworld/magik/language/utils/ExecutableMagik";

    private final Map<String, ClassNode> namedClasses = new HashMap<>();

    /**
     * Constructor.
     * @param productDir Product directory.
     */
    public Sw5LibReader(final Path productDir) throws IOException {
        this.readProductLibs(productDir);
    }

    /**
     * Get executable Magik classes, i.e., source files without method definitions.
     * @return Primary {@link ClassNode}s.
     */
    public Collection<ClassNode> getExecutableClassNodes() {
        return this.namedClasses.values().stream()
            .filter(classNode -> classNode.interfaces.contains(INTERFACE_EXECUTABLE_MAGIK))
            .collect(Collectors.toList());
    }

    /**
     * Get all Subsidiary {@link ClassNode}s.
     * @return Subsidiary {@link ClassNode}s.
     */
    public Collection<ClassNode> getSubsidiaryClassNodes() {
        return this.namedClasses.values().stream()
            .filter(classNode -> classNode.visibleAnnotations.stream()
                .anyMatch(annotation ->
                    annotation.desc.equals(ANNOTATION_CODE_TYPE)
                    && annotation.values.get(1).equals(ANNOTATION_CODE_TYPE_SUBSIDIARY)))
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

    private void readProductLibs(final Path productDir) throws IOException {
        final Path libsDir = productDir.resolve(DIRECTORY_LIBS);
        Stream<Path> libPaths = Files.find(
            libsDir,
            Integer.MAX_VALUE,
            (path, basicFileAttributes) -> path.getFileName().toString().toLowerCase().endsWith(".jar"));
        libPaths.forEach(this::readNamedClassesSafe);
        libPaths.close();
    }

    private void readNamedClassesSafe(final Path archive) {
        try {
            this.readNamedClasses(archive);
        } catch (IOException exception) {
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
                final byte[] bytecode = new byte[size];
                try (InputStream inputStream = zipFile.getInputStream(entry)) {
                    // Get bytecode.
                    final int read = inputStream.read(bytecode);
                    if (read != size) {
                        throw new IOException("Did not read all");
                    }

                    // Read class.
                    final ClassReader classReader = new ClassReader(bytecode);
                    final ClassNode classNode = new ClassNode(Opcodes.ASM8);
                    classReader.accept(classNode, 0);

                    // Store it.
                    this.namedClasses.put(name, classNode);
                }
            }
        }
    }

}
