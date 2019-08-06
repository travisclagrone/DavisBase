package edu.utdallas.davisbase.storage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Preconditions.checkNotNull;
import static edu.utdallas.davisbase.RowIdUtils.ROWID_COLUMN_INDEX;
import static edu.utdallas.davisbase.RowIdUtils.ROWID_MAX_VALUE;
import static edu.utdallas.davisbase.RowIdUtils.ROWID_MIN_VALUE;
import static edu.utdallas.davisbase.storage.DataUtils.convertToBytes;
import static java.lang.String.format;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.util.LinkedList;
import org.checkerframework.checker.nullness.qual.Nullable;

public class TableRowBuilder {

  private final LinkedList<Object> values = new LinkedList<>();

  public TableRowBuilder() {

  }

  public void appendNull() {
    this.values.add("");
  }

  public void appendTinyInt(byte value) {
    this.values.add(value);
  }

  public void appendSmallInt(short value) {
    this.values.add(value);
  }

  public void appendInt(int value) {
    this.values.add(value);
  }

  public void appendBigInt(long value) {
    this.values.add(value);
  }

  public void appendFloat(float value) {
    this.values.add(value);
  }

  public void appendDouble(double value) {
    this.values.add(value);
  }

  public void appendYear(Year value) {
    checkNotNull(value);
    this.values.add(value);
  }

  public void appendTime(LocalTime value) {
    checkNotNull(value);
    this.values.add(value);
  }

  public void appendDateTime(LocalDateTime value) {
    checkNotNull(value);
    this.values.add(value);
  }

  public void appendDate(LocalDate value) {
    checkNotNull(value);
    this.values.add(value);
  }

  public void appendText(String value) {
    checkNotNull(value);
    this.values.add(value);
  }

  void prependRowId(int rowId) {
    checkArgument(ROWID_MIN_VALUE <= rowId && rowId <= ROWID_MAX_VALUE);
    this.values.addFirst(rowId);
  }

  /**
   * Replaces the value at index
   * {@link edu.utdallas.davisbase.RowIdUtils#ROWID_COLUMN_INDEX RowIdUtils.ROWID_COLUMN_INDEX} in
   * this {@code TableRowBuilder} with {@code newRowId}. If the index {@code ROWID_COLUMN_INDEX}
   * does not currently exist in
   *
   * @param newRowId the value with which to replace the old logical ROWID value in this
   *                 {@code TableRowBuilder}
   * @see RowIdUtils
   */
  void replaceRowId(int newRowId) {
    checkArgument(ROWID_MIN_VALUE <= newRowId && newRowId <= ROWID_MAX_VALUE);
    checkState(!this.isEmpty(),
        format("Cannot replace this' old ROWID value with newRowId %d because this is empty.",
            newRowId));

    this.values.set(ROWID_COLUMN_INDEX, newRowId);
  }

  /**
   * @return the value at index
   *         {@link edu.utdallas.davisbase.RowIdUtils#ROWID_COLUMN_INDEX RowIdUtils.ROWID_COLUMN_INDEX}
   *         in this {@code TableRowBuilder} as the type of a ROWID only if the index exists, the
   *         value is not null, and the value is an instance of the type of a ROWID
   */
  int getRowId() {
    return (int) this.values.get(ROWID_COLUMN_INDEX);
  }

  /**
   * @param index the zero-based index of the `index`-th value in this {@code TableRowBuilder}
   * @return the value at the given {@code index} in this {@code TableRowBuilder)}
   */
  public @Nullable Object getValue(int index) {
    return this.values.get(index);
  }

  /**
   * @return @{code true} if-and-only-if this {@code TableRowBuilder} does not contain any values
   */
  public boolean isEmpty() {
    return this.values.isEmpty();
  }

  /**
   * @return the count of values currently in this {@code TableRowBuilder}
   */
  public int size() {
    return this.values.size();
  }

  public TableLeafCellBuffer toLeafCellBuffer() {
    final TableLeafCellBuffer cell = new TableLeafCellBuffer();
    for (final @Nullable Object value : this.values) {
      final byte[] data = convertToBytes(value);
      cell.add(data);
    }
    return cell;
  }

}
