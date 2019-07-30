package edu.utdallas.davisbase.command;

import edu.utdallas.davisbase.DataType;

@SuppressWarnings("nullness")  // COMBAK Unsuppress nullness warnings once we implement CreateIndexCommand.
public class CreateIndexCommand implements Command {

  private String tableName;
  private String columnName;
  private byte columnIndex;
  private DataType dataType;

  public CreateIndexCommand(String tableName, String columnName, byte columnIndex, DataType dataType) {
    this.tableName = tableName;
    this.columnName = columnName;
    this.columnIndex = columnIndex;
    this.dataType = dataType;
  }

  @Override
  public String toString() {
    return "CreateIndexCommand{" +
      "tableName='" + tableName + '\'' +
      ", columnName='" + columnName + '\'' +
      ", columnIndex=" + columnIndex +
      ", dataType=" + dataType +
      '}';
  }
}
