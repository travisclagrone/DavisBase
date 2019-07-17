package edu.utdallas.davisbase.result;

import static java.util.Objects.hash;
import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

public class CreateTableResult implements Result {

  private final String tableName;

  /**
   * @param tableName the name of the table that was created (not null)
   */
  public CreateTableResult(String tableName) {
    checkNotNull(tableName);

    this.tableName = tableName;
  }

  /**
   * @return the name of the table that was created (not null)
   */
  public String getTableName() {
    return tableName;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj != null && obj instanceof CreateTableResult)) {
      return false;
    }

    CreateTableResult other = (CreateTableResult) obj;
    return getTableName().equals(other.getTableName());
  }

  @Override
  public int hashCode() {
    return hash(getTableName());
  }

  @Override
  public String toString() {
    return toStringHelper(CreateTableResult.class)
        .add("tableName", getTableName())
        .toString();
  }

}
