package edu.utdallas.davisbase.storage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static java.util.Arrays.copyOf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
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

  private final List<byte[]> binaryValues;

  public TableLeafCellBuffer() {
    this.binaryValues = new ArrayList<>();
  }

  private TableLeafCellBuffer(int initialSize) {
    this.binaryValues = new ArrayList<>(initialSize);
  }

  /**
   * @see java.util.List#add(Object)
   */
  public boolean add(byte[] binaryValue) {
    checkNotNull(binaryValue, "binaryValue");
    checkArgument(binaryValue.length <= Byte.MAX_VALUE,
        format("binaryValue may not be longer than %d bytes",
            Byte.MAX_VALUE));
    checkState(binaryValues.size() + 1 <= Byte.MAX_VALUE,
        format("Cannot accept more than %d elements",
            Byte.MAX_VALUE));

    final byte[] privateBinaryValue = copyOf(binaryValue, binaryValue.length);
    return binaryValues.add(privateBinaryValue);
  }

  /**
   * @see java.util.List#add(int, Object)
   */
  public void add(byte columnIndex, byte[] binaryValue) {
    checkNotNull(binaryValue, "binaryValue");
    checkArgument(binaryValue.length <= Byte.MAX_VALUE,
        format("binaryValue may not be longer than %d bytes",
            Byte.MAX_VALUE));
    checkState(binaryValues.size() + 1 <= Byte.MAX_VALUE,
        format("Cannot accept more than %d elements",
            Byte.MAX_VALUE));

    binaryValues.add(columnIndex, binaryValue);
  }

  /**
   * @see java.util.List#get(int)
   */
  public byte[] get(byte columnIndex) {
    final byte[] privateBinaryValue = binaryValues.get(columnIndex);
    final byte[] publicBinaryValue = copyOf(privateBinaryValue, privateBinaryValue.length);
    return publicBinaryValue;
  }

  /**
   * @see java.util.List#remove(int)
   */
  public byte[] remove(byte columnIndex) {
    return binaryValues.remove((int) columnIndex);
  }

  /**
   * @see java.util.List#set(int, Object)
   */
  public byte[] set(byte columnIndex, byte[] binaryValue) {
    checkNotNull(binaryValue, "binaryValue");
    checkArgument(binaryValue.length <= Byte.MAX_VALUE,
        format("binaryValue may not be longer than %d bytes",
            Byte.MAX_VALUE));

    return binaryValues.set(columnIndex, binaryValue);
  }

  /**
   * @see java.util.List#isEmpty()
   */
  public boolean isEmpty() {
    return binaryValues.isEmpty();
  }

  /**
   * @see java.util.List#size()
   */
  public byte size() {
    assert binaryValues.size() <= Byte.MAX_VALUE;

    return (byte) binaryValues.size();
  }

  /**
   * @return the length of the byte array that {@link #toBytes()} would return given the current
   *         state of this {@code TableLeafCellBuffer}
   */
  public int length() {
    return this.toBytes().length;
  }

  @Override
  public Iterator<byte[]> iterator() {
    final List<byte[]> publicBinaryValues = new ArrayList<>(this.size());
    for (byte[] privateBinaryValue : this.binaryValues) {
      final byte[] publicBinaryValue = copyOf(privateBinaryValue, privateBinaryValue.length);
      publicBinaryValues.add(publicBinaryValue);
    }

    return publicBinaryValues.iterator();
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
    for (byte[] binaryValue : this.binaryValues) {
      baos.write(binaryValue.length);
    }

    // Write the list of column data values in the cell body.
    for (byte[] binaryValue : this.binaryValues) {
      baos.write(binaryValue, 0, binaryValue.length);
    }

    final byte[] cell = baos.toByteArray();
    return cell;
  }

  /**
   * @param file             the (open) file from which to read the bytes (not null)
   * @param fileOffsetOfCell the (valid) position in the file at which to begin reading bytes
   * @return the {@code TableLeafCellBuffer} representation of the {@link TableFile}
   *         {@link TablePageType#LEAF LEAF} {@link Page} cell beginning at the given position
   * @throws IOException
   */
  public static TableLeafCellBuffer fromBytes(RandomAccessFile file, long fileOffsetOfCell) throws IOException {
    checkNotNull(file, "file");
    checkArgument(file.getChannel().isOpen(), "file is not open");
    checkArgument(0 <= fileOffsetOfCell, "fileOffsetOfCell may not be negative");
    checkArgument(fileOffsetOfCell < file.length(), "fileOffsetOfCell must be less than the file length " + file.length());

    file.seek(fileOffsetOfCell);

    final byte columnCount = file.readByte();
    assert 0 <= columnCount && columnCount <= Byte.MAX_VALUE;

    final List<Byte> dataSizes = new ArrayList<>(columnCount);
    for (int i = 0; i < columnCount; i++) {
      final byte dataSize = file.readByte();
      assert 0 <= dataSize && dataSize <= Byte.MAX_VALUE;
      dataSizes.add(dataSize);
    }

    final TableLeafCellBuffer cellBuffer = new TableLeafCellBuffer(columnCount);
    for (byte dataSize : dataSizes) {
      final byte[] binaryValue = new byte[dataSize];
      final int countRead = file.read(binaryValue);
      assert countRead == binaryValue.length;
      cellBuffer.add(binaryValue);
    }

    return cellBuffer;
  }

}
