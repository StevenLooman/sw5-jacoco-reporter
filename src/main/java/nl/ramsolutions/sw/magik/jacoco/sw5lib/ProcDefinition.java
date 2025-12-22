package nl.ramsolutions.sw.magik.jacoco.sw5lib;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.nio.file.Path;
import java.util.Objects;

/** Representation of a `com/gesmallworld/magik/language/invokers/ConstantBuilder.proc()` call. */
public class ProcDefinition extends Sw5LibCodeDefinition {

  private static final String ANONYMOUS_PROC = "__anonymous_proc__";

  private final String procName;

  public ProcDefinition(
      final Path path,
      final String javaTypeName,
      final String javaMethodName,
      final @Nullable String procName) {
    super(path, javaTypeName, javaMethodName);
    this.procName = procName.isBlank() ? ANONYMOUS_PROC : procName;
  }

  public String getProcName() {
    return this.procName;
  }

  @Override
  public String getMagikName() {
    return "@" + this.procName;
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

    final ProcDefinition other = (ProcDefinition) obj;
    return super.equals(obj) && Objects.equals(other.procName, this.procName);
  }

  @Override
  public int hashCode() {
    return super.hashCode() + Objects.hash(this.procName);
  }

  @Override
  public String toString() {
    return "%s@%s(%s, %s)"
        .formatted(
            this.getClass().getName(),
            Integer.toHexString(this.hashCode()),
            this.getJavaName(),
            this.getMagikName());
  }
}
