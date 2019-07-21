package edu.utdallas.davisbase.representation;

public class ShowTablesCommandRepresentation implements CommandRepresentation {


  @Override
  public String getFullCommand() {
    return "SHOW TABLES;";
  }

  @Override
  public String getOperation() {
    return "SHOW TABLES";
  }

  public String toString() {
    return "ShowTablesCommandRepresentation{}";
  }
}
