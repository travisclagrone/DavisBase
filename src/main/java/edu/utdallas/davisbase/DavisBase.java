package edu.utdallas.davisbase;

public class DavisBase {

  public static void main(String[] args) {
    DavisBase davisBase = DavisBase.startUp(args);
    int exitCode = davisBase.run();
    System.exit(exitCode);
  }

  public static DavisBase startUp(String[] args) {
    return new DavisBase();
  }

  private DavisBase() {}

  /**
   * @return exit code
   */
  public int run() {
    // TODO Implement DavisBase.run()
    throw new NotImplementedException();
  }
}
