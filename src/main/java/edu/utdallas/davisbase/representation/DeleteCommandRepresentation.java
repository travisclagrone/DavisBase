package edu.utdallas.davisbase.representation;

public class DeleteCommandRepresentation implements CommandRepresentation {

  private final String command;
  private final String table;
  private final WhereExpression whereClause;

  public DeleteCommandRepresentation(String command, String table, WhereExpression whereClause) {
    this.command = command;
    this.table = table;
    this.whereClause = whereClause;
  }

  public String getTable() {
    return table;
  }

  public WhereExpression getWhereClause() {
    return whereClause;
  }

  @Override
  public String getFullCommand() {
    return command;
  }

  @Override
  public String getOperation() {
    return "DELETE FROM";
  }

  @Override
  public String toString() {
    return "DeleteCommandRepresentation{" +
      "command='" + command + '\'' +
      ", table='" + table + '\'' +
      ", whereClause=" + whereClause +
      '}';
  }
}
