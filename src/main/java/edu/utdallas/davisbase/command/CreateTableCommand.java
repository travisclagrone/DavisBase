package edu.utdallas.davisbase.command;

import edu.utdallas.davisbase.NotImplementedException;
import edu.utdallas.davisbase.result.CreateTableResult;
import edu.utdallas.davisbase.storage.Storage;

public class CreateTableCommand implements Command {

  @Override
  public CreateTableResult execute(Storage storage) throws ExecuteException {
    // TODO Implement CreateTableCommand.execute(Storage)
    throw new NotImplementedException();
  }

}
