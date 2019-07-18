package edu.utdallas.davisbase.parser;

import edu.utdallas.davisbase.command.Command;
import edu.utdallas.davisbase.command.UpdateCommand;
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
   * @return the {@link Ast} representation of <code>statement</code>
   * @throws ParseException if <code>statement</code> is not a single complete statement that is
   *                        both lexically and syntactically correct
   */
  public Ast parse(String statement) throws ParseException {
    // TODO Implement Parser.parse(String)
    Command cmd;
    try {
      if(statement.equalsIgnoreCase("exit")){
        System.out.println("Exit Command");
      }
      else if(statement.equalsIgnoreCase("show tables")){
        System.out.println("Show Tables");
      }
      CCJSqlParserManager pm = new CCJSqlParserManager();
      Statement stmt = pm.parse(new StringReader(statement));
      System.out.println("Hello this is my statment: " + stmt.toString());
      if(stmt instanceof CreateTable){
        CreateTable createTableStatment = (CreateTable) stmt;
        System.out.println(createTableStatment.getColumnDefinitions());
        System.out.println(createTableStatment.getIndexes());
        System.out.println(createTableStatment.getTable());
        System.out.println(createTableStatment.getTableOptionsStrings());
      }
      else if(stmt instanceof Drop){ //type determines if index or table
        Drop dropTableStatement = (Drop) stmt;
        System.out.println(dropTableStatement.getName());
        System.out.println(dropTableStatement.getParameters());
        System.out.println(dropTableStatement.getType());
      }
      else if(stmt instanceof CreateIndex){
        CreateIndex createIndexStatement = (CreateIndex) stmt;
        System.out.println(createIndexStatement.getIndex());
        System.out.println(createIndexStatement.getTable());
      }

      else if (stmt instanceof Insert) {
        Insert insertStatement = (Insert) stmt;
        System.out.println(insertStatement.getColumns());
        System.out.println(insertStatement.getItemsList());
        System.out.println(insertStatement.getTable());
      }
      if (stmt instanceof Delete) {
        Delete deleteStatement = (Delete) stmt;
        System.out.println(deleteStatement.getWhere());
        System.out.println(deleteStatement.getTable());

      }
      else if (stmt instanceof Update) {
        Update updateStatement = (Update) stmt;
        System.out.println(updateStatement.getColumns());
        System.out.println(updateStatement.getExpressions());
        System.out.println(updateStatement.getWhere());
        cmd = new UpdateCommand();
      }
      else if(stmt instanceof Select){
        Select selectStatement = (Select) stmt;
        System.out.println(selectStatement.getSelectBody());
        System.out.println(selectStatement.getWithItemsList());
      }
      else{
        System.out.println("I do not recognize");
      }
    }
    catch(JSQLParserException e){
      System.out.println("PARSE EXCEPTION");
      throw(new ParseException());
    }

//    throw new NotImplementedException();
    return new Ast();
  }



}
