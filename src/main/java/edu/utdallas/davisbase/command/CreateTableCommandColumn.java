package edu.utdallas.davisbase.command;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
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
  private final boolean isUnique;
  private final boolean isPrimaryKey;

  /**
   * @param name      the name of the column (not null)
   * @param dataType  the {@link DataType} of the column (not null)
   * @param isNotNull {@code true} if this column has the {@code NOT NULL} constraint, otherwise
   *                  {@code false}
   * @param isUnique  {@code true} if this column has the {@code UNIQUE} constraint, otherwise
   *                  {@code false}
   * @param isPrimaryKey {@code true} if this column has the {@code PRIMARY} constraint, otherwise
   *                     {@code false}
   */
  public CreateTableCommandColumn(String name, DataType dataType, boolean isNotNull, boolean isUnique, boolean isPrimaryKey) {
    checkNotNull(name, "name");
    checkNotNull(dataType, "dataType");
    checkArgument(!isPrimaryKey || (isNotNull && isUnique), "If isPrimaryKey is true, then isNotNull and isUnique must also be true.");

    this.name = name;
    this.dataType = dataType;
    this.isNotNull = isNotNull;
    this.isUnique=isUnique;
    this.isPrimaryKey=isPrimaryKey;
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

  /**
   * @return {@code true} if this column has the {@code UNIQUE} constraint, otherwise {@code false}
   */
  public boolean isUnique() {
    return isUnique;
  }

  /**
   * @return {@code true} if this column has the {@code PRIMARY KEY} constraint, otherwise {@code false}
   */
  public boolean isPrimaryKey() {
    return isPrimaryKey;
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
        .add("isUnique", isUnique)
        .add("isPrimaryKey", isPrimaryKey)
        .toString();
  }

}
