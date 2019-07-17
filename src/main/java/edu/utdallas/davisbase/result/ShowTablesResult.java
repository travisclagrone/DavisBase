package edu.utdallas.davisbase.result;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ShowTablesResult implements Result {

  /**
   * The non-null immutable list of non-null table names. Table names are not
   * guaranteed to be sorted, unique, or consistently cased.
   */
  private final List<String> tableNames;

  /**
   * @param tableNames the non-null collection of non-null table names
   */
  public ShowTablesResult(Collection<String> tableNames) {
    checkNotNull(tableNames);
    for (String tableName : tableNames) {
      checkNotNull(tableName);
    }

    // Copy the collection and then wrap it in an unmodifiable view to ensure immutability.
    this.tableNames = Collections.unmodifiableList(new ArrayList<>(tableNames));
  }

  /**
   * @return the non-null unmodifiable list of non-null table names
   */
  public List<String> getTableNames() {
    return tableNames;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj != null && obj instanceof ShowTablesResult)) {
      return false;
    }

    ShowTablesResult other = (ShowTablesResult) obj;
    return tableNames.equals(other.tableNames);
  }

  @Override
  public int hashCode() {
    return tableNames.hashCode();
  }

  @Override
  public String toString() {
    return toStringHelper(this)
        .add("tableNames", Arrays.toString(tableNames.toArray()))
        .toString();
  }

}
