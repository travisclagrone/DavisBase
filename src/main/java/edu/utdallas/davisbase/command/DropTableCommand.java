package edu.utdallas.davisbase.command;

import static java.util.Objects.hash;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

public class DropTableCommand implements Command {

  private final String tableName;

  /**
   * @param tableName the name of the table to drop (not null)
   */
  public DropTableCommand(String tableName) {
    checkNotNull(tableName, "tableName");

    this.tableName = tableName;
  }

  /**
   * @return the name of the table to drop (not null)
   */
  public String getTableName() {
    return tableName;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj != null && obj instanceof DropTableCommand)) {
      return false;
    }

    DropTableCommand other = (DropTableCommand) obj;
    return getTableName().equals(other.getTableName());
  }

  @Override
  public int hashCode() {
    return hash(getTableName());
  }

  @Override
  public String toString() {
    return toStringHelper(DropTableCommand.class)
        .add("tableName", getTableName())
        .toString();
  }

}
