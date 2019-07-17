package edu.utdallas.davisbase.result;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.hash;

public class DropTableResult implements Result {

  private final String tableName;

  /**
   * @param tableName the name of the table that was dropped
   */
  public DropTableResult(String tableName) {
    checkNotNull(tableName);

    this.tableName = tableName;
  }

  /**
   * @return the name of the table that was dropped
   */
  public String getTableName() {
    return tableName;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj != null && obj instanceof DropTableResult)) {
      return false;
    }

    DropTableResult other = (DropTableResult) obj;
    return getTableName().equals(other.getTableName());
  }

  @Override
  public int hashCode() {
    return hash(getTableName());
  }

  @Override
  public String toString() {
    return toStringHelper(DropTableResult.class)
        .add("tableName", getTableName())
        .toString();
  }

}
