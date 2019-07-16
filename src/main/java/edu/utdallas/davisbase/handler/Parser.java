package edu.utdallas.davisbase.handler;

public class Parser {

  /**
   * Returns AST from user input string
   * @param userInput String user commanded
   */
  public AST parse(String userInput){
    //TODO: implement readInput
    validateCommandSyntax(userInput);
    try{
      //create AST from userInput
    }
    catch(Exception e){
      //host.write
    }
    return null;
  };

  /**
   * Validate command against the data catalog
   * @param userInput
   * @return valid SQL command
   */
  private boolean validateCommandSyntax(String userInput){
    //TODO: implement logic for validateCommandSyntax
//    throw new NotImplementedException();
    return false;
  };
}
