package edu.utdallas.davisbase.representation;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;

import java.util.List;

public class CreateTableCommandRepresentation implements CommandRepresentation {

    private static final String OPERATION= "CREATE TABLE";
    private String command;
    private String table;
    private List<ColumnDefinition> definitions;

  public CreateTableCommandRepresentation(String command, Table table, List<ColumnDefinition> definitions) {
    this.command = command;
    this.table = table.toString();
    this.definitions = definitions;
  }

  @Override
  public String getFullCommand() {
    return command;
  }

  @Override
  public String getOperation() {
    return "CREATE TABLE";
  }

  @Override
  public String toString() {
    return "CreateTableCommandRepresentation{" +
      "command='" + command + '\'' +
      ", table='" + table + '\'' +
      ", definitions=" + definitions +
      '}';
  }
}
