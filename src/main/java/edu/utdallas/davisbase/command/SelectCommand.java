package edu.utdallas.davisbase.command;

import java.util.List;

public class SelectCommand implements Command {

  private String table;
  private List<String> columnIds;  // TODO Split this into a list of column name strings and a list of column id bytes.
  // QUESTION How should the where expression be represented?

  // TODO Implement SelectCommand

}
