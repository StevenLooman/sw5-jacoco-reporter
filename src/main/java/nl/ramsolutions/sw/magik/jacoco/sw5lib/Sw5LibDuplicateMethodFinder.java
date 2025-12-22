package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Smallworld/Magik duplicate method finder. */
final class Sw5LibDuplicateMethodFinder {

  private Sw5LibDuplicateMethodFinder() {}

  /**
   * Find duplicate method definitions.
   *
   * @param methodDefinitions Collection of {@link MethodDefinition}s to check.
   * @return List of duplicate method definition strings.
   */
  public static Map<String, List<MethodDefinition>> findDuplicateMethodDefinitions(
      final Collection<MethodDefinition> methodDefinitions) {
    return methodDefinitions.stream()
        .map(methodDefinition -> Map.entry(methodDefinition.getMagikName(), methodDefinition))
        .collect(
            Collectors.groupingBy(
                Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())))
        .entrySet()
        .stream()
        .filter(entry -> entry.getValue().size() > 1)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    // .map(
    //     entry -> {
    //       final String methodName = entry.getKey();
    //       final String files = entry.getValue().stream().collect(Collectors.joining(", "));
    //       return String.format("%s defined in files: %s", methodName, files);
    //     })
    // .toList();
  }
}
