package edu.utdallas.davisbase.representation;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UpdateCommandRepresentation implements CommandRepresentation {

  private final String command;
  private final String table;
  private final Column column;
  private final Expression value;
  private final WhereExpression whereClause;

  public UpdateCommandRepresentation(String command, String table, Column column, Expression value, WhereExpression whereClause) {
    this.command = command;
    this.table = table;
    this.column = column;
    this.value = value;
    this.whereClause = whereClause;
  }

  public String getTable() {
    return table;
  }

  public Column getColumn() {
    return column;
  }

  public Expression getValue() {
    return value;
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
    return "UPDATE";
  }

  @Override
  public String toString() {
    return "UpdateCommandRepresentation{" +
      "command='" + command + '\'' +
      ", table='" + table + '\'' +
      ", column=" + column +
      ", value=" + value +
      ", whereClause=" + whereClause +
      '}';
  }
}
