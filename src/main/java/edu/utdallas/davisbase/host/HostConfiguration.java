package edu.utdallas.davisbase.host;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.StringJoiner;
import com.google.common.base.Strings;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Immutable configuration for a {@link Host}.
 */
public class HostConfiguration {

  private final String lineSeparator;
  private final String prompt;
  private final String copyright;
  private final String version;
  private final String horizontalLine;

  private HostConfiguration(String lineSeparator, String prompt, String copyright, String version, String horizontalLine) {
    assert lineSeparator  != null : "lineSeparator should not be null";
    assert prompt         != null : "prompt should not be null";
    assert copyright      != null : "copyright should not be null";
    assert version        != null : "version should not be null";
    assert horizontalLine != null : "horizontalLine should not be null";

    this.lineSeparator = lineSeparator;
    this.prompt = prompt;
    this.copyright = copyright;
    this.version = version;
    this.horizontalLine = horizontalLine;
  }

  public String getLineSeparator() {
    return lineSeparator;
  }

  /**
   * @return the prompt (not null)
   */
  public String getPrompt() {
    return prompt;
  }

  /**
   * @return the copyright (not null)
   */
  public String getCopyright() {
    return copyright;
  }

  /**
   * @return the version string (not null)
   */
  public String getVersion() {
    return version;
  }

  /**
   * @return the horizontal line to use as a leading and trailing section header for the help message
   */
  public String getHorizontalLine() {
    return horizontalLine;
  }

  public String getWelcome() {
    return new StringJoiner(lineSeparator)
        .add(horizontalLine)
        .add("Welcome to DavisBase")
        .add("DavisBase Version " + version)
        .add(copyright)
        .add("Type \"HELP;\" to display supported commands.")
        .add(horizontalLine)
        .toString();
  }

  public static class Builder {

    private static String getDefaultLineSeparator() {
      return System.lineSeparator();
    }

    private static String getDefaultPrompt() {
      return "davisql> ";
    }

    private static String getDefaultCopyright() {
      return "Team Blue";
    }

    private static String getDefaultVersion() {
      return "v1.0";
    }

    private static String getDefaultHorizontalLine() {
      return Strings.repeat("-", 80);
    }

    private @Nullable String lineSeparator = null;
    private @Nullable String prompt = null;
    private @Nullable String copyright = null;
    private @Nullable String version = null;
    private @Nullable String horizontalLine = null;

    public Builder() {}

    public Builder setLineSeparator(String lineSeparator) {
      checkNotNull(lineSeparator, "lineSeparator");

      this.lineSeparator = lineSeparator;
      return this;
    }

    public Builder setPrompt(String prompt) {
      checkNotNull(prompt, "prompt");

      this.prompt = prompt;
      return this;
    }

    public Builder setCopyright(String copyright) {
      checkNotNull(copyright, "copyright");

      this.copyright = copyright;
      return this;
    }

    public Builder setVersion(String version) {
      checkNotNull(version, "version");

      this.version = version;
      return this;
    }

    public Builder setHorizontalLine(String horizontalLine) {
      checkNotNull(horizontalLine, "horizontalLine");

      this.horizontalLine = horizontalLine;
      return this;
    }

    public HostConfiguration build() {
      final String lineSeparator =  (this.lineSeparator != null)  ? this.lineSeparator  : getDefaultLineSeparator();
      final String prompt =         (this.prompt != null)         ? this.prompt         : getDefaultPrompt();
      final String copyright =      (this.copyright != null)      ? this.copyright      : getDefaultCopyright();
      final String version =        (this.version != null)        ? this.version        : getDefaultVersion();
      final String horizontalLine = (this.horizontalLine != null) ? this.horizontalLine : getDefaultHorizontalLine();

      return new HostConfiguration(lineSeparator, prompt, copyright, version, horizontalLine);
    }

  }

}
