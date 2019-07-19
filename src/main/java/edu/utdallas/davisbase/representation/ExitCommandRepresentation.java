package edu.utdallas.davisbase.representation;

public class ExitCommandRepresentation implements CommandRepresentation {

  @Override
  public String getFullCommand() {
    return "EXIT";
  }

  @Override
  public String getOperation() {
    return "EXIT";
  }
}
