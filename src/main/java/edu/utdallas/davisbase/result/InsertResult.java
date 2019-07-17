package edu.utdallas.davisbase.result;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.util.Objects.hash;

public class InsertResult implements Result {

  private final String tableName;
  private final int rowsInserted;

  /**
   * @param tableName    the name of the table into which rows were inserted (not null)
   * @param rowsInserted the count of rows that were inserted (not negative)
   */
  public InsertResult(String tableName, int rowsInserted) {
    checkNotNull(tableName);
    checkArgument(0 <= rowsInserted,
        format("rowsInserted must be nonnegative, but is %d",
            rowsInserted));

    this.tableName = tableName;
    this.rowsInserted = rowsInserted;
  }

  /**
   * @return the name of the table into which rows were inserted (not null)
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * @return the count of rows that were inserted (not negative)
   */
  public int getRowsInserted() {
    return rowsInserted;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj != null && obj instanceof InsertResult)) {
      return false;
    }

    InsertResult other = (InsertResult) obj;
    return
        getTableName().equals(other.getTableName()) &&
        getRowsInserted() == other.getRowsInserted();
  }

  @Override
  public int hashCode() {
    return hash(getTableName(), getRowsInserted());
  }

  @Override
  public String toString() {
    return toStringHelper(InsertResult.class)
        .add("tableName", getTableName())
        .add("rowsInserted", getRowsInserted())
        .toString();
  }

}
