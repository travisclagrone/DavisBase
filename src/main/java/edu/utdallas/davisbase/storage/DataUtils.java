package edu.utdallas.davisbase.storage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
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
import edu.utdallas.davisbase.NotImplementedException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
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

  private static byte[] EMPTY_BYTE_ARRAY = new byte[0];
  private static byte[] NULL_BINARY_VALUE = EMPTY_BYTE_ARRAY;

  private DataUtils() {
    throw new IllegalStateException(
        format("%s may not be instantiated.",
            DataUtils.class.getName()));
  }

  //region Convert (to byte array)

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

  public static byte[] convertNullToBytes() {
    return NULL_BINARY_VALUE;
  }

  public static byte[] convertTinyIntToBytes(byte value) {
    return new byte[] { value };
  }

  public static byte[] convertSmallIntToBytes(short value) {
    return Shorts.toByteArray(value);
  }

  public static byte[] convertIntToBytes(int value) {
    return Ints.toByteArray(value);
  }

  public static byte[] convertBigIntToBytes(long value) {
    return Longs.toByteArray(value);
  }

  public static byte[] convertFloatToBytes(float value) {
    return Ints.toByteArray(floatToIntBits(value));
  }

  public static byte[] convertDoubleToBytes(double value) {
    return Longs.toByteArray(doubleToLongBits(value));
  }

  public static byte[] convertYearToBytes(@NonNull Year value) {
    checkNotNull(value, "value");

    final int isoYear = value.getValue();
    final int offsetYear = isoYear - YEAR_OFFSET;

    checkArgument(Byte.MIN_VALUE <= offsetYear && offsetYear <= Byte.MAX_VALUE,
        format("The Java Year value %04d is not in the domain of the DavisBase YEAR data type: [%04d, %04d].",
            isoYear,
            YEAR_OFFSET + Byte.MIN_VALUE,
            YEAR_OFFSET + Byte.MAX_VALUE));

    return new byte[] { (byte) offsetYear };
  }

  public static byte[] convertTimeToBytes(@NonNull LocalTime value) {
    checkNotNull(value, "value");

    return Ints.toByteArray(value.toSecondOfDay());
  }

  public static byte[] convertDateTimeToBytes(@NonNull LocalDateTime value) {
    checkNotNull(value, "value");

    return Longs.toByteArray(value.toEpochSecond(DATETIME_ZONE_OFFSET));
  }

  public static byte[] convertDateToBytes(@NonNull LocalDate value) {
    checkNotNull(value, "value");

    return Longs.toByteArray(value.toEpochDay());
  }

  public static byte[] convertTextToBytes(@NonNull String value) {
    checkNotNull(value, "value");

    final byte[] binaryValue = value.getBytes(TEXT_CHARSET);
    checkArgument(binaryValue.length <= TEXT_MAX_BINARY_LENGTH,
        format("value is converted with the %s charset to %d bytes, which is more than the maximum %d byte length of serialized TEXT data. value is \"%s\".",
            TEXT_CHARSET.name(),
            binaryValue.length,
            TEXT_MAX_BINARY_LENGTH,
            value.replace("\"", "\\\"")));

    return binaryValue;
  }

  //endregion

  //region Output (to binary stream)

  public static void output(DataOutput output, @Nullable Object value) throws IOException {
    if (value == null) {
      outputNull(output);
    }
    else if (DataType.TINYINT.getJavaClass().isInstance(value)) {
      outputTinyInt(output, (Byte) value);
    }
    else if (DataType.SMALLINT.getJavaClass().isInstance(value)) {
      outputSmallInt(output, (Short) value);
    }
    else if (DataType.INT.getJavaClass().isInstance(value)) {
      outputInt(output, (Integer) value);
    }
    else if (DataType.BIGINT.getJavaClass().isInstance(value)) {
      outputBigInt(output, (Long) value);
    }
    else if (DataType.FLOAT.getJavaClass().isInstance(value)) {
      outputFloat(output, (Float) value);
    }
    else if (DataType.DOUBLE.getJavaClass().isInstance(value)) {
      outputDouble(output, (Double) value);
    }
    else if (DataType.YEAR.getJavaClass().isInstance(value)) {
      outputYear(output, (Year) value);
    }
    else if (DataType.TIME.getJavaClass().isInstance(value)) {
      outputTime(output, (LocalTime) value);
    }
    else if (DataType.DATETIME.getJavaClass().isInstance(value)) {
      outputDateTime(output, (LocalDateTime) value);
    }
    else if (DataType.DATE.getJavaClass().isInstance(value)) {
      outputDate(output, (LocalDate) value);
    }
    else if (DataType.TEXT.getJavaClass().isInstance(value)) {
      outputText(output, (String) value);
    }
    else {
      throw new IllegalArgumentException(
          format("value is neither null nor an instance of %1$s#getJavaClass() for any of %1$s#values(), but rather is an instance of %2$s",
              DataType.class.getName(),
              value.getClass().getName()));
    }
  }

  public static void outputNull(DataOutput output) throws IOException {
    final byte[] data = convertNullToBytes();
    output.write(data);
  }

  public static void outputTinyInt(DataOutput output, byte value) throws IOException {
    output.writeByte(value);
  }

  public static void outputSmallInt(DataOutput output, short value) throws IOException {
    output.writeShort(value);
  }

  public static void outputInt(DataOutput output, int value) throws IOException {
    output.writeInt(value);
  }

  public static void outputBigInt(DataOutput output, long value) throws IOException {
    output.writeLong(value);
  }

  public static void outputFloat(DataOutput output, float value) throws IOException {
    output.writeFloat(value);
  }

  public static void outputDouble(DataOutput output, double value) throws IOException {
    output.writeDouble(value);
  }

  public static void outputYear(DataOutput output, @NonNull Year value) throws IOException {
    // Delegate to the byte array conversion method because it contains important validation logic.
    final byte[] data = convertYearToBytes(value);
    output.write(data);
  }

  public static void outputTime(DataOutput output, @NonNull LocalTime value) throws IOException {
    final int secondOfDay = value.toSecondOfDay();
    output.writeInt(secondOfDay);
  }

  public static void outputDateTime(DataOutput output, @NonNull LocalDateTime value) throws IOException {
    final long epochSecond = value.toEpochSecond(DATETIME_ZONE_OFFSET);
    output.writeLong(epochSecond);
  }

  public static void outputDate(DataOutput output, @NonNull LocalDate value) throws IOException {
    final long epochDay = value.toEpochDay();
    output.writeLong(epochDay);
  }

  public static void outputText(DataOutput output, @NonNull String value) throws IOException {
    // Delegate to the byte array conversion method because it contains important validation logic.
    final byte[] data = convertTextToBytes(value);
    output.write(data);
  }

  //endregion

  //region Input (from binary stream)

  public static @Nullable Object inputNull(DataInput input) throws IOException {
    assert EMPTY_BYTE_ARRAY != null && EMPTY_BYTE_ARRAY.length == 0;

    input.readFully(EMPTY_BYTE_ARRAY);
    return null;
  }

  public static byte inputTinyInt(DataInput input) throws IOException {
    return input.readByte();
  }

  public static short inputSmallInt(DataInput input) throws IOException {
    return input.readShort();
  }

  public static int inputInt(DataInput input) throws IOException {
    return input.readInt();
  }

  public static long inputBigInt(DataInput input) throws IOException {
    return input.readLong();
  }

  public static float inputFloat(DataInput input) throws IOException {
    return input.readFloat();
  }

  public static double inputDouble(DataInput input) throws IOException {
    return input.readDouble();
  }

  public static @NonNull Year inputYear(DataInput input) throws IOException {
    final byte offsetYear = input.readByte();
    final int isoYear = offsetYear + YEAR_OFFSET;
    return Year.of(isoYear);
  }

  public static @NonNull LocalTime inputTime(DataInput input) throws IOException {
    final int secondOfDay = input.readInt();
    return LocalTime.ofSecondOfDay(secondOfDay);
  }

  public static @NonNull LocalDateTime inputDateTime(DataInput input) throws IOException {
    final long epochSecond = input.readLong();
    return LocalDateTime.ofEpochSecond(epochSecond, 0, DATETIME_ZONE_OFFSET);
  }

  public static @NonNull LocalDate inputDate(DataInput input) throws IOException {
    final long epochDay = input.readLong();
    return LocalDate.ofEpochDay(epochDay);
  }

  public static @NonNull String inputText(DataInput input, byte dataLength) throws IOException {
    checkArgument(0 <= dataLength, "TEXT dataLength may not be negative.");
    checkArgument(dataLength <= TEXT_MAX_BINARY_LENGTH, "TEXT dataLength may not be greater than " + TEXT_MAX_BINARY_LENGTH);

    final byte[] data = new byte[dataLength];
    input.readFully(data);
    return new String(data, TEXT_CHARSET);
  }

  //endregion

}
