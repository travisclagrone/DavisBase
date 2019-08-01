package edu.utdallas.davisbase.storage;

import static com.google.common.base.Preconditions.checkArgument;
import static edu.utdallas.davisbase.DateTimeUtils.DATETIME_ZONE_OFFSET;
import static edu.utdallas.davisbase.TextUtils.TEXT_CHARSET;
import static edu.utdallas.davisbase.TextUtils.TEXT_MAX_BINARY_LENGTH;
import static edu.utdallas.davisbase.YearUtils.YEAR_OFFSET;
import static java.lang.Double.doubleToLongBits;
import static java.lang.Float.floatToIntBits;
import static java.lang.String.format;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import edu.utdallas.davisbase.DataType;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;

/**
 * Utilities and constants for performing internal
 * {@link edu.utdallas.davisbase.storage.Storage Storage}-layer operations on instances of the Java
 * classes associated with the DavisBase {@link edu.utdallas.davisbase.DataType DataType}s.
 */
class DataUtils {

  private static byte[] NULL_BINARY_VALUE = new byte[0];

  private DataUtils() {
    throw new IllegalStateException(
        format("%s may not be instantiated.",
            DataUtils.class.getName()));
  }

  public static byte[] convertToBytes(@Nullable Object value) {
    final byte[] binaryValue;
    if (value == null) {
      binaryValue = convertNullToBytes();
    }
    else if (DataType.TINYINT.getJavaClass().isInstance(value)) {
      binaryValue = convertTinyIntToBytes((Byte) value);
    }
    else if (DataType.SMALLINT.getJavaClass().isInstance(value)) {
      binaryValue = convertSmallIntToBytes((Short) value);
    }
    else if (DataType.INT.getJavaClass().isInstance(value)) {
      binaryValue = convertIntToBytes((Integer) value);
    }
    else if (DataType.BIGINT.getJavaClass().isInstance(value)) {
      binaryValue = convertBigIntToBytes((Long) value);
    }
    else if (DataType.FLOAT.getJavaClass().isInstance(value)) {
      binaryValue = convertFloatToBytes((Float) value);
    }
    else if (DataType.DOUBLE.getJavaClass().isInstance(value)) {
      binaryValue = convertDoubleToBytes((Double) value);
    }
    else if (DataType.YEAR.getJavaClass().isInstance(value)) {
      binaryValue = convertYearToBytes((Year) value);
    }
    else if (DataType.TIME.getJavaClass().isInstance(value)) {
      binaryValue = convertTimeToBytes((LocalTime) value);
    }
    else if (DataType.DATETIME.getJavaClass().isInstance(value)) {
      binaryValue = convertDateTimeToBytes((LocalDateTime) value);
    }
    else if (DataType.DATE.getJavaClass().isInstance(value)) {
      binaryValue = convertDateToBytes((LocalDate) value);
    }
    else if (DataType.TEXT.getJavaClass().isInstance(value)) {
      binaryValue = convertTextToBytes((String) value);
    }
    else {
      throw new IllegalArgumentException(
          format("value is neither null nor an instance of %1$s#getJavaClass() for any of %1$s#values(), but rather is an instance of %2$s",
              DataType.class.getName(),
              value.getClass().getName()));
    }
    return binaryValue;
  }

  private static byte[] convertNullToBytes() {
    return NULL_BINARY_VALUE;
  }

  private static byte[] convertTinyIntToBytes(byte value) {
    return new byte[] { value };
  }

  private static byte[] convertSmallIntToBytes(short value) {
    return Shorts.toByteArray(value);
  }

  private static byte[] convertIntToBytes(int value) {
    return Ints.toByteArray(value);
  }

  private static byte[] convertBigIntToBytes(long value) {
    return Longs.toByteArray(value);
  }

  private static byte[] convertFloatToBytes(float value) {
    return Ints.toByteArray(floatToIntBits(value));
  }

  private static byte[] convertDoubleToBytes(double value) {
    return Longs.toByteArray(doubleToLongBits(value));
  }

  private static byte[] convertYearToBytes(Year value) {
    assert value != null;

    final int isoYear = value.getValue();
    final int offsetYear = isoYear - YEAR_OFFSET;

    checkArgument(Byte.MIN_VALUE <= offsetYear && offsetYear <= Byte.MAX_VALUE,
        format("The Java Year value %04d is not in the domain of the DavisBase YEAR data type: [%04d, %04d].",
            isoYear,
            YEAR_OFFSET + Byte.MIN_VALUE,
            YEAR_OFFSET + Byte.MAX_VALUE));

    return new byte[] { (byte) offsetYear };
  }

  private static byte[] convertTimeToBytes(LocalTime value) {
    assert value != null;

    return Ints.toByteArray(value.toSecondOfDay());
  }

  private static byte[] convertDateTimeToBytes(LocalDateTime value) {
    assert value != null;

    return Longs.toByteArray(value.toEpochSecond(DATETIME_ZONE_OFFSET));
  }

  private static byte[] convertDateToBytes(LocalDate value) {
    assert value != null;

    return Longs.toByteArray(value.toEpochDay());
  }

  private static byte[] convertTextToBytes(String value) {
    assert value != null;

    final byte[] binaryValue = value.getBytes(TEXT_CHARSET);
    checkArgument(binaryValue.length <= TEXT_MAX_BINARY_LENGTH,
        format("value is converted with the %s charset to %d bytes, which is more than the maximum %d byte length of serialized TEXT data. value is \"%s\".",
            TEXT_CHARSET.name(),
            binaryValue.length,
            TEXT_MAX_BINARY_LENGTH,
            value.replace("\"", "\\\"")));

    return binaryValue;
  }

}
