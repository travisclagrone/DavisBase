package edu.utdallas.davisbase;

import static java.lang.String.format;

/**
 * Static utilities for the DavisBase {@link DataType#YEAR YEAR} data type.
 */
public class YearUtils {

  private YearUtils() {
    throw new IllegalStateException(format("%s may not be instantiated.", YearUtils.class.getName()));
  }

  public static final int YEAR_OFFSET = 2000;

}
