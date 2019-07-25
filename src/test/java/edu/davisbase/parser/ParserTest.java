package edu.davisbase.parser;

import edu.utdallas.davisbase.DataType;
import edu.utdallas.davisbase.parser.ParseException;
import edu.utdallas.davisbase.parser.Parser;
import edu.utdallas.davisbase.representation.*;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class ParserTest {

  private final String createTable= "CREATE TABLE Persons (PersonID BIGINT, PreferredName TEXT [NOT NULL]);";
  private final String insertRecord= "INSERT INTO Customers (CustomerName, ContactName, Address) VALUES ('Cardinal', 'Tom B. Erichsen', 'Skagen 21');";
  private final String selectAllCols= "SELECT * FROM Customers;";
  private final String selectCols= "SELECT CustomerName, ContactName FROM Customers;";
  private final String showTable= "SHOW TABLES;";
  private final Parser parser = new Parser();


  @Test
  public void testParseCreateTableStatement() throws ParseException {
    CommandRepresentation command = parser.parse(createTable);
    assertTrue(command instanceof CreateTableCommandRepresentation);
    CreateTableCommandRepresentation createTable = (CreateTableCommandRepresentation)command;
    assertEquals(createTable.getTable(), "Persons");
    assertEquals(createTable.getDefinitions().get(0).getColDataType().toString(),DataType.BIGINT.name());
    assertEquals(createTable.getDefinitions().get(0).getColumnName(), "PersonID");
    assertNull(createTable.getDefinitions().get(0).getColumnSpecStrings());
    assertEquals(createTable.getDefinitions().get(1).getColDataType().toString(),DataType.TEXT.name());
    assertEquals(createTable.getDefinitions().get(1).getColumnName(), "PreferredName");
    assertTrue(!createTable.getDefinitions().get(1).getColumnSpecStrings().isEmpty());
  }

  @Test
  public void testParseInsertRecordStatement() throws ParseException{
    CommandRepresentation command = parser.parse(insertRecord);
    assertTrue(command instanceof InsertCommandRepresentation);
    InsertCommandRepresentation insertRecord = (InsertCommandRepresentation) command;
    assertEquals(insertRecord.getTable(),"Customers");
    assertEquals(insertRecord.getColumns().size(), 3);
    assertEquals(insertRecord.getColumns().get(0).toString(),"CustomerName");
    assertEquals(insertRecord.getColumns().get(1).toString(),"ContactName");
    assertEquals(insertRecord.getColumns().get(2).toString(),"Address");
    assertEquals(insertRecord.getValues().size(),3);
    assertEquals(insertRecord.getValues().get(0).toString(),"'Cardinal'");
    assertEquals(insertRecord.getValues().get(1).toString(), "'Tom B. Erichsen'");
    assertEquals(insertRecord.getValues().get(2).toString(), "'Skagen 21'");
  }

  @Test
  public void testParseSelectAllColsStatement() throws ParseException{
    CommandRepresentation command = parser.parse(selectAllCols);
    assertTrue(command instanceof SelectCommandRepresentation);
    SelectCommandRepresentation selectCommand = (SelectCommandRepresentation) command;
    assertEquals(selectCommand.getTable(),"Customers");
    assertTrue(selectCommand.isAll());
  }

  @Test
  public void testParseSelectColsStatement() throws ParseException{
    CommandRepresentation command = parser.parse(selectCols);
    assertTrue(command instanceof SelectCommandRepresentation);
    SelectCommandRepresentation selectCommand = (SelectCommandRepresentation) command;
    assertEquals(selectCommand.getTable(),"Customers");
    assertEquals(((SelectExpressionItem) selectCommand.getColumns().get(0)).getExpression().toString(),"CustomerName");
    assertEquals(((SelectExpressionItem) selectCommand.getColumns().get(1)).getExpression().toString(),"ContactName");
  }

  @Test
  public void testParseShowTablesStatement() throws ParseException {
    CommandRepresentation command = parser.parse(showTable);
    assertTrue(command instanceof ShowTablesCommandRepresentation);
  }
}
