package edu.utdallas.davisbase;

import static java.lang.String.format;

/**
 * Utilities for working with DavisBase {@code rowId}s.
 */
public class RowIdUtils {

  public static final byte ROWID_COLUMN_INDEX = 0;

  /**
   * The logical maximum value (inclusive) of a DavisBase
   * {@link edu.utdallas.davibase.catalog.DavisBaseColumnsTableColumn#ROWID ROWID}.
   *
   * The logical {@code ROWID_MAX_VALUE} is one less than its Java type's physical max value because
   * {@code ROWID} is effectively a zero-based element index, and so the physical max value must be
   * reserved to represent the one-based count of valid {@code ROWID}s.
   */
  public static final int ROWID_MAX_VALUE = Integer.MAX_VALUE - 1;
  public static final int ROWID_MIN_VALUE = 0;
  public static final int ROWID_DEFAULT_VALUE = 0;
  public static final int ROWID_NULL_VALUE = -1;

  private RowIdUtils() {
    throw new IllegalStateException(
        format("%s may not be instantiated.",
            RowIdUtils.class.getName()));
  }

}
