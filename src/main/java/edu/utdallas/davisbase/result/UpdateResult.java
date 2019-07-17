package edu.utdallas.davisbase.result;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.util.Objects.hash;

public class UpdateResult implements Result {

  private final String tableName;
  private final int rowsUpdated;

  /**
   * @param tableName   the name of the table that was updated (not null)
   * @param rowsUpdated the count of rows updated (not negative)
   */
  public UpdateResult(String tableName, int rowsUpdated) {
    checkNotNull(tableName);
    checkArgument(0 <= rowsUpdated,
        format("rowsUpdated must be nonnegative, but is %d",
            rowsUpdated));

    this.tableName = tableName;
    this.rowsUpdated = rowsUpdated;
  }

  /**
   * @return the name of the table that was updated (not null)
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * @return the count of rows updated (not negative)
   */
  public int getRowsUpdated() {
    return rowsUpdated;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj != null && obj instanceof UpdateResult)) {
      return false;
    }

    UpdateResult other = (UpdateResult) obj;
    return
        getTableName().equals(other.getTableName()) &&
        getRowsUpdated() == other.getRowsUpdated();
  }

  @Override
  public int hashCode() {
    return hash(getTableName(), getRowsUpdated());
  }

  @Override
  public String toString() {
    return toStringHelper(UpdateResult.class)
        .add("tableName", getTableName())
        .add("rowsUpdated", getRowsUpdated())
        .toString();
  }

}
