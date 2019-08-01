package edu.utdallas.davisbase.storage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static edu.utdallas.davisbase.storage.DataUtils.convertToBytes;
import static edu.utdallas.davisbase.storage.Page.convertPageNoToFileOffset;
import static edu.utdallas.davisbase.storage.Page.getPageOffsetOfCell;
import static edu.utdallas.davisbase.storage.TablePageType.INTERIOR;
import static edu.utdallas.davisbase.storage.TablePageType.LEAF;
import static java.lang.String.format;
import static java.util.Arrays.stream;

import edu.utdallas.davisbase.YearUtils;
import edu.utdallas.davisbase.DataType;
import edu.utdallas.davisbase.NotImplementedException;
import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.Map;
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

  private static int NULL_LEAF_PAGE_NO = -1;
  private static short NULL_LEAF_CELL_INDEX = -1;

  protected final RandomAccessFile file;
  private int currentLeafPageNo = NULL_LEAF_PAGE_NO;
  private short currentLeafCellIndex = NULL_LEAF_CELL_INDEX;

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
    int currentPageNo;
    currentPageNo = (int) (this.file.length() / Page.PAGE_SIZE);
    long pageOffset = (currentPageNo - 1) * Page.PAGE_SIZE;
    file.seek(pageOffset);
    byte pageType = file.readByte();

    int rightPageSeekPoint = (int) (pageOffset + 6);
    file.seek(rightPageSeekPoint);
    int rightPageNo = file.readInt();

    boolean searchFlag = false;

    while (rightPageNo != -1) {
      searchFlag = true;
      rightPageSeekPoint = ((rightPageNo - 1) * Page.PAGE_SIZE) + 6;
      file.seek(rightPageSeekPoint);
      rightPageNo = file.readInt();
      file.seek(rightPageSeekPoint);
    }
    if (searchFlag) {

      pageOffset = rightPageSeekPoint - 6;
      currentPageNo = (int) ((pageOffset / Page.PAGE_SIZE) + 1);
      searchFlag = false;
    }

    int noOfColumns = tableRowBuilder.getNoOfValues();
    noOfColumns = noOfColumns + 1;
    int[] columnSizeArray = new int[noOfColumns];
    int payLoad = 4;// since rowId is not a part of table row builder
    columnSizeArray[0] = payLoad;
    Object data;
    for (int i = 1; i < columnSizeArray.length; i++) {
      data = tableRowBuilder.getValueAt(i - 1);
      switch (data.getClass().getSimpleName()) {
        case "Integer":
          columnSizeArray[i] = 4;
          break;

        case "String":

          columnSizeArray[i] = data.toString().length();
          break;
        case "Byte":
          columnSizeArray[i] = 1;
          break;
        case "Short":
          columnSizeArray[i] = 2;
          break;
        case "Long":
          columnSizeArray[i] = 8;
          break;
        case "Float":
          columnSizeArray[i] = 4;
          break;
        case "Double":
          columnSizeArray[i] = 8;
          break;
        case "Year":
          columnSizeArray[i] = 1;
          break;
        case "LocalTime":
          columnSizeArray[i] = 4;
          break;
        case "LocalDateTime":
          columnSizeArray[i] = 8;
          break;
        case "LocalDate":
          columnSizeArray[i] = 8;
          break;
        case "":
          columnSizeArray[i] = 0;
          break;

        default:
          columnSizeArray[i] = 0;
          break;

      }
      payLoad = (short) (payLoad + columnSizeArray[i]);
    }

    int totalSpaceRequired;

    file.seek(pageOffset);
    pageType = file.readByte();
    Boolean splitFlag = false;

    totalSpaceRequired = (1 + columnSizeArray.length + payLoad);
    boolean overflowFlag = checkPagesize(totalSpaceRequired, currentPageNo);
    if (overflowFlag) {
      if (pageType == 0x05) {
        currentPageNo = Page.splitInteriorPage(file, currentPageNo);
      } else if (pageType == 0x0D) {
        currentPageNo = Page.splitLeafPage(file, currentPageNo);
        splitFlag = true;
      }
    }
    if (splitFlag) {

      pageOffset = (currentPageNo - 1) * Page.PAGE_SIZE;
      splitFlag = false;
    }
    file.seek(pageOffset);
    pageType = file.readByte();
    long seekOffset = pageOffset + 3;
    file.seek(seekOffset);
    short cellOffset = file.readShort();
    if (cellOffset == 0) {
      cellOffset = (short) (Page.PAGE_SIZE);
    }

    short dataEntryPoint = (short) (cellOffset - totalSpaceRequired);

    int rowId = 0;

    // for (int i = 0; i < columnSizeArray.length; i++) {
    // file.writeByte(columnSizeArray[i]);
    // }

    if (pageType == 0x05) {
      rowId = getNextRowIdInterior();
      file.seek(0x09);
      file.writeInt(rowId);
    } else if (pageType == 0x0D) {
      rowId = getNextRowId();
      file.seek(0x01);
      file.writeInt(rowId);
    }

    file.seek(pageOffset + dataEntryPoint);
    file.writeByte(noOfColumns);

    for (int i = 0; i < columnSizeArray.length; i++) {
      file.writeByte(columnSizeArray[i]);
    }
    file.writeInt(rowId);
    file.write(tableRowBuilder.toBytes());

    file.seek(pageOffset + 1);
    int noOfCellsInPage = file.readShort();
    noOfCellsInPage = noOfCellsInPage + 1;
    file.seek(pageOffset + 1);
    file.writeShort(noOfCellsInPage);

    file.seek(pageOffset + 3);
    file.writeShort(dataEntryPoint);// writing Offset

    file.seek(pageOffset + 16 + 2 * (noOfCellsInPage - 1));
    file.writeShort(dataEntryPoint);

    // Update table meta data with rowId
    // file.seek(0x01);
    // file.writeInt(rowId);

  }

  private boolean checkPagesize(int sizeRequired, int currentPageNo) {
    try {
      file.seek((currentPageNo - 1) * Page.PAGE_SIZE);
      byte pageType = file.readByte();
      if (pageType == 0x0D) {

        this.file.seek((currentPageNo - 1) * Page.PAGE_SIZE + 1);
        short noOfRecords = this.file.readShort();

        this.file.seek((currentPageNo - 1) * Page.PAGE_SIZE + 3);
        short startofCellConcent = file.readShort();
        if (startofCellConcent == 0) {
          startofCellConcent = (short) (Page.PAGE_SIZE);
        }
        short arryLastEntry = (short) (16 + (noOfRecords * 2));
        if ((startofCellConcent - arryLastEntry - 1) < sizeRequired) {
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

  // TODO How account for null values?
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
    // TODO Implement TableFile.removeRow()
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

      // TODO Implement case 2/2
    }

    /*
    else:
      removeRow()
      appendRow()
      this.isCurrentRowRemoved = true
    */

    // TODO Implement TableFile.writeRow(TableRowWrite)
    throw new NotImplementedException("edu.utdallas.davisbase.storage.TableFile#writeRow(TableRowWrite)");
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
    return currentLeafPageNo != NULL_LEAF_PAGE_NO;
  }

  private boolean hasCurrentLeafCellIndex() {
    return currentLeafCellIndex != NULL_LEAF_CELL_INDEX;
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
