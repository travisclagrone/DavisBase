package edu.utdallas.davisbase.command;

import edu.utdallas.davisbase.NotImplementedException;
import edu.utdallas.davisbase.result.InsertResult;
import edu.utdallas.davisbase.storage.Storage;

import java.util.List;

public class InsertCommand implements Command {

  private String tableName;
  private List<String> columnIds;
  private List<String> values;

  @Override
  public InsertResult execute(Storage storage) throws ExecuteException {
    // TODO Implement InsertCommand.execute(Storage)
    throw new NotImplementedException();
  }

}
