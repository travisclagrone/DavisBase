package edu.utdallas.davisbase.representation;

public class CreateIndexCommandRepresentation implements CommandRepresentation {
  private final String command;
  private final String table;
  private final String index;
  private final String column;

  public CreateIndexCommandRepresentation(String command, String table, String index, String column) {
    this.command = command;
    this.table = table;
    this.index = index;
    this.column = column;
  }

  public String getTable() {
    return table;
  }

  public String getIndex() {
    return index;
  }

  public String getColumn() {
    return column;
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
      ", column='" + column + '\'' +
      '}';
  }
}
