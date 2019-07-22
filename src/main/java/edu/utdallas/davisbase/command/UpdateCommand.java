package edu.utdallas.davisbase.command;

import java.util.List;

public class UpdateCommand implements Command {

  private String tableName;
  private List<String> columnIds;
  private List<String> values;
  //TODO: How should where expression be represented?

}
