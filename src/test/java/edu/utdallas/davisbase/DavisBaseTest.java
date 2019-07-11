package edu.utdallas.davisbase;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DavisBaseTest {

  @Test
  public void shouldExitWithCodeZero() {
    DavisBase davisBase = DavisBase.startUp(new String[] {});
    int exitCode = davisBase.run();
    assertEquals(0, exitCode);
  }
}
