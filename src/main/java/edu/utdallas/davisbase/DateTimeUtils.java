package edu.utdallas.davisbase;

import static java.lang.String.format;

import java.time.ZoneOffset;

/**
 * Utilities and compile-time constants for working with instances of the Java class associated with
 * the DavisBase {@link edu.utdallas.davisbase.DataType#DATETIME DATETIME}
 * {@link edu.utdallas.davisbase.DataType DataType}.
 *
 * @see edu.utdallas.davisbase.DataType#getJavaClass()
 */
public class DateTimeUtils {

  public static final ZoneOffset DATETIME_ZONE_OFFSET = ZoneOffset.UTC;

  private DateTimeUtils() {
    throw new IllegalStateException(
        format("%s may not be instantiated.",
            DateTimeUtils.class.getName()));
  }

}
