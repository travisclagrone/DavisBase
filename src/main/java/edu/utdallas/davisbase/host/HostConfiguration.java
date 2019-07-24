package edu.utdallas.davisbase.host;

import java.io.PrintWriter;
import static com.google.common.base.Preconditions.checkNotNull;
import java.util.Collections;

/**
 * Immutable configuration for a {@link Host}.
 */
public class HostConfiguration {

  protected final PrintWriter writer;

  public HostConfiguration(PrintWriter writer){
    checkNotNull(writer);

    this.writer = writer;
  }

  String prompt = "davisql> ";
	String copyright = "Team Blue";
  String version = "v1.0";

  String str = String.join("", (CharSequence) Collections.nCopies(80, '-'));

  public void PromptScreen() {
		writer.println(str);
    writer.println("Welcome to DavisBase");
		writer.println("DavisBase Version " + version);
		writer.println(copyright);
		writer.println("\nType \"help;\" to display supported commands.");
		writer.println(str);
  }

}
