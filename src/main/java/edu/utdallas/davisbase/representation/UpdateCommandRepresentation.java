package edu.utdallas.davisbase.representation;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpdateCommandRepresentation implements CommandRepresentation {

  String command;
  private String table;
  private List<String> columns;
  private List<String> values;
  private WhereExpression whereClause;

  public UpdateCommandRepresentation(String command, Table table, List<Column> columns, List<Expression> values, WhereExpression whereClause) {
    this.command = command;
    this.table = table.toString();
    List<String> cols = new ArrayList<>();
    for(Column c: columns){
      cols.add(c.getColumnName());
    }
    this.columns = cols;
    String vals = values.toString().replaceAll("[\\[''\\]]", "");
    String[] splitValues=vals.trim().split("\\s*,\\s*");
    this.values = Arrays.asList(splitValues);
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
