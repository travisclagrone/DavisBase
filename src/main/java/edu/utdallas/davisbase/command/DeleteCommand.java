package edu.utdallas.davisbase.command;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.hash;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DeleteCommand implements Command {

  private final String tableName;
  private final @Nullable CommandWhere where;

  public DeleteCommand(String tableName, @Nullable CommandWhere where) {
    checkNotNull(tableName, "tableName");

    this.tableName = tableName;
    this.where = where;
  }

  /**
   * @return the name of the table from which to delete (not null)
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * @return the specification of the simple {@code WHERE} clause expression of this command, if any
   *         (nullable)
   */
  public @Nullable CommandWhere getWhere() {
    return where;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj != null && obj instanceof DeleteCommand)) {
      return false;
    }

    DeleteCommand other = (DeleteCommand) obj;
    return
        getTableName().equals(other.getTableName()) && (
          (
            getWhere() == null &&
            other.getWhere() == null
          ) || (
            getWhere() != null &&
            other.getWhere() != null
            && getWhere().equals(other.getWhere())
          )
        );
  }

  @Override
  public int hashCode() {
    return hash(getTableName(), getWhere());
  }

  @Override
  public String toString() {
    return toStringHelper(DeleteCommand.class)
        .add("tableName", getTableName())
        .add("where", getWhere())
        .toString();
  }

}
