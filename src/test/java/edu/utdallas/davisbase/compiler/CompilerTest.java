//package edu.utdallas.davisbase.compiler;
//
//import edu.utdallas.davisbase.DataType;
//import edu.utdallas.davisbase.command.*;
//import edu.utdallas.davisbase.representation.*;
//import edu.utdallas.davisbase.storage.Storage;
//import net.sf.jsqlparser.JSQLParserException;
//import net.sf.jsqlparser.expression.Expression;
//import net.sf.jsqlparser.parser.CCJSqlParserUtil;
//import net.sf.jsqlparser.schema.Column;
//import net.sf.jsqlparser.schema.Table;
//import net.sf.jsqlparser.statement.create.table.ColDataType;
//import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
//import net.sf.jsqlparser.statement.select.SelectExpressionItem;
//import net.sf.jsqlparser.statement.select.SelectItem;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//
//import java.math.BigInteger;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SuppressWarnings("nullness")
//public class CompilerTest {
//
//  public static final String TABLE_PERSONS = "Persons";
//  public static final String COL1_PERSON_ID = "PersonID";
//  public static final String COL2_PREFERRED_NAME = "PreferredName";
//
//  @Mock Storage context;
//  private final Compiler compiler = new Compiler(context);
//
//  @Test
//  public void testCompileCreateCommandRepresentation() throws CompileException{
//    CreateTableCommandRepresentation createTableCommandRepresentation = validCreateTableCommandRepresentation();
//    Command command = compiler.compile(createTableCommandRepresentation);
//    assertTrue(command instanceof CreateTableCommand);
//    CreateTableCommand createTable = (CreateTableCommand) command;
//    assertEquals(createTable.getTableName(), TABLE_PERSONS);
//    assertEquals(createTable.getColumnSchemas().get(0).getDataType(), DataType.BIGINT);
//    assertEquals(createTable.getColumnSchemas().get(0).getName(), COL1_PERSON_ID);
//    assertFalse(createTable.getColumnSchemas().get(0).isNotNull());
//    assertEquals(createTable.getColumnSchemas().get(1).getDataType(), DataType.TEXT);
//    assertEquals(createTable.getColumnSchemas().get(1).getName(), COL2_PREFERRED_NAME);
//    assertTrue(createTable.getColumnSchemas().get(1).isNotNull());
//  }
//
//  @Test
//  public void testCompileInsertCommandRepresentation() throws CompileException, JSQLParserException{
//    InsertCommandRepresentation insertCommandRepresentation = validInsertCommandRepresentation();
//    Command command = compiler.compile(insertCommandRepresentation);
//    assertTrue(command instanceof InsertCommand);
//    InsertCommand insertCommand = (InsertCommand) command;
//    assertEquals(insertCommand.getTableName(), TABLE_PERSONS);
//    assertEquals(insertCommand.getValues().get(0), BigInteger.valueOf(1));
//    assertEquals(insertCommand.getValues().get(1), "Name");
//  }
//
//  @Test
//  public void testCompileSelectAllCommandRepresentation() throws CompileException, JSQLParserException{
//    SelectCommandRepresentation selectCommandRepresentation = validSelectAllCommandRepresentation();
//    Command command = compiler.compile(selectCommandRepresentation);
//    assertTrue(command instanceof SelectCommand);
//    SelectCommand select = (SelectCommand) command;
//    assertEquals(select.getTableName(), TABLE_PERSONS);
//    assertEquals(select.getSelectClauseColumns().get(0).getDataType(),DataType.BIGINT);
//    assertEquals(select.getSelectClauseColumns().get(0).getIndex(), 1);
//    assertEquals(select.getSelectClauseColumns().get(0).getName(), COL1_PERSON_ID);
//    assertEquals(select.getSelectClauseColumns().get(1).getDataType(),DataType.TEXT);
//    assertEquals(select.getSelectClauseColumns().get(1).getIndex(), 2);
//    assertEquals(select.getSelectClauseColumns().get(1).getName(), COL2_PREFERRED_NAME);
//  }
//
//  @Test
//  public void testCompileSelectColsCommandRepresentation() throws CompileException, JSQLParserException{
//    SelectCommandRepresentation selectCommandRepresentation = validSelectColCommandRepresentation();
//    Command command = compiler.compile(selectCommandRepresentation);
//    assertTrue(command instanceof SelectCommand);
//    SelectCommand select = (SelectCommand) command;
//    assertEquals(select.getTableName(), TABLE_PERSONS);
//  }
//
//  @Test
//  public void testCompileShowTablesCommandRepresentation() throws CompileException{
//    ShowTablesCommandRepresentation showTablesCommandRepresentation = new ShowTablesCommandRepresentation();
//    Command command = compiler.compile(showTablesCommandRepresentation);
//    assertTrue(command instanceof ShowTablesCommand);
//  }
//
//  @Test
//  public void testCompileExitCommandRepresentation() throws CompileException{
//    ExitCommandRepresentation exitCommandRepresentation = new ExitCommandRepresentation();
//    Command command = compiler.compile(exitCommandRepresentation);
//    assertTrue(command instanceof ExitCommand);
//  }
//
//  public CreateTableCommandRepresentation validCreateTableCommandRepresentation(){
//    List<ColumnDefinition> colDef =  new ArrayList<>();
//
//    ColumnDefinition colDef1 = new ColumnDefinition();
//    ColDataType col1DataType = new ColDataType();
//    col1DataType.setDataType(DataType.BIGINT.name());
//    colDef1.setColDataType(col1DataType);
//    colDef1.setColumnName(COL1_PERSON_ID);
//
//    ColumnDefinition colDef2 = new ColumnDefinition();
//    ColDataType col2DataType = new ColDataType();
//    col2DataType.setDataType(DataType.TEXT.name());
//    colDef2.setColDataType(col1DataType);
//    colDef2.setColumnName(COL2_PREFERRED_NAME);
//    colDef2.setColumnSpecStrings(new ArrayList<>(Arrays.asList("NOT", "NULL")));
//
//    colDef.add(colDef1);
//    colDef.add(colDef2);
//    CreateTableCommandRepresentation createTableCommand = new CreateTableCommandRepresentation(
//      "CREATE TABLE Persons (PersonID BIGINT, PreferredName TEXT [NOT NULL]);",
//      TABLE_PERSONS,
//      colDef
//    );
//    return createTableCommand;
//  }
//
//  public SelectCommandRepresentation validSelectColCommandRepresentation() throws JSQLParserException {
//    List<SelectItem> selectItems = new ArrayList<>();
//    SelectExpressionItem selectCol1 = new SelectExpressionItem();
//    Expression parseExpression = CCJSqlParserUtil.parseExpression(COL1_PERSON_ID);
//    selectCol1.setExpression(parseExpression);
//
//    SelectExpressionItem selectCol2 = new SelectExpressionItem();
//    Expression parseExpression2 = CCJSqlParserUtil.parseExpression(COL2_PREFERRED_NAME);
//    selectCol2.setExpression(parseExpression2);
//
//    selectItems.add(selectCol1);
//    selectItems.add(selectCol2);
//
//    return new SelectCommandRepresentation(
//      "SELECT PersonID, PreferredName FROM Persons;",
//      TABLE_PERSONS,
//      selectItems,
//      false
//    );
//  }
//
//  public SelectCommandRepresentation validSelectAllCommandRepresentation() throws JSQLParserException {
//    List<SelectItem> selectItems = new ArrayList<>();
//    SelectExpressionItem selectCol1 = new SelectExpressionItem();
//    Expression parseExpression = CCJSqlParserUtil.parseExpression("*");
//    selectCol1.setExpression(parseExpression);
//
//    selectItems.add(selectCol1);
//
//    return new SelectCommandRepresentation(
//      "SELECT * FROM Persons;",
//      TABLE_PERSONS,
//      selectItems,
//      true
//    );
//  }
//  public InsertCommandRepresentation validInsertCommandRepresentation()throws JSQLParserException{
//    List<Column> listColumns = new ArrayList<>();
//    listColumns.add(new Column(new Table(TABLE_PERSONS), COL1_PERSON_ID));
//    listColumns.add(new Column(new Table(TABLE_PERSONS), COL2_PREFERRED_NAME));
//    List<Expression> listExpressions = new ArrayList<>();
//    listExpressions.add(CCJSqlParserUtil.parseExpression("5"));
//    listExpressions.add(CCJSqlParserUtil.parseExpression("Name"));
//    return new InsertCommandRepresentation(
//      "INSERT INTO Customers (PersonID, PreferredName) VALUES ('5','Name')",
//      "Customers",
//      listColumns,
//      listExpressions)
//      ;
//  }
//}
