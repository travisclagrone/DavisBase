package edu.utdallas.davisbase.command;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.hash;
import java.util.ArrayList;
import java.util.List;

public class CreateTableCommand implements Command {

  private final String tableName;
  private final List<CreateTableCommandColumn> columnSchemas;

  /**
   * @param tableName     the name of the table to create (not null)
   * @param columnSchemas the ordered list of (nonnull) column schemas that make up this table's
   *                      schema, where the ordering of the list is the ordering of the columns in
   *                      the table; SHOULD include the index-zero default {@code rowId} column
   *                      (not null, not empty)
   */
  public CreateTableCommand(String tableName, List<CreateTableCommandColumn> columnSchemas) {
    checkNotNull(tableName, "tableName");
    checkNotNull(columnSchemas, "columnSchemas");
    for (int i = 0; i < columnSchemas.size(); i++) {
      checkNotNull(columnSchemas.get(i), "columnSchemas.get(%d)", i);
    }

    this.tableName = tableName;
    // Copy to a new list for encapsulation, and wrap in an unmodifiable view for immutability.
    this.columnSchemas = unmodifiableList(new ArrayList<>(columnSchemas));
  }

  /**
   * @return the name of the table to create (not null)
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * @return the ordered list of (nonnull) column schemas that make up this table's schema, where
   *         the ordering of the list is the ordering of the columns in the table; SHOULD include
   *         the index-zero default {@code rowId} column (not null, not empty)
   */
  public List<CreateTableCommandColumn> getColumnSchemas() {
    return columnSchemas;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj != null && obj instanceof CreateTableCommand)) {
      return false;
    }

    CreateTableCommand other = (CreateTableCommand) obj;
    return
        getTableName().equals(other.getTableName()) &&
        getColumnSchemas().equals(other.getColumnSchemas());
  }

  @Override
  public int hashCode() {
    return hash(getTableName(), getColumnSchemas());
  }

  @Override
  public String toString() {
    return toStringHelper(CreateTableCommand.class)
        .add("tableName", getTableName())
        .add("columnSchemas", getColumnSchemas())
        .toString();
  }

}
