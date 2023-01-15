package nl.ramsolutions.sw.magik.jacoco.generators;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

final class DefinitionFileReader {

    private DefinitionFileReader() {
    }

    static boolean definitionFileHasName(final Path definitionFilePath, final String name) throws IOException {
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
