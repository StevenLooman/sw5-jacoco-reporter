package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import java.nio.file.Path;
import java.util.Objects;

/** Smallworld/Magik code definition. */
public abstract class Sw5LibCodeDefinition {

  private final Path sourceFile;
  private final String javaTypeName;
  private final String javaMethodName;

  /**
   * Constructor.
   *
   * @param sourceFile Path to the code definition.
   * @param javaTypeName Java type name.
   * @param javaMethodName Java method name.
   */
  public Sw5LibCodeDefinition(
      final Path sourceFile, final String javaTypeName, final String javaMethodName) {
    this.sourceFile = sourceFile;
    this.javaTypeName = javaTypeName.replace("/", ".");
    this.javaMethodName = javaMethodName;
  }

  public Path getSourceFile() {
    return this.sourceFile;
  }

  public String getJavaTypeName() {
    return this.javaTypeName;
  }

  public String getJavaMethodName() {
    return this.javaMethodName;
  }

  public String getJavaName() {
    return this.javaTypeName + "." + this.javaMethodName;
  }

  public abstract String getMagikName();

  @Override
  @SuppressWarnings("EqualsGetClass")
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }

    if (this.getClass() != obj.getClass()) {
      return false;
    }

    final Sw5LibCodeDefinition other = (Sw5LibCodeDefinition) obj;
    return Objects.equals(other.sourceFile, this.sourceFile)
        && Objects.equals(other.javaTypeName, this.javaTypeName)
        && Objects.equals(other.javaMethodName, this.javaMethodName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.sourceFile, this.javaTypeName, this.javaMethodName);
  }

  @Override
  public String toString() {
    return "%s@%s(%s, %s, %s)"
        .formatted(
            this.getClass().getName(),
            Integer.toHexString(this.hashCode()),
            this.getSourceFile(),
            this.getJavaName(),
            this.getMagikName());
  }

  public static String keyForClassMethodName(
      final String javaClassName, final String javaMethodName) {
    return javaClassName.replace("/", ".") + "." + javaMethodName;
  }
}
