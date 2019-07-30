package edu.utdallas.davisbase.representation;

import org.checkerframework.checker.nullness.qual.Nullable;

public class DeleteCommandRepresentation implements CommandRepresentation {

  private final String command;
  private final String table;
  private final @Nullable WhereExpression whereClause;

  public DeleteCommandRepresentation(String command, String table, @Nullable WhereExpression whereClause) {
    this.command = command;
    this.table = table;
    this.whereClause = whereClause;
  }

  public String getTable() {
    return table;
  }

  public @Nullable WhereExpression getWhereClause() {
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
