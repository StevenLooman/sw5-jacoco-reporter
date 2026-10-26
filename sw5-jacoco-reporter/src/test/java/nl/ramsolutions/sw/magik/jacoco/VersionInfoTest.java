package nl.ramsolutions.sw.magik.jacoco;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Tests for {@link VersionInfo}. */
class VersionInfoTest {

  @Test
  void testGetJacocoVersionIsQualifierLess() {
    // E.g. 0.8.14, from JaCoCo.VERSION 0.8.14.202510111229.
    assertThat(VersionInfo.getJacocoVersion()).matches("\\d+\\.\\d+\\.\\d+");
    assertThat(org.jacoco.core.JaCoCo.VERSION).startsWith(VersionInfo.getJacocoVersion());
  }

  @Test
  void testGetToolVersion() {
    // No manifest when running from the test classpath, so the fallback is expected.
    assertThat(VersionInfo.getToolVersion()).isNotBlank();
  }

  @Test
  void testGetVersionString() {
    assertThat(VersionInfo.getVersionString())
        .contains(VersionInfo.getToolVersion())
        .contains(VersionInfo.getJacocoVersion());
  }

  @Test
  void testGetAgentVersionNotice() {
    assertThat(VersionInfo.getAgentVersionNotice())
        .contains(VersionInfo.getJacocoVersion())
        .containsIgnoringCase("agent");
  }
}
