package edu.utdallas.davisbase.command;

import edu.utdallas.davisbase.NotImplementedException;
import edu.utdallas.davisbase.result.CreateIndexResult;
import edu.utdallas.davisbase.storage.Storage;

public class CreateIndexCommand implements Command {

  private String tableName;
  private String indexName;
  private String columnName;

  @Override
  public CreateIndexResult execute(Storage storage) throws ExecuteException {
    // TODO Implement CreateIndexCommand.execute(Storage)
    throw new NotImplementedException();
  }

}
