package edu.utdallas.davisbase.representation;

import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.Index;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreateTableCommandRepresentation implements CommandRepresentation {

    private final String command;
    private final String table;
    private final List<ColumnDefinition> definitions;
    private final @Nullable Index index;

  public CreateTableCommandRepresentation(String command, String table, List<ColumnDefinition> definitions, @Nullable Index index) {
    this.command = command;
    this.table = table;
    this.definitions = Collections.unmodifiableList(new ArrayList<>(definitions));
    this.index=index;
  }

  public String getTable() {
    return table;
  }

  public List<ColumnDefinition> getDefinitions() {
    return definitions;
  }

  public @Nullable Index getIndex() {
    return index;
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
      ", index=" + index +
      '}';
  }
}
