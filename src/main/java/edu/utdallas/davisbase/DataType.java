package edu.utdallas.davisbase;

import static com.google.common.base.Preconditions.checkNotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;

public enum DataType {
  TINYINT  (Byte.class),
  SMALLINT (Short.class),
  INT      (Integer.class),
  BIGINT   (Long.class),
  FLOAT    (Float.class),
  DOUBLE   (Double.class),
  YEAR     (Year.class),
  TIME     (LocalTime.class),
  DATETIME (LocalDateTime.class),
  DATE     (LocalDate.class),
  TEXT     (String.class);

  private final Class<?> javaClass;

  /**
   * @param javaClass the native Java class corresponding to the DavisBase data type (not null)
   */
  DataType(Class<?> javaClass) {
    checkNotNull(javaClass);

    this.javaClass = javaClass;
  }

  /**
   * @return the native Java class corresponding to the DavisBase data type (not null)
   */
  public Class<?> getJavaClass() {
    return javaClass;
  }

}
