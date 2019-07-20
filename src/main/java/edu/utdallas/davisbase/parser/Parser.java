package edu.utdallas.davisbase.parser;

import edu.utdallas.davisbase.representation.*;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.*;
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
        return create;
      }
      else if(stmt instanceof Drop){ //type determines if index or table
        Drop dropTableStatement = (Drop) stmt;
        if(dropTableStatement.getType().equalsIgnoreCase("TABLE")){
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
          parseWhereExpression(deleteStatement.getWhere())
        );
        return delete;
      }
      else if (stmt instanceof Update) {
        Update updateStatement = (Update) stmt;
        UpdateCommandRepresentation update = new UpdateCommandRepresentation(
          updateStatement.toString(),
          updateStatement.getTable(),
          updateStatement.getColumns(),
          updateStatement.getExpressions(),
          parseWhereExpression(updateStatement.getWhere())
        );
        return update;
      }
      else if(stmt instanceof Select){
        Select selectStatement = (Select) stmt;
        SelectCommandRepresentation select = new SelectCommandRepresentation(
          selectStatement.toString(),
          selectStatement.getSelectBody()
        );
        return select;
      }
      else{
        throw new ParseException("Sorry DavisBase does not support this command");
      }
    }
    catch(JSQLParserException e){
      throw(new ParseException(e.getCause()));
    }
  }

  /**
   * @param where clause to parse
   * @return WhereExpression representation of the expression
   */
  public WhereExpression parseWhereExpression(Expression where){
    WhereExpression whereExpression;
    if(where instanceof EqualsTo){
      EqualsTo equals = (EqualsTo) where;
      whereExpression= new WhereExpression(
        equals.toString(),
        equals.isNot(),
        equals.getLeftExpression(),
        equals.getStringExpression(),
        equals.getRightExpression()
      );
      return whereExpression;
    }
    else if(where instanceof NotEqualsTo){
      NotEqualsTo notEqualsTo = (NotEqualsTo) where;
      whereExpression= new WhereExpression(
        notEqualsTo.toString(),
        notEqualsTo.isNot(),
        notEqualsTo.getLeftExpression(),
        notEqualsTo.getStringExpression(),
        notEqualsTo.getRightExpression()
      );
      return whereExpression;
    }
    else if(where instanceof GreaterThan){
      GreaterThan greaterThan = (GreaterThan)where;
      whereExpression= new WhereExpression(
        greaterThan.toString(),
        greaterThan.isNot(),
        greaterThan.getLeftExpression(),
        greaterThan.getStringExpression(),
        greaterThan.getRightExpression()
      );
      return whereExpression;
    }
    else if(where instanceof GreaterThanEquals){
      GreaterThanEquals greaterThanEquals = (GreaterThanEquals)where;
      whereExpression= new WhereExpression(
        greaterThanEquals.toString(),
        greaterThanEquals.isNot(),
        greaterThanEquals.getLeftExpression(),
        greaterThanEquals.getStringExpression(),
        greaterThanEquals.getRightExpression()
      );
      return whereExpression;
    }
    else if(where instanceof MinorThan){
      MinorThan minorThan = (MinorThan)where;
      whereExpression= new WhereExpression(
        minorThan.toString(),
        minorThan.isNot(),
        minorThan.getLeftExpression(),
        minorThan.getStringExpression(),
        minorThan.getRightExpression()
      );
      return whereExpression;
    }
    else { //if(where instanceof MinorThanEquals)
      MinorThanEquals minorThanEquals = (MinorThanEquals)where;
      whereExpression= new WhereExpression(
        minorThanEquals.toString(),
        minorThanEquals.isNot(),
        minorThanEquals.getLeftExpression(),
        minorThanEquals.getStringExpression(),
        minorThanEquals.getRightExpression()
      );
      return whereExpression;
    }

  }

}
