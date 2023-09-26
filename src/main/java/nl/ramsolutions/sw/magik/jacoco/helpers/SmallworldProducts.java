package nl.ramsolutions.sw.magik.jacoco.helpers;

import javax.annotation.CheckForNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SmallworldProducts {

    private final List<SmallworldProduct> products;

    public SmallworldProducts(final List<Path> productPaths) {
        this.products = SmallworldProducts.buildProducts(productPaths);
    }

    private static List<SmallworldProduct> buildProducts(final List<Path> productPaths) {
        return productPaths.stream()
            .map(productPath -> {
                try {
                    return SmallworldProducts.findProducts(productPath);
                } catch (final IOException exception) {
                    throw new IllegalStateException(exception);
                }
            })
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    private static List<SmallworldProduct> findProducts(final Path productPath) throws IOException {
        final BiPredicate<Path, BasicFileAttributes> pred = (path, attrs) -> {
            final String filename = path.getFileName().toString();
            return filename.equalsIgnoreCase(SmallworldProduct.PRODUCT_DEF);
        };
        try (Stream<Path> findStream = Files.find(productPath, Integer.MAX_VALUE, pred)) {
            return findStream
                .map(Path::getParent)
                .map(SmallworldProduct::new)
                .collect(Collectors.toList());
        }
    }

    @CheckForNull
    public Path getSourcePath(final String packageName, final String fileName) throws IOException {
        final Optional<SmallworldProduct> optionalProduct = this.products.stream()
            .filter(prod -> {
                try {
                    return prod.containsPackage(packageName);
                } catch (final IOException exception) {
                    throw new IllegalStateException(exception);
                }
            })
            .findFirst();
        if (!optionalProduct.isPresent()) {
            return null;
        }

        final SmallworldProduct product = optionalProduct.get();
        return product.getSourcePath(packageName, fileName);
    }

}
