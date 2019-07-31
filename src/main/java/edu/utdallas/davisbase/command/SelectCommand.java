package edu.utdallas.davisbase.command;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.hash;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SelectCommand implements Command {

  private final String tableName;
  private final List<SelectCommandColumn> selectClauseColumns;
  private final @Nullable CommandWhere where;

  /**
   * @param tableName           the name of the table being selected FROM (not null)
   * @param selectClauseColumns the ordered list of (nonnull) column specifications in the SELECT
   *                            clause, where the order of the list determines the order output (not
   *                            null, not empty)
   * @param where               the specification of the simple {@code WHERE} clause expression of
   *                            this command, if any (nullable)
   */
  public SelectCommand(String tableName, List<SelectCommandColumn> selectClauseColumns, @Nullable CommandWhere where) {
    checkNotNull(tableName, "tableName");
    checkNotNull(selectClauseColumns, "selectClauseColumns");
    checkArgument(!selectClauseColumns.isEmpty(), "selectClauseColumns may not be empty");
    for (int i = 0; i < selectClauseColumns.size(); i++) {
      checkNotNull(selectClauseColumns.get(i), "selectClauseColumns.get(%d) is null", i);
    }

    this.tableName = tableName;
    // Copy to a new list for encapsulation, and wrap in an unmodifiable view for immutability.
    this.selectClauseColumns = unmodifiableList(new ArrayList<>(selectClauseColumns));
    this.where = where;
  }

  /**
   * @return the name of the table being selected FROM (not null)
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * @return an unmodifiable view of the ordered list of (nonnull) column specifications in the
   *         SELECT clause, where the order of the list determines the order output (not null, not
   *         empty)
   */
  public List<SelectCommandColumn> getSelectClauseColumns() {
    return selectClauseColumns;
  }

  /**
   * @return the specification of the simple {@code WHERE} clause expression of this command, if any
   *         (nullable)
   */
  public @Nullable CommandWhere getWhere() {
    return where;
  }

  @Override
  @SuppressWarnings("nullness")
  public boolean equals(Object obj) {
    if (!(obj != null && obj instanceof SelectCommand)) {
      return false;
    }

    SelectCommand other = (SelectCommand) obj;
    return
        tableName.equals(other.getTableName()) &&
        selectClauseColumns.equals(other.getSelectClauseColumns()) &&
        Objects.equals(getWhere(), other.getWhere());
  }

  @Override
  @SuppressWarnings("nullness")
  public int hashCode() {
    return hash(getTableName(), getSelectClauseColumns(), getWhere());
  }

  @Override
  @SuppressWarnings("nullness")
  public String toString() {
    return toStringHelper(SelectCommand.class)
        .add("tableName", getTableName())
        .add("selectClauseColumns", getSelectClauseColumns())
        .add("where", getWhere())
        .toString();
  }

}
