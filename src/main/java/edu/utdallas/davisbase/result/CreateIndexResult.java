package edu.utdallas.davisbase.result;

import static java.util.Objects.hash;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

public class CreateIndexResult implements Result {

  private final String tableName;
  private final String columnName;

  /**
   * @param tableName the name of the table on which the index was created (not null)
   * @param columnName the name of the column on which the index was created (not null)
   */
  public CreateIndexResult(String tableName, String columnName) {
    checkNotNull(tableName);
    checkNotNull(columnName);

    this.tableName = tableName;
    this.columnName = columnName;
  }

  /**
   * @return the name of the table on which the index was created (not null)
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * @return the name of the column on which the index was created (not null)
   */
  public String getColumnName() {
    return columnName;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj != null && obj instanceof CreateIndexResult)) {
      return false;
    }

    CreateIndexResult other = (CreateIndexResult) obj;
    return
        getTableName().equals(other.getTableName()) &&
        getColumnName().equals(other.getColumnName());
  }

  @Override
  public int hashCode() {
    return hash(getTableName(), getColumnName());
  }

  @Override
  public String toString() {
    return toStringHelper(CreateIndexResult.class)
        .add("tableName", getTableName())
        .add("columnName", getColumnName())
        .toString();
  }

}
