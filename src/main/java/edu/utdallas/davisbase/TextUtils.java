package edu.utdallas.davisbase;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.US_ASCII;

import java.nio.charset.Charset;

/**
 * Utilities and compile-time constants for working with instances of the Java class associated with
 * the DavisBase {@link edu.utdallas.davisbase.DataType#TEXT TEXT}
 * {@link edu.utdallas.davisbase.DataType DataType}.
 *
 * @see edu.utdallas.davisbase.DataType#getJavaClass()
 */
public class TextUtils {

  public static final Charset TEXT_CHARSET = US_ASCII;

  private TextUtils() {
    throw new IllegalStateException(
        format("%s may not be instantiated.",
            TextUtils.class.getName()));
  }

}
