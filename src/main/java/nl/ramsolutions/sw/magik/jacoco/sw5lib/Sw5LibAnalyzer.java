package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Lib analyzer.
 *
 * <p>Extracts exemplar/method definitions from class nodes.
 */
public final class Sw5LibAnalyzer {

  private final Sw5LibReader libReader;
  private Map<ClassNode, ClassNode> classDependencyMap;
  private Map<String, Sw5LibCodeDefinition> methodNameMap;

  /**
   * Constructor.
   *
   * @param libReader Library reader used for analysis.
   */
  public Sw5LibAnalyzer(final Sw5LibReader libReader) {
    this.libReader = libReader;
  }

  public List<Path> getProductPaths() {
    return this.libReader.getProductPaths();
  }

  /**
   * Get a {@link ClassNode} by its name.
   *
   * @param className {@link ClassNode} name.
   * @return {@link ClassNode}, if found.
   */
  @CheckForNull
  public ClassNode getClassByName(final String className) {
    return this.libReader.getClassByName(className);
  }

  public Map<MethodNode, MethodNode> getMethodDependencyMap(
      final ClassNode providerNode, final ClassNode supplierNode) {
    return Sw5LibDependencyBuilder.buildMethodDependencyMap(providerNode, supplierNode);
  }

  /**
   * Get the primary/subsidiary class mapping.
   *
   * @return Mapping keyed on primary class, valued on subsidiary class.
   */
  public Map<ClassNode, ClassNode> getClassDependencyMap() {
    if (this.classDependencyMap == null) {
      final Collection<ClassNode> primaryClassNodes = this.libReader.getPrimaryClassNodes();
      final Collection<ClassNode> subsidiaryClassNodes = this.libReader.getSubsidiaryClassNodes();
      this.classDependencyMap =
          Sw5LibDependencyBuilder.buildClassDependencyMap(primaryClassNodes, subsidiaryClassNodes);
    }

    return Collections.unmodifiableMap(this.classDependencyMap);
  }

  /**
   * Get the Magik method from a Java class/method combination.
   *
   * @param javaClassName Name of Java class.
   * @param javaMethodName Name of Java method.
   * @return Magik method name, if known.
   */
  @CheckForNull
  public Sw5LibCodeDefinition getElement(final String javaClassName, final String javaMethodName) {
    final Map<String, Sw5LibCodeDefinition> methodNameMap = this.getMethodNameMap();
    final String completeJavaName =
        Sw5LibCodeDefinition.keyForClassMethodName(javaClassName, javaMethodName);
    final Sw5LibCodeDefinition element = methodNameMap.get(completeJavaName);
    if (element == null) {
      throw new IllegalStateException("Could not find mapped method, key: " + completeJavaName);
    }

    return element;
  }

  /**
   * Get duplicate method definitions.
   *
   * @return List of duplicate method names and their files.
   */
  public Map<String, List<MethodDefinition>> getDuplicateMethodDefinitions() {
    // Create map keyed on source file, valued on MethodDefinition.
    final Collection<MethodDefinition> methodDefinitions =
        this.getMethodNameMap().entrySet().stream()
            .map(Map.Entry::getValue)
            .filter(MethodDefinition.class::isInstance)
            .map(MethodDefinition.class::cast)
            .toList();
    return Sw5LibDuplicateMethodFinder.findDuplicateMethodDefinitions(methodDefinitions);
  }

  /**
   * Create a mapping from Java class/method names to {@link Sw5LibCodeDefinition}.
   *
   * @return Mapping from Java class/method to {@link Sw5LibCodeDefinition}.
   */
  private Map<String, Sw5LibCodeDefinition> getMethodNameMap() {
    if (this.methodNameMap == null) {
      final Map<String, MethodDefinition> methodMapping =
          this.libReader.getPrimaryClassNodes().stream()
              .map(Sw5LibMethodDefinitionExtractor::extractMethodDefinitions)
              .flatMap(Collection::stream)
              .collect(Collectors.toMap(MethodDefinition::getJavaName, cm -> cm));

      final Map<String, ProcDefinition> procMappingExec =
          this.libReader.getPrimaryClassNodes().stream()
              .map(Sw5LibProcDefinitionExtractor::extractProcDefinitions)
              .flatMap(Collection::stream)
              .collect(Collectors.toMap(ProcDefinition::getJavaName, pb -> pb));
      final Map<String, ProcDefinition> procMappingSub =
          this.libReader.getSubsidiaryClassNodes().stream()
              .map(Sw5LibProcDefinitionExtractor::extractProcDefinitions)
              .flatMap(Collection::stream)
              .collect(Collectors.toMap(ProcDefinition::getJavaName, pb -> pb));

      this.methodNameMap = new HashMap<>();
      this.methodNameMap.putAll(methodMapping);
      this.methodNameMap.putAll(procMappingExec);
      this.methodNameMap.putAll(procMappingSub);
    }

    return this.methodNameMap;
  }
}
