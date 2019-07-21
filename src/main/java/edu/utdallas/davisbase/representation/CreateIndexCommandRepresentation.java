package edu.utdallas.davisbase.representation;

public class CreateIndexCommandRepresentation implements CommandRepresentation {
  private String command;
  private String table;
  private String index;

  public CreateIndexCommandRepresentation(String command, String table, String index) {
    this.command = command;
    this.table = table;
    this.index = index;
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
