package edu.utdallas.davisbase.command;

import edu.utdallas.davisbase.NotImplementedException;
import edu.utdallas.davisbase.result.SelectResult;
import edu.utdallas.davisbase.storage.Storage;

import java.util.List;

public class SelectCommand implements Command {

  private String table;
  private List<String> columnIds;

  @Override
  public SelectResult execute(Storage storage) throws ExecuteException {
    // TODO Implement SelectCommand.execute(Storage)
    throw new NotImplementedException();
  }

}
