package edu.utdallas.davisbase.representation;

import net.sf.jsqlparser.statement.create.table.ColumnDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreateTableCommandRepresentation implements CommandRepresentation {

    private final String command;
    private final String table;
    private final List<ColumnDefinition> definitions;

  public CreateTableCommandRepresentation(String command, String table, List<ColumnDefinition> definitions) {
    this.command = command;
    this.table = table;
    this.definitions = Collections.unmodifiableList(new ArrayList<>(definitions));
  }

  public String getTable() {
    return table;
  }

  public List<ColumnDefinition> getDefinitions() {
    return definitions;
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
