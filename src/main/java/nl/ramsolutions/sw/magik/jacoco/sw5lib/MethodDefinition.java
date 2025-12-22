package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Objects;

/**
 * Representation of a `com/gesmallworld/magik/language/utils/MagikObjectUtils.createMethod()` call.
 */
public class MethodDefinition extends Sw5LibCodeDefinition {

  public enum Flag {
    ABSTRACT,
    PRIVATE,
    ITERATOR
  }

  private final String magikExemplar;
  private final String magikMethodName;
  private final EnumSet<Flag> flags;

  public MethodDefinition(
      final Path sourceFile,
      final String javaTypeName,
      final String javaMethodName,
      final String magikExemplar,
      final String magikMethodName,
      final EnumSet<Flag> flags) {
    super(sourceFile, javaTypeName, javaMethodName);
    this.magikExemplar = magikExemplar;
    this.magikMethodName = magikMethodName;
    this.flags = EnumSet.copyOf(flags);
  }

  public String getMagikExemplar() {
    return this.magikExemplar;
  }

  public String getMagikMethodName() {
    return this.magikMethodName;
  }

  @Override
  public String getMagikName() {
    return this.getMagikExemplarMethodName();
  }

  public String getMagikExemplarMethodName() {
    if (this.magikMethodName.startsWith("[")) {
      return String.format("%s%s", magikExemplar, magikMethodName);
    }

    return String.format("%s.%s", magikExemplar, magikMethodName);
  }

  public EnumSet<Flag> getFlags() {
    return this.flags;
  }

  public boolean isFlagSet(final Flag flag) {
    return this.flags.contains(flag);
  }

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

    final MethodDefinition other = (MethodDefinition) obj;
    return super.equals(obj)
        && Objects.equals(other.magikExemplar, this.magikExemplar)
        && Objects.equals(other.magikMethodName, this.magikMethodName)
        && Objects.equals(other.flags, this.flags);
  }

  @Override
  public int hashCode() {
    return super.hashCode() + Objects.hash(this.magikExemplar, this.magikMethodName, this.flags);
  }

  @Override
  public String toString() {
    return "%s@%s(%s, %s, %s, %s)"
        .formatted(
            this.getClass().getName(),
            Integer.toHexString(this.hashCode()),
            this.getSourceFile(),
            this.getJavaName(),
            this.getMagikName(),
            this.getFlags());
  }
}
