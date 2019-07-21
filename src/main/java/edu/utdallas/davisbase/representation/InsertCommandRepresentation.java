package edu.utdallas.davisbase.representation;

import java.util.List;

public class InsertCommandRepresentation implements CommandRepresentation {
  private String command;
  private String table;
  private List<String> columns;
  private List<String> values;

  public InsertCommandRepresentation(String command, String table, List<String> columns, List<String> values) {
    this.command= command;
    this.table = table;
    this.columns = columns;
    this.values = values;
  }

  public String getTable() {
    return table;
  }

  public void setTable(String table) {
    this.table = table;
  }

  public List<String> getColumns() {
    return columns;
  }

  public void setColumns(List<String> columns) {
    this.columns = columns;
  }

  public List<String> getValues() {
    return values;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }

  @Override
  public String getFullCommand() {
    return command;
  }

  @Override
  public String getOperation() {
    return "INSERT INTO";
  }

  @Override
  public String toString() {
    return "InsertCommandRepresentation{" +
      "command='" + command + '\'' +
      ", table='" + table + '\'' +
      ", columns=" + columns +
      ", values=" + values +
      '}';
  }
}
