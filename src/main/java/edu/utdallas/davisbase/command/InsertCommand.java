package edu.utdallas.davisbase.command;

import java.util.List;

public class InsertCommand implements Command {

  private String tableName;
  private List<String> columnIds;
  private List<String> values;

}
