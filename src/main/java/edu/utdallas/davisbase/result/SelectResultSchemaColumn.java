package edu.utdallas.davisbase.result;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.hash;

import edu.utdallas.davisbase.DataType;

/**
 * A column specification for a {@link SelectResultSchema}.
 */
public class SelectResultSchemaColumn {

  private final String name;
  private final DataType dataType;

  /**
   * @param name     the name of the column in the {@link SelectResultSchema} (not null)
   * @param dataType the {@link DataType} of the column in the {@link SelectResultSchema} (not null)
   */
  public SelectResultSchemaColumn(String name, DataType dataType) {
    checkNotNull(name);
    checkNotNull(dataType);

    this.name = name;
    this.dataType = dataType;
  }

  /**
   * @return the name of the column (not null)
   */
  public String getName() {
    return name;
  }

  /**
   * @return the {@link DataType} of the column (not null)
   */
  public DataType getDataType() {
    return dataType;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj != null && obj instanceof SelectResultSchemaColumn)) {
      return false;
    }

    SelectResultSchemaColumn other = (SelectResultSchemaColumn) obj;
    return
      getName().equals(other.getName()) &&
      getDataType().equals(other.getDataType());
  }

  @Override
  public int hashCode() {
    return hash(getName(), getDataType());
  }

  @Override
  public String toString() {
    return toStringHelper(SelectResultSchemaColumn.class)
        .add("name", getName())
        .add("dataType", getDataType())
        .toString();
  }

}
