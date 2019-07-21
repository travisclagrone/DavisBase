package edu.utdallas.davisbase;

import edu.utdallas.davisbase.parser.ParseException;
import edu.utdallas.davisbase.parser.Parser;
import edu.utdallas.davisbase.parser.ParserConfiguration;
import edu.utdallas.davisbase.representation.CommandRepresentation;

public class DavisBase {

  public static void main(String[] args) {
    DavisBase davisBase = DavisBase.startUp(args);
    int exitCode = davisBase.run();
    System.exit(exitCode);
  }

  public static DavisBase startUp(String[] args) {
    return new DavisBase();
  }

  private DavisBase() {}

  /**
   * @return exit code
   */
  public int run() {
    // TODO Implement DavisBase.run()
    //call Parser.parse(input)
    Parser parse = new Parser(new ParserConfiguration());
    try{
      CommandRepresentation rep;
//      rep = parse.parse("CREATE TABLE Persons (PersonID int, LastName varchar(255), FirstName varchar(255));");
//     rep= parse.parse("DROP TABLE Shippers;");
//      rep = parse.parse("CREATE INDEX idx_lastname ON Persons;");
//      rep=parse.parse("DELETE FROM Customers WHERE CustomerName='Alfreds Futterkiste';");
//      rep=parse.parse("DELETE FROM Customers WHERE NOT id <> 5;");
//       rep = parse.parse("SELECT age+16 FROM Customers;");
//      rep = parse.parse("SELECT * FROM Customers;");
//        rep= parse.parse("SELECT col1, col2 FROM Customers;");
//      rep=parse.parse("UPDATE Customers\n" +
//        "SET ContactName = 'Alfred Schmidt', City= 'Frankfurt'\n" +
//        "WHERE NOT CustomerID =5;");
//      rep=parse.parse("UPDATE Customers\n" +
//        "SET ContactName = 'Alfred Schmidt', City= 'Frankfurt'\n" +
//        "WHERE CustomerID IN  ('1','2','3');");
//      parse.parse("SELECT * FROM Customers\n" +
//        "WHERE NOT Country='Germany';");
//    rep= parse.parse("INSERT INTO Customers (CustomerName, ContactName, Address, City, PostalCode, Country)\n" +
//      "VALUES ('Cardinal', 'Tom B. Erichsen', 'Skagen 21', 'Stavanger', '4006', 'Norway');");
//      rep= parse.parse("EXIT;");
//      rep= parse.parse("  show      tables;  ");
      System.out.println(rep);
    }
    catch (ParseException e){
      System.out.println(e.getCause());
    }
//    throw new NotImplementedException();
    return 0;
  }
}
