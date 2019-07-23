package edu.utdallas.davisbase.command;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.hash;
import edu.utdallas.davisbase.DataType;

/**
 * A column schema for a {@code CREATE TABLE} command.
 */
public class CreateTableCommandColumn {

  private final String name;
  private final DataType dataType;
  private final boolean isNotNull;
  // COMBAK Implement CreateTableCommandColumn.isUnique
  // COMBAK Implement CreateTableCommandColumn.isPrimaryKey

  /**
   * @param name      the name of the column (not null)
   * @param dataType  the {@link DataType} of the column (not null)
   * @param isNotNull {@code true} if this column has the {@code NOT NULL} constraint, otherwise
   *                  {@code false}
   */
  public CreateTableCommandColumn(String name, DataType dataType, boolean isNotNull) {
    checkNotNull(name, "name");
    checkNotNull(dataType, "dataType");

    this.name = name;
    this.dataType = dataType;
    this.isNotNull = isNotNull;
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

  /**
   * @return {@code true} if this column has the {@code NOT NULL} constraint, otherwise {@code false}
   */
  public boolean isNotNull() {
    return isNotNull;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj != null && obj instanceof CreateTableCommandColumn)) {
      return false;
    }

    CreateTableCommandColumn other = (CreateTableCommandColumn) obj;
    return
        getName().equals(other.getName()) &&
        getDataType().equals(other.getDataType()) &&
        isNotNull() == other.isNotNull();
  }

  @Override
  public int hashCode() {
    return hash(getName(), getDataType(), isNotNull());
  }

  @Override
  public String toString() {
    return toStringHelper(CreateTableCommandColumn.class)
        .add("name", getName())
        .add("dataType", getDataType())
        .add("isNotNull", isNotNull())
        .toString();
  }

}
