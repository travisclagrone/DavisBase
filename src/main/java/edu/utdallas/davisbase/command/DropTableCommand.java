package edu.utdallas.davisbase.command;

import edu.utdallas.davisbase.NotImplementedException;
import edu.utdallas.davisbase.result.DropTableResult;
import edu.utdallas.davisbase.storage.Storage;

public class DropTableCommand implements Command {

  private String tableName;

  @Override
  public DropTableResult execute(Storage storage) throws ExecuteException {
    // TODO Implement DropTableCommand.execute(Storage)
    throw new NotImplementedException();
  }

}
