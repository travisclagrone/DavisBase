package edu.utdallas.davisbase.command;

import java.util.List;

public class InsertCommand implements Command {

  private String tableName;
  private List<String> columnIds;  // TODO Split columnIds field into a list of column name strings and a list of column id byte.
  private List<String> values;  // TODO Refactor values field to use structured objects vs. raw strings.

  // TODO Implement InsertCommand

}
