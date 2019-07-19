package edu.utdallas.davisbase.representation;

import net.sf.jsqlparser.schema.Table;

public class DeleteCommandRepresentation implements CommandRepresentation {

  private String command;
  private String table;
  private WhereExpression whereClause;

  public DeleteCommandRepresentation(String command, Table table, WhereExpression whereClause) {
    this.command = command;
    this.table = table.toString();
    this.whereClause = whereClause;
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
