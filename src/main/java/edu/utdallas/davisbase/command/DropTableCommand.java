package edu.utdallas.davisbase.command;

@SuppressWarnings("nullness")  // COMBAK Unsuppress nullness warnings once we implement DropTableCommand.
public class DropTableCommand implements Command {

  private String tableName;

  // COMBAK Implement DropTableCommand

  public DropTableCommand(String tableName) {
    this.tableName = tableName;
  }
}
