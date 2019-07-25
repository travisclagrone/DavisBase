package edu.utdallas.davisbase.command;

@SuppressWarnings("nullness")  // COMBAK Unsuppress nullness warnings once we implement DeleteCommand.
public class DeleteCommand implements Command {

  private String tableName;
  // QUESTION How should the where expression be represented?

  // COMBAK Implement DeleteCommand

  public DeleteCommand(String tableName) {
    this.tableName = tableName;
  }
}
