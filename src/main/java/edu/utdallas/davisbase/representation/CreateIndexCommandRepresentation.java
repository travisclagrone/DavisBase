package edu.utdallas.davisbase.representation;

public class CreateIndexCommandRepresentation implements CommandRepresentation {
  private String command;
  private String table;
  private String index;
  private String column;

  public CreateIndexCommandRepresentation(String command, String table, String index, String column) {
    this.command = command;
    this.table = table;
    this.index = index;
    this.column = column;
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

  public String getColumn() {
    return column;
  }

  public void setColumn(String column) {
    this.column = column;
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
