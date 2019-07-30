package edu.utdallas.davisbase.command;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Objects.hash;

import static org.checkerframework.checker.nullness.NullnessUtil.castNonNull;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

import edu.utdallas.davisbase.DataType;

/**
 * The specification of a column to update (including the value to update it to) for a
 * {@link UpdateCommand}.
 */
public class UpdateCommandColumn {

  private final byte columnIndex;
  private final @Nullable Object value;

  /**
   * @param columnIndex the zero-based index of the column in the source table to update (positive,
   *                    less than {@link java.lang.Byte#MAX_VALUE Byte.MAX_VALUE})
   * @param value       the literal value to which to assign to the column (either {@code null} or
   *                    an instance of the class returned by {@link DataType#getJavaClass()} for one
   *                    of the {@link DataType}s)
   */
  public UpdateCommandColumn(byte columnIndex, @Nullable Object value) {
    checkArgument(1 <= columnIndex && columnIndex < Byte.MAX_VALUE,
        format("columnIndex %d should be in the range [1, %d)", columnIndex, Byte.MAX_VALUE));
    checkArgument(value != null || stream(DataType.values()).map(DataType::getJavaClass).anyMatch(cls -> castNonNull(value).getClass().equals(cls)),
        "value should be either null or an instance of one of edu.utdallas.davisbase.Datatype#getJavaClass()");

    this.columnIndex = columnIndex;
    this.value = value;
  }

  /**
   * @return the zero-based index of the column in the source table to update (positive, less than
   *         {@link java.lang.Byte#MAX_VALUE Byte.MAX_VALUE})
   */
  public byte getColumnIndex() {
    return columnIndex;
  }

  /**
   * @return the literal value to which to assign to the column (either {@code null} or an instance
   *         of the class returned by {@link DataType#getJavaClass()} for one of the
   *         {@link DataType}s)
   */
  public @Nullable Object getValue() {
    return value;
  }

  @Override
  @SuppressWarnings("nullness")
  public boolean equals(Object obj) {
    if (!(obj != null && obj instanceof UpdateCommandColumn)) {
      return false;
    }

    UpdateCommandColumn other = (UpdateCommandColumn) obj;
    return
        getColumnIndex() == other.getColumnIndex() &&
        Objects.equals(getValue(), other.getValue());
  }

  @Override
  @SuppressWarnings("nullness")
  public int hashCode() {
    return hash(getColumnIndex(), getValue());
  }

  @Override
  @SuppressWarnings("nullness")
  public String toString() {
    return toStringHelper(UpdateCommandColumn.class)
        .add("columnIndex", getColumnIndex())
        .add("value", getValue())
        .toString();
  }

}
