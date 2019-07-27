package edu.utdallas.davisbase.command;

@SuppressWarnings("nullness")  // COMBAK Unsuppress nullness warnings once we implement CreateIndexCommand.
public class CreateIndexCommand implements Command {

  private String tableName;
  private String indexName;
  private String columnName;
  // QUESTION Don't we need the column index too? And maybe even data type?

  // COMBAK Implement CreateIndexCommand

  public CreateIndexCommand(String tableName, String indexName, String columnName) {
    this.tableName = tableName;
    this.indexName = indexName;
    this.columnName = columnName;
  }
}
