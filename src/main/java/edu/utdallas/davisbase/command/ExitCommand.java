package edu.utdallas.davisbase.command;

import edu.utdallas.davisbase.NotImplementedException;
import edu.utdallas.davisbase.result.ExitResult;
import edu.utdallas.davisbase.storage.Storage;

public class ExitCommand implements Command {

  @Override
  public ExitResult execute(Storage storage) throws ExecuteException {
    // TODO Implement ExitCommand.execute(Storage)
    throw new NotImplementedException();
  }

}
