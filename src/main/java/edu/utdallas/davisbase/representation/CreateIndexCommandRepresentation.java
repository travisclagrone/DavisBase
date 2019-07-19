package edu.utdallas.davisbase.representation;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.Index;

public class CreateIndexCommandRepresentation implements CommandRepresentation {
  private String command;
  private String table;
  private String index;

  public CreateIndexCommandRepresentation(String command, Table table, Index index) {
    this.command = command;
    this.table = table.getName();
    this.index = index.getName();
  }

  public String getTable() {
    return table;
  }

  public void setTable(String table) {
    this.table = table;
  }

  public String getIndex() {
    return index;
  }

  public void setIndex(String index) {
    this.index = index;
  }

  @Override
  public String getFullCommand() {
    return command;
  }

  @Override
  public String getOperation() {
    return "CREATE INDEX";
  }

  @Override
  public String toString() {
    return "CreateIndexCommandRepresentation{" +
      "command='" + command + '\'' +
      ", table='" + table + '\'' +
      ", index='" + index + '\'' +
      '}';
  }
}
