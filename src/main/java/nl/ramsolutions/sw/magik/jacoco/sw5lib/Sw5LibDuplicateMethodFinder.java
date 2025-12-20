package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Smallworld/Magik duplicate method finder. */
public class Sw5LibDuplicateMethodFinder {

  private static final String ANONYMOUS_PROC = "@__anonymous_proc__";

  private Sw5LibDuplicateMethodFinder() {}

  /**
   * Find duplicate method definitions and the files the methods are defined in.
   *
   * @param fileMethodNameMap Mapping of file to method names.
   * @return List of duplicate method definition strings.
   */
  public static List<String> findDuplicateMethodDefinitions(
      final Map<String, List<String>> fileMethodNameMap) {
    return fileMethodNameMap.entrySet().stream()
        .flatMap(
            entry -> {
              final String fileName = entry.getKey();
              final List<String> methodNames =
                  entry.getValue().stream()
                      .filter(name -> !name.endsWith(ANONYMOUS_PROC))
                      .collect(Collectors.toList());
              return methodNames.stream().map(methodName -> Map.entry(methodName, fileName));
            })
        .collect(
            Collectors.groupingBy(
                Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())))
        .entrySet()
        .stream()
        .filter(entry -> entry.getValue().size() > 1)
        .map(
            entry -> {
              final String methodName = entry.getKey();
              final String files = entry.getValue().stream().collect(Collectors.joining(", "));
              return String.format("%s defined in files: %s", methodName, files);
            })
        .collect(Collectors.toList());
  }
}
