package edu.utdallas.davisbase.result;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.hash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ShowTablesResult implements Result {

  /**
   * The non-null immutable list of non-null table names. Table names are not
   * guaranteed to be sorted, unique, or consistently cased.
   */
  private final List<String> tableNames;

  /**
   * @param tableNames the collection of (nonnull) names of the tables currently defined in this
   *                   DavisBase storage instance (not null)
   */
  public ShowTablesResult(Collection<String> tableNames) {
    checkNotNull(tableNames);
    for (String tableName : tableNames) {
      checkNotNull(tableName);
    }

    // Copy the collection and then wrap it in an unmodifiable view to ensure immutability.
    this.tableNames = unmodifiableList(new ArrayList<>(tableNames));
  }

  /**
   * @return an unmodifiable list of the (nonnull) names of the tables defined in this DavisBase
   *         storage instance at the time this {@link ShowTablesResult} was created (not null)
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
    return getTableNames().equals(other.getTableNames());
  }

  @Override
  public int hashCode() {
    return hash(getTableNames());
  }

  @Override
  public String toString() {
    return toStringHelper(ShowTablesResult.class)
        .add("tableNames", Arrays.toString(getTableNames().toArray()))
        .toString();
  }

}
