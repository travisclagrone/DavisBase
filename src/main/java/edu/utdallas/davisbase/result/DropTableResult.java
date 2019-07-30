package edu.utdallas.davisbase.result;

import static java.util.Objects.hash;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

public class DropTableResult implements Result {

  // TODO Add field `indexNames: List<String>` (not null, no element null, may be empty) that lists
  // the names of the indexes that were on the dropped table; those indexes should have been dropped
  // as well.

  // QUESTION Are indexes in DavisBase named? Or are they identified by table name plus column name?

  private final String tableName;

  /**
   * @param tableName the name of the table that was dropped (not null)
   */
  public DropTableResult(String tableName) {
    checkNotNull(tableName);

    this.tableName = tableName;
  }

  /**
   * @return the name of the table that was dropped (not null)
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
