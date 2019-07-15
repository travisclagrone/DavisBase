package edu.utdallas.davisbase.storage;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;

import org.checkerframework.checker.nullness.qual.Nullable;

import edu.utdallas.davisbase.NotImplementedException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkElementIndex;

/**
 * A DavisBase "Table" file.
 *
 * A {@link TableFile} object is the lowest-level <i>structured</i> interface to a DavisBase "Table"
 * file. It is intended to wrap a live (albeit closeable) file connection, and thereby abstract
 * access and manipulation of the underlying binary file as if it were <b>a collection of
 * records</b>.
 *
 * A {@link TableFile} object functions similarly to a mutable forward-only cursor.
 *
 * @apiNote Unlike a conceptual "table", a {@link TableFile} object does not have a schema. This is
 *          because the {@link TableFile} class is intended to abstract only the binary structures
 *          and algorithms (e.g. paging, record de-/serialization, b-tree balancing, etc.), and be
 *          used by other higher-level classes to effect a schematic table and complex SQL
 *          operations. As such, <b>schematically correct reading and writing of records is the
 *          responsibility of the code using a {@link TableFile} object</b>.
 */
public class TableFile implements Closeable {

  private final RandomAccessFile file;
  private short rootPageId;
  // TODO Add current state

  public TableFile(RandomAccessFile file, short rootPageId) {
    checkNotNull(file);
    checkArgument(file.getChannel().isOpen());

    checkElementIndex(rootPageId, Short.MAX_VALUE);

    this.file = file;
    this.rootPageId = rootPageId;
  }

  public void appendRow(TableRowBuilder tableRowBuilder) {
    // TODO Implement TableFile.appendRow(TableRowBuilder)
    throw new NotImplementedException();
  }

  @Override
  public void close() throws IOException {
    file.close();
  }

  public boolean goToNextRow() {
    // TODO Implement TableFile.goToNextRow()
    throw new NotImplementedException();
  }

  public boolean goToRow(int rowId) {
    // TODO Implement TableFile.goToRow(int)
    throw new NotImplementedException();
  }

  public @Nullable Byte readTinyInt(int columnIndex) {
    // TODO Implement TableFile.readTinyInt(int)
    throw new NotImplementedException();
  }

  public @Nullable Short readSmallInt(int columnIndex) {
    // TODO Implement TableFile.readSmallInt(int)
    throw new NotImplementedException();
  }

  public @Nullable Integer readInt(int columnIndex) {
    // TODO Implement TableFile.readInt(int)
    throw new NotImplementedException();
  }

  public @Nullable Long readBigInt(int columnIndex) {
    // TODO Implement TableFile.readBigInt(int)
    throw new NotImplementedException();
  }

  public @Nullable Float readFloat(int columnIndex) {
    // TODO Implement TableFile.readFloat(int)
    throw new NotImplementedException();
  }

  public @Nullable Double readDouble(int columnIndex) {
    // TODO Implement TableFile.readDouble(int)
    throw new NotImplementedException();
  }

  public @Nullable Year readYear(int columnIndex) {
    // TODO Implement TableFile.readYear(int)
    throw new NotImplementedException();
  }

  public @Nullable LocalTime readTime(int columnIndex) {
    // TODO Implement TableFile.readTime(int)
    throw new NotImplementedException();
  }

  public @Nullable LocalDateTime readDateTime(int columnIndex) {
    // TODO Implement TableFile.readDateTime(int)
    throw new NotImplementedException();
  }

  public @Nullable LocalDate readDate(int columnIndex) {
    // TODO Implement TableFile.readDate(int)
    throw new NotImplementedException();
  }

  public @Nullable String readText(int columnIndex) {
    // TODO Implement TableFile.readText(int)
    throw new NotImplementedException();
  }

  public void removeRow() {
    // TODO Implement TableFile.removeRow()
    throw new NotImplementedException();
  }

  public void writeNull(int columnIndex) {
    // TODO Implement TableFile.writeNull(int)
    throw new NotImplementedException();
  }

  public void writeTinyIny(int columnIndex, byte value) {
    // TODO Implement TableFile.writeTinyInt(int, byte)
    throw new NotImplementedException();
  }

  public void writeSmallInt(int columnIndex, short value) {
    // TODO Implement TableFile.writeSmallInt(int, short)
    throw new NotImplementedException();
  }

  public void writeInt(int columnIndex, int value) {
    // TODO Implement TableFile.writeInt(int, int)
    throw new NotImplementedException();
  }

  public void writeBigInt(int columnIndex, long value) {
    // TODO Implement TableFile.writeBigInt(int, long)
    throw new NotImplementedException();
  }

  public void writeFloat(int columnIndex, float value) {
    // TODO Implement TableFile.writeFloat(int, float)
    throw new NotImplementedException();
  }

  public void writeDouble(int columnIndex, double value) {
    // TODO Implement TableFile.writeDouble(int, double)
    throw new NotImplementedException();
  }

  public void writeYear(int columnIndex, Year value) {
    checkNotNull(value);

    // TODO Implement TableFile.writeYear(int, Year)
    throw new NotImplementedException();
  }

  public void writeTime(int columnIndex, LocalTime value) {
    checkNotNull(value);

    // TODO Implement TableFile.writeTime(int, LocalTime)
    throw new NotImplementedException();
  }

  public void writeDateTime(int columnIndex, LocalDateTime value) {
    checkNotNull(value);

    // TODO Implement TableFile.writeDateTime(int, value)
    throw new NotImplementedException();
  }

  public void writeDate(int columnIndex, LocalDate value) {
    checkNotNull(value);

    // TODO Implement TableFile.writeDate(int, LocalDate)
    throw new NotImplementedException();
  }

  public void writeText(int columnIndex, String value) {
    checkNotNull(value);

    // TODO Implement TableFile.writeText(int, String)
    throw new NotImplementedException();
  }

}
