package edu.utdallas.davisbase.representation;

import jdk.nashorn.internal.objects.annotations.Where;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SelectCommandRepresentation implements CommandRepresentation {

  private final String command;
  private final List<SelectItem> columns;
  private final String table;
  private final boolean all;
  private final @Nullable WhereExpression whereClause;

  public SelectCommandRepresentation(String command, String table, List<SelectItem> columns, boolean all, @Nullable WhereExpression whereClause){
    this.command=command;
    this.columns = Collections.unmodifiableList(new ArrayList<>(columns));
    this.table=table;
    this.all=all;
    this.whereClause = whereClause;
  }

  public List<SelectItem> getColumns() {
    return columns;
  }

  public String getTable() {
    return table;
  }

  public boolean isAll() {
    return all;
  }

  public @Nullable WhereExpression getWhereClause() {
    return whereClause;
  }

  @Override
  public String getFullCommand() {
    return command;
  }

  @Override
  public String getOperation() {
    return "SELECT";
  }

  @Override
  public String toString() {
    return "SelectCommandRepresentation{" +
      "command='" + command + '\'' +
      ", columns=" + columns +
      ", table='" + table + '\'' +
      ", all=" + all +
      ", whereClause=" + whereClause +
      '}';
  }
}
