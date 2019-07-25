package edu.utdallas.davisbase;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Scanner;
import edu.utdallas.davisbase.executor.Executor;
import edu.utdallas.davisbase.executor.ExecutorConfiguration;
import edu.utdallas.davisbase.host.Host;
import edu.utdallas.davisbase.host.HostConfiguration;
import edu.utdallas.davisbase.parser.Parser;
import edu.utdallas.davisbase.storage.Storage;
import edu.utdallas.davisbase.storage.StorageConfiguration;
import edu.utdallas.davisbase.storage.StorageState;

public class DavisBase {

  public static void main(String[] args) {
    DavisBase davisBase = new DavisBase(args);
    int exitCode = davisBase.run();
    System.exit(exitCode);
  }

  private final Host host;
  private final Parser parser;
  private final edu.utdallas.davisbase.compiler.Compiler compiler;
  private final Executor executor;
  private final Storage storage;

  private DavisBase(String[] args) {
    assert args != null;

    final StorageConfiguration storageConfiguration = new StorageConfiguration.Builder().build();
    final StorageState storageState = new StorageState.Builder().build();
    this.storage = new Storage(storageConfiguration, storageState);

    this.executor = new Executor(new ExecutorConfiguration());

    this.compiler = new edu.utdallas.davisbase.compiler.Compiler(this.storage);

    this.parser = new Parser();

    final HostConfiguration hostConfiguration = new HostConfiguration.Builder().build();
    final Scanner scanner = new Scanner(new BufferedReader(new InputStreamReader(System.in)));
    final PrintWriter printer = new PrintWriter(System.out);
    this.host = new Host(hostConfiguration, scanner, printer);
  }

  /**
   * @return exit code
   */
  public int run() {
    // TODO Implement DavisBase.run()
    throw new NotImplementedException();
  }
}
