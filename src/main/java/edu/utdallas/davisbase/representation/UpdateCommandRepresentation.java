package edu.utdallas.davisbase.representation;

import java.util.List;

public class UpdateCommandRepresentation implements CommandRepresentation {

  String command;
  private String table;
  private List<String> columns;
  private List<String> values;
  private WhereExpression whereClause;

  public UpdateCommandRepresentation(String command, String table, List<String> columns, List<String> values, WhereExpression whereClause) {
    this.command = command;
    this.table = table;
    this.columns = columns;
    this.values = values;
    this.whereClause = whereClause;
  }


  public String getTable() {
    return table;
  }

  public List<String> getColumns() {
    return columns;
  }

  public List<String> getValues() {
    return values;
  }

  public WhereExpression getWhereClause() {
    return whereClause;
  }

  public void setWhereClause(WhereExpression whereClause) {
    this.whereClause = whereClause;
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
      ", columns=" + columns +
      ", values=" + values +
      ", whereClause=" + whereClause +
      '}';
  }
}
