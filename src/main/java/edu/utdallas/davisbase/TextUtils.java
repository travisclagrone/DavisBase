package edu.utdallas.davisbase;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.US_ASCII;

import java.nio.charset.Charset;

/**
 * Utilities and compile-time constants for working with instances of the Java class associated with
 * the DavisBase {@link edu.utdallas.davisbase.DataType#TEXT TEXT}
 * {@link edu.utdallas.davisbase.DataType DataType}.
 * <p>
 * This class is intended to host global, component-agnostic utilities. As such, utilities for
 * {@code String} parsing/compilation and {@code byte[]} serialization/deserialization should not be
 * defined here, but rather in a separate package-private utility class located in the appropriate
 * package.
 *
 * @see edu.utdallas.davisbase.DataType#getJavaClass()
 */
public class TextUtils {

  public static final Charset TEXT_CHARSET = US_ASCII;

  /**
   * The maximum valid length of the byte array representation of the serialized data of one
   * {@link edu.utdallas.davisbase.DataType#TEXT TEXT}.
   *
   * @implNote The maximum binary length is {@code Byte.MAX_VALUE} instead of the lesser
   *           {@code 0x0C - Byte.MAX_VALUE} as given in the original project instructions because
   *           our current implementation of the {@code edu.utdallas.davisbase.storage} package (as
   *           of 2019-08-01) does not implement the serial type codes as instructed, but rather
   *           persists the binary length of the data value in its stead. Thus, the base serial code
   *           for the {@link edu.utdallas.davisbase.DataType#TEXT TEXT}
   *           {@link edu.utdallas.davisbase.DataType DataType} does not reduce the range of values
   *           that may be used to persist a {@code TEXT} data value's binary length.
   * @author Travis C. LaGrone
   * @since 2019-08-01
   */
  public static final int TEXT_MAX_BINARY_LENGTH = Byte.MAX_VALUE;

  /**
   * The minimum valid length of the byte array representation of the serialized data of one
   * {@link edu.utdallas.davisbase.DataType#TEXT TEXT}.
   *
   * @implNote The minimum binary length is {@code 1} instead of {@code 0} because our current
   *           implementation of the {@code edu.utdallas.davisbase.storage} package (as of
   *           2019-08-01) interprets an empty Java {@code String} as a null DavisBase data value
   *           rather than empty-but-not-null {@link edu.utdallas.davisbase.DataType#TEXT TEXT}
   *           data.
   * @author Travis C. LaGrone
   * @since 2019-08-01
   */
  public static final int TEXT_MIN_BINARY_LENGTH = 1;  // May not be empty. Otherwise, it will be interpreted as null in our current implementation.

  private TextUtils() {
    throw new IllegalStateException(
        format("%s may not be instantiated.",
            TextUtils.class.getName()));
  }

}
