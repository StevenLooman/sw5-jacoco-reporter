package nl.ramsolutions.sw.magik.jacoco;

import java.io.PrintStream;
import org.jacoco.core.JaCoCo;

/**
 * Version information about this tool and the JaCoCo library it embeds.
 *
 * <p>The JaCoCo version matters to users: the probes recorded by the JaCoCo agent are only
 * interpreted correctly by the JaCoCo version they were recorded with. Reporting the embedded
 * version makes it possible to pick the matching agent.
 */
public final class VersionInfo {

  private static final String UNKNOWN_VERSION = "unknown";
  private static final int VERSION_PARTS = 3;

  private VersionInfo() {}

  /**
   * Get the version of this tool, as recorded in the manifest.
   *
   * @return Version of this tool, or {@literal unknown} if it cannot be determined.
   */
  public static String getToolVersion() {
    final String version = VersionInfo.class.getPackage().getImplementationVersion();
    return version != null ? version : UNKNOWN_VERSION;
  }

  /**
   * Get the version of the JaCoCo library used by this tool.
   *
   * <p>{@link JaCoCo#VERSION} carries a build qualifier, e.g. {@literal 0.8.14.202510111229}. Only
   * the {@literal 0.8.14} part is reported, as that is how the agent is released.
   *
   * @return Version of the embedded JaCoCo library.
   */
  public static String getJacocoVersion() {
    final String[] parts = JaCoCo.VERSION.split("\\.", VERSION_PARTS + 1);
    return parts.length > VERSION_PARTS
        ? String.join(".", parts[0], parts[1], parts[2])
        : JaCoCo.VERSION;
  }

  /**
   * Get the version of this tool and of the embedded JaCoCo library.
   *
   * @return Version string.
   */
  public static String getVersionString() {
    return "sw5-jacoco-reporter "
        + VersionInfo.getToolVersion()
        + ", using JaCoCo "
        + VersionInfo.getJacocoVersion();
  }

  /**
   * Get the notice telling the user which JaCoCo agent version to record {@literal jacoco.exec}
   * with.
   *
   * @return Notice about the JaCoCo agent version to use.
   */
  public static String getAgentVersionNotice() {
    return "Note: record jacoco.exec with the JaCoCo agent version "
        + VersionInfo.getJacocoVersion()
        + ". Using another version of the agent can silently report covered lines as missed.";
  }

  /**
   * Print the versions in use and the notice about the JaCoCo agent version.
   *
   * @param printStream Stream to print to.
   */
  public static void printTo(final PrintStream printStream) {
    final String versionString = VersionInfo.getVersionString();
    printStream.println(versionString);
    final String agentVersionNotice = VersionInfo.getAgentVersionNotice();
    printStream.println(agentVersionNotice);
  }
}
