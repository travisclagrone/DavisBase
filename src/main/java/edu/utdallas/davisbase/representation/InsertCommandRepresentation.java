package edu.utdallas.davisbase.representation;

import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InsertCommandRepresentation implements CommandRepresentation {
  private String command;
  private String table;
  private List<String> columns;
  private List<String> values;

  public InsertCommandRepresentation(String command, Table table, List<Column> columns, ItemsList values) {
    this.command= command;
    this.table = table.getName();
    List<String> cols = new ArrayList<>();
    for(Column c: columns){
      cols.add(c.getColumnName());
    }
    this.columns = cols;
    String vals = values.toString().replaceAll("[('')]", "");
    String[] splitValues=vals.trim().split("\\s*,\\s*");
    this.values = Arrays.asList(splitValues);
  }

  public String getTable() {
    return table;
  }

  public void setTable(String table) {
    this.table = table;
  }

  public List<String> getColumns() {
    return columns;
  }

  public void setColumns(List<String> columns) {
    this.columns = columns;
  }

  public List<String> getValues() {
    return values;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }

  @Override
  public String getFullCommand() {
    return command;
  }

  @Override
  public String getOperation() {
    return "INSERT INTO";
  }

  @Override
  public String toString() {
    return "InsertCommandRepresentation{" +
      "command='" + command + '\'' +
      ", table='" + table + '\'' +
      ", columns=" + columns +
      ", values=" + values +
      '}';
  }
}
