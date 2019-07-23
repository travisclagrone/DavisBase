package edu.utdallas.davisbase.command;

public class DropTableCommand implements Command {

  private String tableName;

  // COMBAK Implement DropTableCommand

  public DropTableCommand(String tableName) {
    this.tableName = tableName;
  }
}
