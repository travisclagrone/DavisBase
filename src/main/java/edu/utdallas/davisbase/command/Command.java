package edu.utdallas.davisbase.command;

import edu.utdallas.davisbase.result.Result;
import edu.utdallas.davisbase.storage.Storage;

public interface Command {

  public Result execute(Storage storage);
}
