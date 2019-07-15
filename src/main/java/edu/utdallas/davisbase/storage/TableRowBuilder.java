package edu.utdallas.davisbase.storage;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import edu.utdallas.davisbase.NotImplementedException;

public class TableRowBuilder {

  private final int rowId;
  private final List<Object> values = new ArrayList<>();

  public TableRowBuilder(int rowId) {
    checkElementIndex(rowId, Integer.MAX_VALUE);
    this.rowId = rowId;
  }

  public void appendNull() {
    // TODO Implement TableRowBuilder.appendNull()
    throw new NotImplementedException();
  }

  public void appendTinyInt(byte value) {
    // TODO Implement TableRowBuilder.appendTinyInt(byte)
    throw new NotImplementedException();
  }

  public void appendSmallInt(short value) {
    // TODO Implement TableRowBuilder.appendSmallInt(short)
    throw new NotImplementedException();
  }

  public void appendInt(int value) {
    // TODO Implement TableRowBuilder.appendInt(int)
    throw new NotImplementedException();
  }

  public void appendBigInt(long value) {
    // TODO Implement TableRowBuilder.appendBigInt(long)
    throw new NotImplementedException();
  }

  public void appendFloat(float value) {
    // TODO Implement TableRowBuilder.appendFloat(float)
    throw new NotImplementedException();
  }

  public void appendDouble(double value) {
    // TODO Implement TableRowBuilder.appendDouble(double)
    throw new NotImplementedException();
  }

  public void appendYear(Year value) {
    checkNotNull(value);

    // TODO Implement TableRowBuilder.appendYear(Year)
    throw new NotImplementedException();
  }

  public void appendTime(LocalTime value) {
    checkNotNull(value);

    // TODO Implement TableRowBuilder.appendTime(LocalTime)
    throw new NotImplementedException();
  }

  public void appendDateTime(LocalDateTime value) {
    checkNotNull(value);

    // TODO Implement TableRowBuilder.appendDateTime(LocalDateTime)
    throw new NotImplementedException();
  }

  public void appendDate(LocalDate value) {
    checkNotNull(value);

    // TODO Implement TableRowBuilder.appendDate(LocalDate)
    throw new NotImplementedException();
  }

  public void appendText(String value) {
    checkNotNull(value);

    // TODO Implement TableRowBuilder.appendText(String)
    throw new NotImplementedException();
  }

  public byte[] toBytes() {
    // TODO Implement TableRowBuilder.toBytes()
    throw new NotImplementedException();
  }
}
