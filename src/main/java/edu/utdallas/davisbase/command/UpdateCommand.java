package edu.utdallas.davisbase.command;

import java.beans.Expression;

@SuppressWarnings("nullness")  // COMBAK Unsuppress nullness warnings once we implement UpdateCommand.
public class UpdateCommand implements Command {

  private String tableName;
  private String columnId;  // COMBAK Split columnIds field into a list column name strings and a list of column id bytes.
  private Object value;  // COMBAK Refactor values field to use structured objects vs. raw strings.
  // QUESTION How should the where expression be represented?


  public UpdateCommand(String tableName, String columnId, Object value) {
    this.tableName = tableName;
    this.columnId = columnId;
    this.value = value;
  }

  public String getTableName() {
    return tableName;
  }

  public String getColumnId() {
    return columnId;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "UpdateCommand{" +
      "tableName='" + tableName + '\'' +
      ", columnId='" + columnId + '\'' +
      ", value='" + value + '\'' +
      '}';
  }
}
