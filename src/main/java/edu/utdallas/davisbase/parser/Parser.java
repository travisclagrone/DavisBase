package edu.utdallas.davisbase.parser;

import edu.utdallas.davisbase.representation.*;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

import java.io.StringReader;

import static com.google.common.base.Preconditions.checkNotNull;

public class Parser {

  protected final ParserConfiguration configuration;

  public Parser(ParserConfiguration configuration) {
    checkNotNull(configuration);
    this.configuration = configuration;
  }

  /**
   * @param statement a single complete statement to parse
   * @return the {@link CommandRepresentation} representation of <code>statement</code>
   * @throws ParseException if <code>statement</code> is not a single complete statement that is
   *                        both lexically and syntactically correct
   */
  public CommandRepresentation parse(String statement) throws ParseException {
    try {
      if(statement.equalsIgnoreCase(new ExitCommandRepresentation().getFullCommand())){
        return new ExitCommandRepresentation();
      }
      if(statement.equalsIgnoreCase(new ShowTablesCommandRepresentation().getFullCommand())){
        return new ShowTablesCommandRepresentation();
      }
      CCJSqlParserManager pm = new CCJSqlParserManager();
      Statement stmt = pm.parse(new StringReader(statement));
      System.out.println("Hello this is my statment: " + stmt.toString());
      if(stmt instanceof CreateTable){
        CreateTable createTableStatement = (CreateTable) stmt;
        CreateTableCommandRepresentation create = new CreateTableCommandRepresentation(
          createTableStatement.toString(),
          createTableStatement.getTable(),
          createTableStatement.getColumnDefinitions()
        );
           }
      else if(stmt instanceof Drop){ //type determines if index or table
        Drop dropTableStatement = (Drop) stmt;
        if(dropTableStatement.getType().equalsIgnoreCase("INDEX")){
          DropTableCommandRepresentation dropTable = new DropTableCommandRepresentation(
            dropTableStatement.toString(),
            dropTableStatement.getName()
          );
          return dropTable;
        }
      }
      else if(stmt instanceof CreateIndex){
        CreateIndex createIndexStatement = (CreateIndex) stmt;
        CreateIndexCommandRepresentation createIndex = new CreateIndexCommandRepresentation(
          createIndexStatement.toString(),
          createIndexStatement.getTable(),
          createIndexStatement.getIndex()
        );
        return createIndex;
      }

      else if (stmt instanceof Insert) {
        Insert insertStatement = (Insert) stmt;
        InsertCommandRepresentation insert = new InsertCommandRepresentation(
          insertStatement.toString(),
          insertStatement.getTable(),
          insertStatement.getColumns(),
          insertStatement.getItemsList());
        return insert;
      }
      if (stmt instanceof Delete) {
        Delete deleteStatement = (Delete) stmt;
        DeleteCommandRepresentation delete = new DeleteCommandRepresentation(
          deleteStatement.toString(),
          deleteStatement.getTable(),
          new WhereExpression(deleteStatement.getWhere())
        );
      }
      else if (stmt instanceof Update) {
        Update updateStatement = (Update) stmt;
        UpdateCommandRepresentation update = new UpdateCommandRepresentation(
          updateStatement.toString(),
          updateStatement.getTable(),
          updateStatement.getColumns(),
          updateStatement.getExpressions(),
          new WhereExpression(updateStatement.getWhere())
        );
        return update;
      }
      else if(stmt instanceof Select){
        Select selectStatement = (Select) stmt;
        System.out.println(selectStatement.getSelectBody());
        System.out.println(selectStatement.getWithItemsList());
      }
      else{
        throw new ParseException("Sorry DavisBase does not support this command");
      }
    }
    catch(JSQLParserException e){
      System.out.println("PARSE EXCEPTION " + e.getCause());
      throw(new ParseException(e.getCause()));
    }
    throw new ParseException("Sorry DavisBase does not support this command");
  }

}
