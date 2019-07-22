package edu.utdallas.davisbase.command;

import edu.utdallas.davisbase.NotImplementedException;
import edu.utdallas.davisbase.result.DeleteResult;
import edu.utdallas.davisbase.storage.Storage;

public class DeleteCommand implements Command {

  private String tableName;
  //TODO: How should where expression be represented?

  @Override
  public DeleteResult execute(Storage storage) throws ExecuteException {
    // TODO Implement DeleteCommand.execute(Storage)
    throw new NotImplementedException();
  }

}
