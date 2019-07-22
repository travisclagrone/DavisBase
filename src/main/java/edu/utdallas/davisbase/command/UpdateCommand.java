package edu.utdallas.davisbase.command;

import edu.utdallas.davisbase.NotImplementedException;
import edu.utdallas.davisbase.result.UpdateResult;
import edu.utdallas.davisbase.storage.Storage;

import java.util.List;

public class UpdateCommand implements Command {

  private String tableName;
  private List<String> columnIds;
  private List<String> values;
  //TODO: How should where expression be represented?

  @Override
  public UpdateResult execute(Storage storage) throws ExecuteException {
    // TODO Implement UpdateCommand.execute(Storage)
    throw new NotImplementedException();
  }

}
