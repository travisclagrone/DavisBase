package edu.utdallas.davisbase.command;

@SuppressWarnings("nullness")  // COMBAK Unsuppress nullness warnings once we implement DeleteCommand.
public class DeleteCommand implements Command {

  private String tableName;
  // TODO Implement DeleteCommand.wherePredicate

  // COMBAK Implement DeleteCommand

  public DeleteCommand(String tableName) {
    this.tableName = tableName;
  }
}
