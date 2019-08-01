package edu.utdallas.davisbase.storage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static java.util.Arrays.copyOf;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A semi-structured, mutable, in-memory representation of a cell in a
 * {@link TablePageType#LEAF LEAF} {@link Page} of a {@link TableFile}. Intended for use in the
 * following three-part workflow, such as is used by {@link TableFile#writeRow(TableRowWrite)}:
 * <ol>
 *   <li>Partially deserialize a pre-existing cell</li>
 *   <li>Perform low-level manipulations on the cell</li>
 *   <li>Re-serialize the modified cell</li>
 * </ol>
 */
class TableLeafCellBuffer implements Iterable<byte[]> {

  private final List<byte[]> datas;  // spell-checker:ignore datas

  public TableLeafCellBuffer() {
    this.datas = new ArrayList<>();
  }

  private TableLeafCellBuffer(int initialSize) {
    this.datas = new ArrayList<>(initialSize);
  }

  /**
   * Appends a new column of data to this {@code TableLeafCellBuffer}.
   *
   * @param data the data to append as a new column (not null, but may be empty)
   * @see java.util.List#add(Object)
   */
  public void add(byte[] data) {
    checkNotNull(data, "data");
    checkArgument(data.length <= Byte.MAX_VALUE,
        format("data may not be longer than %d bytes",
            Byte.MAX_VALUE));
    checkState(datas.size() < Byte.MAX_VALUE,
        format("Cannot accept more than %d data columns",
            Byte.MAX_VALUE));

    final byte[] internalArray = copyOf(data, data.length);
    datas.add(internalArray);
  }

  /**
   * Inserts a new column of data into this {@code TableLeafCellBuffer} at the specified
   * {@code columnIndex}.
   * <p>
   * Does <b>not</b> overwrite the pre-existing column of data (if any) at the specified
   * {@code columnIndex}, but rather shifts every data array at a column index equal to or greater
   * than the given {@code columnIndex} to the next greatest column index.
   *
   * @param columnIndex
   * @see java.util.List#add(int, Object)
   */
  public void add(byte columnIndex, byte[] data) {
    checkNotNull(data, "data");
    checkArgument(data.length <= Byte.MAX_VALUE,
        format("data may not be longer than %d bytes",
            Byte.MAX_VALUE));
    checkState(datas.size() + 1 <= Byte.MAX_VALUE,
        format("Cannot accept more than %d elements",
            Byte.MAX_VALUE));

    final byte[] internalArray = copyOf(data, data.length);
    datas.add(columnIndex, internalArray);
  }

  /**
   * @see java.util.List#get(int)
   */
  public byte[] get(byte columnIndex) {
    final byte[] internalArray = datas.get(columnIndex);
    final byte[] externalArray = copyOf(internalArray, internalArray.length);
    return externalArray;
  }

  /**
   * @see java.util.List#remove(int)
   */
  public byte[] remove(byte columnIndex) {
    return datas.remove((int) columnIndex);
  }

  /**
   * @see java.util.List#set(int, Object)
   */
  public byte[] set(byte columnIndex, byte[] data) {
    checkNotNull(data, "data");
    checkArgument(data.length <= Byte.MAX_VALUE,
        format("data may not be longer than %d bytes",
            Byte.MAX_VALUE));

    final byte[] internalArray = copyOf(data, data.length);
    return datas.set(columnIndex, internalArray);
  }

  /**
   * @see java.util.List#isEmpty()
   * @return {@code true} if-and-only-if this {@code TableLeafCellBuffer} does not contain any data
   *         instances; empty-but-existent data instances entail <i>not</i> empty
   */
  public boolean isEmpty() {
    return datas.isEmpty();
  }

  /**
   * @see java.util.List#size()
   * @return the count of the column-wise data instances that this {@code TableLeafCellBuffer}
   *         contains; empty-but-existent data instances are counted
   */
  public byte size() {
    assert datas.size() <= Byte.MAX_VALUE;

    return (byte) datas.size();
  }

  /**
   * @return the length of the byte array that {@link #toBytes()} would return given the current
   *         state of this {@code TableLeafCellBuffer}
   */
  public int length() {
    int length = 0;

    // Cell Header
    length += 1;  // "Number of Columns" byte field
    length += datas.size();  // "List of Data Sizes" byte array field

    // Cell Body
    for (byte[] data : this.datas) {
      length += data.length;  // "Column Data Value" payload for each column
    }

    assert length == this.toBytes().length;
    return length;
  }

  @Override
  public Iterator<byte[]> iterator() {
    final List<byte[]> externalArrays = new ArrayList<>(this.size());
    for (byte[] internalArray : this.datas) {
      final byte[] externalArray = copyOf(internalArray, internalArray.length);
      externalArrays.add(externalArray);
    }

    return externalArrays.iterator();
  }

  /**
   * @return the binary representation of the current state of this {@code TableLeafCellBuffer} as a
   *         complete/atomic cell (including the header) for a {@link TablePageType#LEAF LEAF}
   *         {@link Page} in a {@link TableFile}
   *
   * @apiNote Does <b>not</b> guarantee that the length of the binary representation is less than
   *          the size of a page.
   */
  public byte[] toBytes() {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();  // spell-checker:ignore baos

    // Write the number of columns in the cell header.
    baos.write(this.size());

    // Write the list of column data value sizes in the cell header.
    for (byte[] data : this.datas) {
      baos.write(data.length);
    }

    // Write the list of column data values in the cell body.
    for (byte[] data : this.datas) {
      baos.write(data, 0, data.length);
    }

    final byte[] cell = baos.toByteArray();
    return cell;
  }

  /**
   * @param input the {@link DataInput} from which to read the bytes (not null, open, in a valid
   *              state, and not empty)
   * @return the {@code TableLeafCellBuffer} representation of the {@link TableFile}
   *         {@link TablePageType#LEAF LEAF} {@link Page} cell beginning at the current position of
   *         the {@code input} when passed to this method
   * @throws IOException
   */
  public static TableLeafCellBuffer fromBytes(DataInput input) throws IOException {
    assert input != null;  // Don't need to actually do a check here since input is dereferenced almost immediately anyway.

    final byte columnCount = input.readByte();
    assert 1 <= columnCount && columnCount <= Byte.MAX_VALUE;  // At least one since there will should always be the built-in rowId at columIndex zero.

    final List<Byte> dataSizes = new ArrayList<>(columnCount);
    for (int i = 0; i < columnCount; i++) {
      final byte dataSize = input.readByte();
      assert 0 <= dataSize && dataSize <= Byte.MAX_VALUE;  // Zero dataSize denotes null.
      dataSizes.add(dataSize);
    }

    final TableLeafCellBuffer cellBuffer = new TableLeafCellBuffer(columnCount);
    for (byte dataSize : dataSizes) {
      final byte[] data = new byte[dataSize];
      input.readFully(data);
      cellBuffer.add(data);
    }

    return cellBuffer;
  }

}
