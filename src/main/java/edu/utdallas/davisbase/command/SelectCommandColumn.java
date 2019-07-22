package edu.utdallas.davisbase.command;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.hash;
import edu.utdallas.davisbase.DataType;

/**
 * A column specification for the select clause of a compiled {@link SelectCommand}.
 */
public class SelectCommandColumn {

  private final byte index;
  private final String name;
  private final DataType dataType;

  /**
   * @param index    the index of the column in the source table's schema; {@code rowId} is always
   *                 {@code 0}), and everything else starts at {@code 1} (not negative, less than
   *                 {@link java.lang.Byte#MAX_VALUE Byte.MAX_VALUE})
   * @param name     the name of the column in the source table's schema (not null)
   * @param dataType the {@link DataType} of the column in the source table's schema (not null)
   */
  public SelectCommandColumn(byte index, String name, DataType dataType) {
    checkElementIndex(index, Byte.MAX_VALUE);  // exclusive upper bound
    checkNotNull(name);
    checkNotNull(dataType);

    this.index = index;
    this.name = name;
    this.dataType = dataType;
  }

  /**
   * @return the zero-based index of the column in the source table's schema, where index {@code 0}
   *         is always {@code rowId} (not negative, less than
   *         {@link java.lang.Byte#MAX_VALUE Byte.MAX_VALUE})
   */
  public byte getIndex() {
    return index;
  }

  /**
   * @return the name of the column in the source table's schema (not null)
   */
  public String getName() {
    return name;
  }

  /**
   * @return the {@link DataType} of the column in the source table's schema (not null)
   */
  public DataType getDataType() {
    return dataType;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj != null && obj instanceof SelectCommandColumn)) {
      return false;
    }

    SelectCommandColumn other = (SelectCommandColumn) obj;
    return
        getIndex() == other.getIndex() &&
        getName().equals(other.getName()) &&
        getDataType().equals(other.getDataType());
  }

  @Override
  public int hashCode() {
    return hash(getIndex(), getName(), getDataType());
  }

  @Override
  public String toString() {
    return toStringHelper(SelectCommandColumn.class)
        .add("index", getIndex())
        .add("name", getName())
        .add("dataType", getDataType())
        .toString();
  }

}
