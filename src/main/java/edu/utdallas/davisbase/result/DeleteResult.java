package edu.utdallas.davisbase.result;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.util.Objects.hash;

public class DeleteResult implements Result {

  private final String tableName;
  private final int rowsDeleted;

  /**
   * @param tableName   the name of the table from which rows were deleted (not null)
   * @param rowsDeleted the count of rows that were deleted (not negative)
   */
  public DeleteResult(String tableName, int rowsDeleted) {
    checkNotNull(tableName);
    checkArgument(0 <= rowsDeleted, format("rowsDeleted must be nonnegative, but is %d", rowsDeleted));

    this.tableName = tableName;
    this.rowsDeleted = rowsDeleted;
  }

  /**
   * @return the name of the table from which rows were deleted (not null)
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * @return the count of rows deleted (not negative)
   */
  public int getRowsDeleted() {
    return rowsDeleted;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj != null && obj instanceof DeleteResult)) {
      return false;
    }

    DeleteResult other = (DeleteResult) obj;
    return
      getTableName().equals(other.getTableName()) &&
      getRowsDeleted() == other.getRowsDeleted();
  }

  @Override
  public int hashCode() {
    return hash(getTableName(), getRowsDeleted());
  }

  @Override
  public String toString() {
    return toStringHelper(DeleteResult.class)
        .add("tableName", getTableName())
        .add("rowsDeleted", getRowsDeleted())
        .toString();
  }

}
