package edu.utdallas.davisbase.command;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.hash;

public class UpdateCommand implements Command {

  private final String tableName;
  private final UpdateCommandColumn column;
  private final @Nullable CommandWhere where;

  // TODO Implement fields in UpdateCommand for whether a size change could occur (e.g. an update
  // column is nullable, an updated column is of type TEXT, etc.)

  /**
   * @param tableName the name of the table to update
   * @param column    nonnull) column-index-value specifications that
   *                  make up this {@code UpdateCommand}, where {@code UpdateCommandColumn}
   *                  returns a distinct value for {@link UpdateCommandColumn#getColumnIndex()} (not
   *                  null, not empty)
   * @param where     the specification of the simple {@link CommandWhere where} clause expression
   *                  for this {@code UpdateCommand}, if any (nullable)
   */
  public UpdateCommand(String tableName, UpdateCommandColumn column, @Nullable CommandWhere where) {
    checkNotNull(tableName, "tableName");
    checkNotNull(column, "column");
    checkNotNull(column.getColumnIndex(), "columnIndex");

    this.tableName = tableName;
    this.column = column;
    this.where = where;
  }

  /**
   * @return the name of the table to update (not null)
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * @return (nonnull) column-index-value specification {@link UpdateCommandColumn} has a distinct value for
   *         {@link UpdateCommandColumn#getColumnIndex()} (not null, not empty)
   */
  public UpdateCommandColumn getColumn() {
    return column;
  }

  /**
   * @return the simple {@link CommandWhere where} clause expression for this {@code UpdateCommand},
   *         if any (nullable)
   */
  public @Nullable CommandWhere getWhere() {
    return where;
  }

  @Override
  @SuppressWarnings("nullness")
  public boolean equals(Object obj) {
    if (!(obj != null && obj instanceof UpdateCommand)) {
      return false;
    }

    UpdateCommand other = (UpdateCommand) obj;
    return
        getTableName().equals(other.getTableName()) &&
        getColumn().equals(other.getColumn()) &&
        Objects.equals(getWhere(), other.getWhere());
  }

  @Override
  @SuppressWarnings("nullness")
  public int hashCode() {
    return hash(getTableName(), getColumn(), getWhere());
  }

  @Override
  @SuppressWarnings("nullness")
  public String toString() {
    return toStringHelper(UpdateCommand.class)
        .add("tableName", getTableName())
        .add("column", getColumn())
        .add("where", getWhere())
        .toString();
  }

}
