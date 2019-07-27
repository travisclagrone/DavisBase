package edu.utdallas.davisbase.command;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.hash;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

public class UpdateCommand implements Command {

  private final String tableName;
  private final List<UpdateCommandColumn> columns;
  private final @Nullable CommandWhere where;

  // TODO Implement fields in UpdateCommand for whether a size change could occur (e.g. an update
  // column is nullable, an updated column is of type TEXT, etc.)

  /**
   * @param tableName the name of the table to update
   * @param columns   the unordered collection of (nonnull) column-index-value specifications that
   *                  make up this {@code UpdateCommand}, where each {@code UpdateCommandColumn}
   *                  returns a distinct value for {@link UpdateCommandColumn#getColumnIndex()} (not
   *                  null, not empty)
   * @param where     the specification of the simple {@link CommandWhere where} clause expression
   *                  for this {@code UpdateCommand}, if any (nullable)
   */
  public UpdateCommand(String tableName, List<UpdateCommandColumn> columns, @Nullable CommandWhere where) {
    checkNotNull(tableName, "tableName");
    checkNotNull(columns, "columns");
    checkArgument(!columns.isEmpty(), "columns should not be empty");
    final Set<Byte> columnIndexes = new HashSet<>(columns.size());
    for (int i = 0; i < columns.size(); i++) {
      checkNotNull(columns.get(i), format("columns.get(%d) should not be null", i));
      checkArgument(columnIndexes.add(columns.get(i).getColumnIndex()),
          format("Duplicate column index %d detected in columns at column.get(%d)",
              columns.get(i).getColumnIndex(), i));
    }

    this.tableName = tableName;
    this.columns = unmodifiableList(new ArrayList<>(columns));
    this.where = where;
  }

  /**
   * @return the name of the table to update (not null)
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * @return an unmodifiable view of the list of (nonnull) column-index-value specifications that
   *         make up this {@code UpdateCommand}, where the order of the returned list is not
   *         significant, and each {@link UpdateCommandColumn} has a distinct value for
   *         {@link UpdateCommandColumn#getColumnIndex()} (not null, not empty)
   */
  public List<UpdateCommandColumn> getColumns() {
    return columns;
  }

  /**
   * @return the simple {@link CommandWhere where} clause expression for this {@code UpdateCommand},
   *         if any (nullable)
   */
  public CommandWhere getWhere() {
    return where;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj != null && obj instanceof UpdateCommand)) {
      return false;
    }

    UpdateCommand other = (UpdateCommand) obj;
    return
        getTableName().equals(other.getTableName()) &&
        getColumns().equals(other.getColumns()) && (
          (
            getWhere() == null &&
            other.getWhere() == null
          ) || (
            getWhere() != null &&
            other.getWhere() != null &&
            getWhere().equals(other.getWhere())
          )
        );
  }

  @Override
  public int hashCode() {
    return hash(getTableName(), getColumns(), getWhere());
  }

  @Override
  public String toString() {
    return toStringHelper(UpdateCommand.class)
        .add("tableName", getTableName())
        .add("columns", getColumns())
        .add("where", getWhere())
        .toString();
  }

}
