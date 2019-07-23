package edu.utdallas.davisbase.representation;

import net.sf.jsqlparser.statement.select.SelectItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SelectCommandRepresentation implements CommandRepresentation {

  private final String command;
  private final List<SelectItem> columns;
  private final String table;
  private final boolean all;
  //professor indicated where expression would not be tested for part 1

  public SelectCommandRepresentation(String command, String table, List<SelectItem> columns, boolean all){
    this.command=command;
    this.columns = Collections.unmodifiableList(new ArrayList<>(columns));
    this.table=table;
    this.all=all;
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
      '}';
  }
}
