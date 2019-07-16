package edu.utdallas.davisbase.command;

import edu.utdallas.davisbase.NotImplementedException;
import edu.utdallas.davisbase.result.Result;
import edu.utdallas.davisbase.storage.Storage;

public interface Command {

  public default Result execute(Storage storage) throws ExecuteException {
    // TODO Implement Command.execute(Storage)
    throw new NotImplementedException();
  }

}
