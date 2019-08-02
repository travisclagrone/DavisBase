package edu.utdallas.davisbase.storage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static edu.utdallas.davisbase.TextUtils.TEXT_CHARSET;
import static edu.utdallas.davisbase.storage.DataUtils.*;
import static edu.utdallas.davisbase.storage.Page.*;
import static edu.utdallas.davisbase.storage.TablePageType.*;
import static java.lang.Integer.min;
import static java.lang.String.format;
import static java.util.Arrays.stream;

import edu.utdallas.davisbase.DataType;
import edu.utdallas.davisbase.NotImplementedException;
import edu.utdallas.davisbase.YearUtils;
import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.Map;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Shorts;
import org.checkerframework.checker.nullness.qual.Nullable;

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

  private static final int   NULL_PAGE_NO    = -1;
  private static final short NULL_CELL_INDEX = -1;

  private int   currentLeafPageNo    = NULL_PAGE_NO;
  private short currentLeafCellIndex = NULL_CELL_INDEX;

  protected final RandomAccessFile file;

  public TableFile(RandomAccessFile file) {
    checkNotNull(file);
    checkArgument(file.getChannel().isOpen());
    this.file = file;

    try {

      if (file.length() < 512) {
        Page.addTableMetaDataPage(file);
      }
    } catch (Exception e) {

    }
  }

  @Override
  public void close() throws IOException {
    file.close();
  }

  public void appendRow(TableRowBuilder tableRowBuilder) throws IOException {

    //region Find rightmost leaf page.

    // FIXME Initialize pageNo to the root page (from metadata) instead of the last physical page.
    int  pageNo = Ints.checkedCast(file.length() / PAGE_SIZE);
    long pageFileOffset = convertPageNoToFileOffset(pageNo);
    byte pageTypeCode;

    // TODO Refactor the following two lines to a new method Page+getRightPageNo(file, pageNo)
    file.seek(pageFileOffset + 6);
    int rightPageNo = file.readInt();

    while (rightPageNo != -1) {  // FIXME magic number
      pageNo = rightPageNo;
      pageFileOffset = convertPageNoToFileOffset(pageNo);

      file.seek(pageFileOffset + 6);
      rightPageNo = file.readInt();
    }

    file.seek(pageFileOffset);
    pageTypeCode = file.readByte();

    //endregion

    //region Calculate total space required to insert row.

    // TODO Convert tableRowBuilder to a TableLeafCellBuffer.
    // TODO "Calculate" cell space requirements using TableLeafCellBuffer.

    final int nColumns = tableRowBuilder.getNoOfValues() + 1;  // count of columns
    int[] columnSizes = new int[nColumns];  // size in bytes
    int payloadSize = 0;  // total size in bytes

    columnSizes[0] = 4;  // since rowId is not a part of table row builder
    payloadSize += columnSizes[0];

    @Nullable Object value;
    for (int i = 1; i < nColumns; i++) {
      value = tableRowBuilder.getValueAt(i - 1);
      switch (value.getClass().getSimpleName()) {
        case "Integer":
          columnSizes[i] = 4;
          break;
        case "String":
          columnSizes[i] = value.toString().getBytes(TEXT_CHARSET).length;
          break;
        case "Byte":
          columnSizes[i] = 1;
          break;
        case "Short":
          columnSizes[i] = 2;
          break;
        case "Long":
          columnSizes[i] = 8;
          break;
        case "Float":
          columnSizes[i] = 4;
          break;
        case "Double":
          columnSizes[i] = 8;
          break;
        case "Year":
          columnSizes[i] = 1;
          break;
        case "LocalTime":
          columnSizes[i] = 4;
          break;
        case "LocalDateTime":
          columnSizes[i] = 8;
          break;
        case "LocalDate":
          columnSizes[i] = 8;
          break;

        /* FIXME case ""
         *
         * Refactor this case to use proper null evaluation as designed, rather than repurposing the
         * empty string to serve as a "null object". Make sure to refactor any TableDataRow building
         * and serialization logic that is coupled to this empty-string-as-null-object design, too.
         */
        case "":
          columnSizes[i] = 0;
          break;

        /* FIXME default
         *
         * The default case should throw an IllegalArgumentException since the value was not an
         * of the corresponding Java class for any DavisBase data type.
         */
        default:
          columnSizes[i] = 0;
          break;
      }
      payloadSize += columnSizes[i];
    }

    final int newCellDataSize = (1 + columnSizes.length + payloadSize);

    //endregion

    //region Check ahead for overflow, and preemptively "split" page if so.

    final boolean overflowFlag = wouldPageOverflow(newCellDataSize, pageNo);

    if (overflowFlag) {
      // QUESTION Can the original local pageNo actually be INTERIOR? If so, why? (it shouldn't be) If not, then what step(s) are we missing that's leading us to think that it could be?
      if (pageTypeCode == 0x05) {  // FIXME magic number
        pageNo = Page.splitInteriorPage(file, pageNo);  // VERIFY Page+splitInteriorPage(file, pageNo)
      }
      else if (pageTypeCode == 0x0D) {  // FIXME magic number
        pageNo = Page.splitLeafPage(file, pageNo);  // VERIFY Page+splitLeafPage(file, pageNo)
      }
      else {
        throw new IllegalStateException(
          format("Unrecognized page type serial code: %#04x",
              pageTypeCode));
      }

      pageFileOffset = convertPageNoToFileOffset(pageNo);

      file.seek(pageFileOffset);
      pageTypeCode = file.readByte();
    }

    //endregion

    //region Allocate new rowId, and update metadata in table accordingly.

    // QUESTION Can the post-split page actually be INTERIOR? If so, why? If not, what's fucked up?
    final int newRowId;
    if (pageTypeCode == 0x05) {  // FIXME magic number
      newRowId = getNextRowIdInterior();  // VERIFY -getNextRowIdInterior(): int
      file.seek(0x09);  // FIXME magic number
      file.writeInt(newRowId);
    }
    else if (pageTypeCode == 0x0D) {  // FIXME magic number
      newRowId = getNextRowId();  // VERIFY -getNextRowId(): int
      file.seek(0x01);  // FIXME magic number
      file.writeInt(newRowId);
    }
    else {
      throw new IllegalStateException(
          format("Unrecognized page type serial code: %#04x",
              pageTypeCode));
    }

    //endregion

    //region Calculate new "cell content area" page offset.

    file.seek(pageFileOffset + 3);  // FIXME magic number
    short oldContentPageOffset = file.readShort();
    assert oldContentPageOffset >= 0;

    if (oldContentPageOffset <= 0) {
      oldContentPageOffset = Shorts.checkedCast(PAGE_SIZE);
    }

    final short newContentPageOffset = Shorts.checkedCast(oldContentPageOffset - newCellDataSize);

    //endregion

    //region Insert cell in content area of page.

    // TODO Serialize using TableLeafCellBuffer.

    file.seek(pageFileOffset + newContentPageOffset);

    file.writeByte(nColumns);

    for (int i = 0; i < columnSizes.length; i++) {
      file.writeByte(columnSizes[i]);
    }

    file.writeInt(newRowId);
    file.write(tableRowBuilder.toBytes());

    //endregion

    //region Increment cell count in page.

    file.seek(pageFileOffset + 1);  // FIXME magic number
    final short oldPageCellCount = file.readShort();

    final short newPageCellCount = Shorts.checkedCast(oldPageCellCount + 1);

    file.seek(pageFileOffset + 1);  // FIXME magic number
    file.writeShort(newPageCellCount);

    //endregion

    //region Update "cell content area" page offset in page.

    file.seek(pageFileOffset + 3);  // FIXME magic number
    file.writeShort(newContentPageOffset);

    //endregion

    //region Append the new cell's page offset to the array of such in page.

    file.seek(pageFileOffset + 16 + 2 * (newPageCellCount - 1));  // FIXME magic numbers
    file.writeShort(newContentPageOffset);

    /* NOTE newContentPageOffset
     *
     * The cell was inserted beginning at newContentPageOffset, and so newContentPageOffset is
     * exactly the newCellPageOffset for this cell.
     */

    //endregion

    // TODO Recursively update ancestors with new greatest row id key (as far as applicable, anyway).
  }

  private void appendRow(TableLeafCellBuffer cellBuffer) throws IOException {
    // TODO TableFile-appendRow(TableLeafCellBuffer)
    throw new NotImplementedException("TableFile-appendRow(TableLeafCellBuffer)");
  }

  // VERIFY -wouldPageOverflow(newCellDataSize, pageNo): boolean
  private boolean wouldPageOverflow(int newCellDataSize, int pageNo) {
    try {
      file.seek((pageNo - 1) * PAGE_SIZE);
      byte pageType = file.readByte();
      if (pageType == 0x0D) {

        this.file.seek((pageNo - 1) * PAGE_SIZE + 1);
        short noOfRecords = this.file.readShort();

        this.file.seek((pageNo - 1) * PAGE_SIZE + 3);
        short startofCellConcent = file.readShort();
        if (startofCellConcent == 0) {
          startofCellConcent = (short) (PAGE_SIZE);
        }
        short arryLastEntry = (short) (16 + (noOfRecords * 2));
        if ((startofCellConcent - arryLastEntry - 1) < newCellDataSize) {
          return true;
        }
      } else {// to Update in Part 2 for the remainig page types
        return false;
      }
    } catch (Exception e) {

    }
    return false;
  }

  public boolean goToNextRow() throws IOException {
    assert this.hasCurrentLeafPageNo() == this.hasCurrentLeafCellIndex();

    if (this.hasCurrentLeafPageNo()) {
      this.currentLeafCellIndex += 1;
    } else { // Very first time goToNextRow() has been called for this TableFile instance.
      this.currentLeafPageNo = getLeftmostLeafPageNo();
      this.currentLeafCellIndex = 0;
    }

    short countCells = Page.getNumberOfCells(file, this.currentLeafPageNo);
    if (!(this.currentLeafCellIndex < countCells)) {

      final int rightSiblingPageNo = Page.getRightSiblingOfLeafPage(file, this.currentLeafPageNo);
      if (!Page.exists(file, rightSiblingPageNo)) {
        return false;
      }

      this.currentLeafPageNo = rightSiblingPageNo;
      this.currentLeafCellIndex = 0;

      countCells = Page.getNumberOfCells(file, this.currentLeafPageNo);
      if (!(this.currentLeafCellIndex < countCells)) {
        return false;
      }
    }

    return true;
  }

  public boolean goToRow(int rowId) throws IOException {
    // COMBAK Implement TableFile.goToRow(int)
    throw new NotImplementedException();
  }

  private boolean valueOfCurrentRowColumnIsNull(int columnIndex) throws IOException {
    checkArgument(0 <= columnIndex && columnIndex < Byte.MAX_VALUE,
        "columnIndex (%d) is not in range [0, %d)", columnIndex, Byte.MAX_VALUE);
    checkState(this.hasCurrentRow(),
        "tableFile is not pointing to a current row from which to read");

    final long fileOffsetOfPage = Page.convertPageNoToFileOffset(this.currentLeafPageNo);
    final short pageOffsetOfCell =
        Page.getPageOffsetOfCell(file, this.currentLeafPageNo, this.currentLeafCellIndex);
    final long fileOffsetOfPageCell = fileOffsetOfPage + pageOffsetOfCell;

    final int valueSizeInBytes =
        Page.getSizeOfTableLeafCellColumn(file, fileOffsetOfPageCell, columnIndex);
    return valueSizeInBytes <= 0;
  }

  private void goToCurrentLeafPageCellColumnValue(int columnIndex)
      throws IOException, StorageException {
    checkArgument(0 <= columnIndex && columnIndex < Byte.MAX_VALUE,
        "columnIndex (%d) is not in range [0, %d)", columnIndex, Byte.MAX_VALUE);
    checkState(this.hasCurrentRow(),
        "tableFile is not pointing to a current row from which to read");

    final long fileOffsetOfPage = Page.convertPageNoToFileOffset(this.currentLeafPageNo);
    final short pageOffsetOfCell =
        Page.getPageOffsetOfCell(file, this.currentLeafPageNo, this.currentLeafCellIndex);
    final long fileOffsetOfPageCell = fileOffsetOfPage + pageOffsetOfCell;

    final byte columnCount = Page.getNumberOfColumnsOfTableLeafCell(file, fileOffsetOfPageCell);
    if (!(columnIndex < columnCount)) {
      throw new StorageException(
          format("columnIndex (%d) is not less than columnCount (%d)", columnIndex, columnCount));
    }

    int cellOffset = 1 + columnCount; // 1 to account for the initial byte of column count.
    for (int i = 0; i < columnIndex; i++) {
      cellOffset += Page.getSizeOfTableLeafCellColumn(file, fileOffsetOfPageCell, i);
      // cellOffset += Page.getSizeOfTableLeafCellColumn(file, fileOffsetOfPageCell, columnIndex);
    }

    final long fileOffsetOfPageCellColumnValue = fileOffsetOfPageCell + cellOffset;
    file.seek(fileOffsetOfPageCellColumnValue);
  }

  public @Nullable Byte readTinyInt(int columnIndex) throws IOException, StorageException {
    checkArgument(0 <= columnIndex && columnIndex < Byte.MAX_VALUE,
        "columnIndex (%d) is not in range [0, %d)", columnIndex, Byte.MAX_VALUE);
    checkState(this.hasCurrentRow(),
        "tableFile is not pointing to a current row from which to read");

    if (valueOfCurrentRowColumnIsNull(columnIndex)) {
      return null;
    }

    goToCurrentLeafPageCellColumnValue(columnIndex);
    return file.readByte();
  }

  public @Nullable Short readSmallInt(int columnIndex) throws IOException, StorageException {
    checkArgument(0 <= columnIndex && columnIndex < Byte.MAX_VALUE,
        "columnIndex (%d) is not in range [0, %d)", columnIndex, Byte.MAX_VALUE);
    checkState(this.hasCurrentRow(),
        "tableFile is not pointing to a current row from which to read");

    if (valueOfCurrentRowColumnIsNull(columnIndex)) {
      return null;
    }

    goToCurrentLeafPageCellColumnValue(columnIndex);
    return file.readShort();
  }

  public @Nullable Integer readInt(int columnIndex) throws IOException, StorageException {
    checkArgument(0 <= columnIndex && columnIndex < Byte.MAX_VALUE,
        "columnIndex (%d) is not in range [0, %d)", columnIndex, Byte.MAX_VALUE);
    checkState(this.hasCurrentRow(),
        "tableFile is not pointing to a current row from which to read");

    if (valueOfCurrentRowColumnIsNull(columnIndex)) {
      return null;
    }

    goToCurrentLeafPageCellColumnValue(columnIndex);
    return file.readInt();
  }

  public @Nullable Long readBigInt(int columnIndex) throws IOException, StorageException {
    checkArgument(0 <= columnIndex && columnIndex < Byte.MAX_VALUE,
        "columnIndex (%d) is not in range [0, %d)", columnIndex, Byte.MAX_VALUE);
    checkState(this.hasCurrentRow(),
        "tableFile is not pointing to a current row from which to read");

    if (valueOfCurrentRowColumnIsNull(columnIndex)) {
      return null;
    }

    goToCurrentLeafPageCellColumnValue(columnIndex);
    return file.readLong();
  }

  public @Nullable Float readFloat(int columnIndex) throws IOException, StorageException {
    checkArgument(0 <= columnIndex && columnIndex < Byte.MAX_VALUE,
        "columnIndex (%d) is not in range [0, %d)", columnIndex, Byte.MAX_VALUE);
    checkState(this.hasCurrentRow(),
        "tableFile is not pointing to a current row from which to read");

    if (valueOfCurrentRowColumnIsNull(columnIndex)) {
      return null;
    }

    goToCurrentLeafPageCellColumnValue(columnIndex);
    return file.readFloat();
  }

  public @Nullable Double readDouble(int columnIndex) throws IOException, StorageException {
    checkArgument(0 <= columnIndex && columnIndex < Byte.MAX_VALUE,
        "columnIndex (%d) is not in range [0, %d)", columnIndex, Byte.MAX_VALUE);
    checkState(this.hasCurrentRow(),
        "tableFile is not pointing to a current row from which to read");

    if (valueOfCurrentRowColumnIsNull(columnIndex)) {
      return null;
    }

    goToCurrentLeafPageCellColumnValue(columnIndex);
    return file.readDouble();
  }

  public @Nullable Year readYear(int columnIndex) throws IOException, StorageException {
    checkArgument(0 <= columnIndex && columnIndex < Byte.MAX_VALUE,
        "columnIndex (%d) is not in range [0, %d)", columnIndex, Byte.MAX_VALUE);
    checkState(this.hasCurrentRow(),
        "tableFile is not pointing to a current row from which to read");

    if (valueOfCurrentRowColumnIsNull(columnIndex)) {
      return null;
    }

    goToCurrentLeafPageCellColumnValue(columnIndex);
    return Year.of(file.readByte() + YearUtils.YEAR_OFFSET);
  }

  public @Nullable LocalTime readTime(int columnIndex) throws IOException, StorageException {
    checkArgument(0 <= columnIndex && columnIndex < Byte.MAX_VALUE,
        "columnIndex (%d) is not in range [0, %d)", columnIndex, Byte.MAX_VALUE);
    checkState(this.hasCurrentRow(),
        "tableFile is not pointing to a current row from which to read");

    if (valueOfCurrentRowColumnIsNull(columnIndex)) {
      return null;
    }

    goToCurrentLeafPageCellColumnValue(columnIndex);
    return LocalTime.ofSecondOfDay(file.readInt());
  }

  public @Nullable LocalDateTime readDateTime(int columnIndex)
      throws IOException, StorageException {
    checkArgument(0 <= columnIndex && columnIndex < Byte.MAX_VALUE,
        "columnIndex (%d) is not in range [0, %d)", columnIndex, Byte.MAX_VALUE);
    checkState(this.hasCurrentRow(),
        "tableFile is not pointing to a current row from which to read");

    if (valueOfCurrentRowColumnIsNull(columnIndex)) {
      return null;
    }

    goToCurrentLeafPageCellColumnValue(columnIndex);
    return LocalDateTime.ofEpochSecond(file.readLong(), 0, ZoneOffset.UTC);
  }

  public @Nullable LocalDate readDate(int columnIndex) throws IOException, StorageException {
    checkArgument(0 <= columnIndex && columnIndex < Byte.MAX_VALUE,
        "columnIndex (%d) is not in range [0, %d)", columnIndex, Byte.MAX_VALUE);
    checkState(this.hasCurrentRow(),
        "tableFile is not pointing to a current row from which to read");

    if (valueOfCurrentRowColumnIsNull(columnIndex)) {
      return null;
    }

    goToCurrentLeafPageCellColumnValue(columnIndex);
    return LocalDate.ofEpochDay(file.readLong());
  }

  public @Nullable String readText(int columnIndex) throws IOException, StorageException {
    checkArgument(0 <= columnIndex && columnIndex < Byte.MAX_VALUE,
        "columnIndex (%d) is not in range [0, %d)", columnIndex, Byte.MAX_VALUE);
    checkState(this.hasCurrentRow(),
        "tableFile is not pointing to a current row from which to read");

    final long fileOffsetOfPage = Page.convertPageNoToFileOffset(this.currentLeafPageNo);
    final short pageOffsetOfCell =
        Page.getPageOffsetOfCell(file, this.currentLeafPageNo, this.currentLeafCellIndex);
    final long fileOffsetOfPageCell = fileOffsetOfPage + pageOffsetOfCell;

    final int valueSizeInBytes =
        Page.getSizeOfTableLeafCellColumn(file, fileOffsetOfPageCell, columnIndex);
    if (valueSizeInBytes <= 0) {
      return null;
    }

    goToCurrentLeafPageCellColumnValue(columnIndex);
    final byte[] bytes = new byte[valueSizeInBytes];
    file.read(bytes);
    return new String(bytes);
  }

  public void removeRow() throws IOException {
    // TODO TableFile+removeRow()
    throw new NotImplementedException();
  }

  /**
   * Overwrites zero-or-more pre-existing (but nullable) columns of the current row.
   * <p>
   * May delete and re-append the row with a new {@code rowid}, but does not modify this
   * {@code TableFile}'s current row pointer. However, if the current row is deleted and
   * re-appended, then this method not be called again until the current row pointer is updated
   * (e.g. by invoking {@link #goToNextRow()}). When the current row is deleted and re-appended is
   * implementation-defined.
   *
   * @param row the set of zero-or-more {@code columnIndex}-keyed nullable values with which to
   *        update the current row
   * @throws IOException
   * @see TableRowWrite
   */
  public void writeRow(TableRowWrite rowWrite) throws IOException {
    checkNotNull(rowWrite, "rowWrite");
    checkState(this.hasCurrentRow(),
        format("This %s{currentLeafPageNo=%d, currentLeafCellIndex=%d, fileLength=%d} is not currently pointing to any row to which to write.",
            this.getClass().getName(),
            this.currentLeafPageNo,
            this.currentLeafCellIndex,
            this.file.length()));

    //region Locate and read "old" cell.

    final short pageOffsetOfCell = getPageOffsetOfCell(this.file, this.currentLeafPageNo, this.currentLeafCellIndex);
    final long fileOffsetOfCell = convertPageNoToFileOffset(this.currentLeafPageNo) + pageOffsetOfCell;
    file.seek(fileOffsetOfCell);
    final TableLeafCellBuffer cellBuffer = TableLeafCellBuffer.fromBytes(file);

    final int oldCellLength = cellBuffer.length();

    //endregion

    //region Create "new" updated cell in-memory.

    // Apply the column-wise updates to the "old" cell data in the cell buffer.
    for (final Map.Entry<Byte, @Nullable Object> column : rowWrite) {
      assert 1 <= column.getKey() && column.getKey() < cellBuffer.size();  // Cannot be zero because that is built-in reserved for rowId, which is not user-writable.
      assert column.getValue() == null || stream(DataType.values()).allMatch(dt -> dt.getJavaClass().isInstance(column.getValue()));

      final byte columnIndex = column.getKey();
      final byte[] data = convertToBytes(column.getValue());

      cellBuffer.set(columnIndex, data);
    }
    // The cell buffer now contains the "new" cell data.

    final int newCellLength = cellBuffer.length();

    //endregion

    // CASE 1/2 : The new cell is *not* larger, so we can just overwrite the old cell in-place.
    if (newCellLength <= oldCellLength) {
      final byte[] newCellData = cellBuffer.toBytes();
      file.seek(fileOffsetOfCell);
      file.write(newCellData);
    }

    // CASE 2/2 : The new cell *is* larger, so we have to do some reorganizing to make it fit.
    else {
      /* IMPORTANT
       *
       * The professor has verbally stated during lecture that if an UPDATE command causes a cell to
       * increase in size (of bytes), we are **not** required to either of the following:
       *
       * - Defragment the cell content area _within_ the affected page in an attempt to re-fit the
       *   now-larger cell
       * - Balance cells _across_ sibling pages (using rotations and/or splits) in order to make
       *   room for the now-large cell on one of those pages
       *
       * Rather, he both permitted as well as actively suggested simply deleting the "old" row and
       * inserting the "new" (updated) row. The professor did verbally acknowledge that this would
       * necessarily (albeit not preferably) alter the rowId of the updated row. However, he stated
       * that he would not take off grade points for that considering the extensive project scope
       * and compressed course timeline of the summer.
       *
       * In light of the professor's aforementioned statements, this second case (i.e. the updated
       * row _is_ larger), is simply implemented using the one catch-all strategy of naively
       * deleting and re-inserting the updated row.
       *
       * AUTHOR Travis C. LaGrone
       * SINCE 2019-08-01
       */

      this.removeRow();
      this.appendRow(cellBuffer);
    }
  }

  public int getCurrentMaxRowId() throws IOException {
    file.seek(0x01);
    final int currentMaxRowId = file.readInt();
    return currentMaxRowId;
  }

  private int getNextRowId() throws IOException {
    file.seek(0x01);
    int rowId = file.readInt();
    return (rowId + 1);
  }

  private int getNextRowIdInterior() throws IOException {
    file.seek(0x09);
    int rowId = file.readInt();
    return (rowId + 1);
  }

  private boolean hasCurrentLeafPageNo() {
    return currentLeafPageNo != NULL_PAGE_NO;
  }

  private boolean hasCurrentLeafCellIndex() {
    return currentLeafCellIndex != NULL_CELL_INDEX;
  }

  private boolean hasCurrentRow() throws IOException {
    assert this.hasCurrentLeafPageNo() == this.hasCurrentLeafCellIndex();

    return
        this.hasCurrentLeafPageNo() &&
        Page.exists(file, this.currentLeafPageNo) &&
        this.currentLeafCellIndex < Page.getNumberOfCells(file, currentLeafPageNo);
  }

  private int getLeftmostLeafPageNo() throws IOException {
    int pageNo = Page.getMetaDataRootPageNo(file);
    while (Page.getTablePageType(file, pageNo) == INTERIOR) {
      pageNo = Page.getLeftmostChildPageNoOfInteriorPage(file, pageNo);
    }
    assert Page.getTablePageType(file, pageNo) == LEAF;
    return pageNo;
  }

}
