package edu.utdallas.davisbase.representation;

import java.util.List;

public class SelectCommandRepresentation implements CommandRepresentation {

  String command;
  List<String> columns;
  String table;
  boolean all;

  public SelectCommandRepresentation(String command, String table, List<String> columns, boolean all){
    this.command=command;
    this.columns = columns;
    this.table=table;
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
