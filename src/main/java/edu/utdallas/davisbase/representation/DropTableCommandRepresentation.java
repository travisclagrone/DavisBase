package edu.utdallas.davisbase.representation;

public class DropTableCommandRepresentation implements CommandRepresentation {
  private final String command;
  private final String table;

  public DropTableCommandRepresentation(String command,String table) {
    this.command= command;
    this.table = table;
  }

  public String getTable() {
    return table;
  }

  @Override
  public String getFullCommand() {
    return command;
  }

  @Override
  public String getOperation() {
    return "DROP TABLE";
  }

  @Override
  public String toString() {
    return "DropTableCommandRepresentation{" +
      "command='" + command + '\'' +
      ", table='" + table + '\'' +
      '}';
  }
}
