package edu.utdallas.davisbase.representation;

import net.sf.jsqlparser.statement.select.SelectBody;

import java.util.Arrays;
import java.util.List;

public class SelectCommandRepresentation implements CommandRepresentation {

  String command;
  List<String> columns;
  String table;
  boolean all=false;

  public SelectCommandRepresentation(String command, SelectBody selectBody){
    this.command=command;
    String select = selectBody.toString();
    String[] splitStr = select.trim().split("SELECT |FROM ");
    String[] splitValues=splitStr[1].trim().split("\\s*,\\s*");
    this.columns = Arrays.asList(splitValues);
    if(splitValues[0].equalsIgnoreCase("*")){
      all=true;
    }
    this.table=splitStr[2];
  }

  @Override
  public String getFullCommand() {
    return command;
  }

  @Override
  public String getOperation() {
    return "SELECT";
  }

  @Override
  public String toString() {
    return "SelectCommandRepresentation{" +
      "command='" + command + '\'' +
      ", columns=" + columns +
      ", table='" + table + '\'' +
      ", all=" + all +
      '}';
  }
}
