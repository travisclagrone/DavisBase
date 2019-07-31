package edu.utdallas.davisbase;

import static java.lang.String.format;

/**
 * Utilities for working with DavisBase {@code rowId}s.
 */
public class RowIdUtils {

  private RowIdUtils() {
    throw new IllegalStateException(format("%s may not be instantiated.", RowIdUtils.class.getName()));
  }

  public static final byte ROWID_COLUMN_INDEX = 0;

}
