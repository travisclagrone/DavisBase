package edu.utdallas.davisbase.command;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.util.Objects.hash;
import edu.utdallas.davisbase.DataType;

/**
 * The specification of a column reference on the left side of a
 * {@link edu.utdallas.davisbase.compiler.Compiler#compile(edu.utdallas.davisbase.representation.CommandRepresentation) compiled}
 * simple {@link CommandWhere where} clause expression.
 */
public class CommandWhereColumn {

  private final byte index;
  private final String name;
  private final DataType dataType;
  private final boolean isNullable;
  private final boolean hasIndexFile;

  /**
   * @param index        the zero-based index of the column in the source table (not negative, and
   *                     less than {@link java.lang.Byte#MAX_VALUE Byte.MAX_VALUE})
   * @param name         the name of the column in the source table (not null)
   * @param dataType     the {@link DataType} of the column in the source table (not null)
   * @param isNullable   whether the column accepts {@code null} values in the source table
   * @param hasIndexFile whether the source {@code table.column} has an index file defined for it
   *                     (e.g. as per the {@code CREATE INDEX} command)
   */
  public CommandWhereColumn(byte index, String name, DataType dataType, boolean isNullable, boolean hasIndexFile) {
    checkElementIndex(index, Byte.MAX_VALUE,
        format("index %d must be in range [0, %d)",
            index, Byte.MAX_VALUE));
    checkNotNull(name, "name");
    checkNotNull(dataType , "dataType");

    this.index = index;
    this.name = name;
    this.dataType = dataType;
    this.isNullable = isNullable;
    this.hasIndexFile = hasIndexFile;
  }

  /**
   * @return the zero-based index of the column in the source table (not negative, and less than
   *         {@link java.lang.Byte#MAX_VALUE Byte.MAX_VALUE})
   */
  public byte getIndex() {
    return index;
  }

  /**
   * @return the name of the column in the source table (not null)
   */
  public String getName() {
    return name;
  }

  /**
   * @return the {@link DataType} of the column in the source table (not null)
   */
  public DataType getDataType() {
    return dataType;
  }

  /**
   * @return whether the column in the source table accepts {@code null} values
   */
  public boolean isNullable() {
    return isNullable;
  }

  /**
   * @return whether the source {@code table.column} has an index file defined for it (e.g. as per
   *         the {@code CREATE INDEX} command)
   */
  public boolean hasIndexFile() {
    return hasIndexFile;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj != null && obj instanceof CommandWhereColumn)) {
      return false;
    }

    CommandWhereColumn other = (CommandWhereColumn) obj;
    return
        getIndex() == other.getIndex() &&
        getName().equals(other.getName()) &&
        getDataType().equals(other.getDataType()) &&
        isNullable() == other.isNullable() &&
        hasIndexFile() == other.hasIndexFile();
  }

  @Override
  public int hashCode() {
    return hash(getIndex(), getName(), getDataType(), isNullable(), hasIndexFile());
  }

  @Override
  public String toString() {
    return toStringHelper(CommandWhereColumn.class)
        .add("index", getIndex())
        .add("name", getName())
        .add("dataType", getDataType())
        .add("isNullable", isNullable())
        .add("hasIndexFile", hasIndexFile())
        .toString();
  }

}
