package edu.utdallas.davisbase.parser;

import edu.utdallas.davisbase.NotImplementedException;
import edu.utdallas.davisbase.representation.*;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

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
      if (Pattern.matches("(?i)\\s*EXIT\\s*;\\s*", statement)){
        return new ExitCommandRepresentation();
      }
      if (Pattern.matches("(?i)\\s*SHOW\\s+TABLES\\s*;\\s*", statement)) {
        return new ShowTablesCommandRepresentation();
      }
      CCJSqlParserManager pm = new CCJSqlParserManager();
      Statement stmt = pm.parse(new StringReader(statement));
      if(stmt instanceof CreateTable){
        CreateTable createTableStatement = (CreateTable) stmt;
        CreateTableCommandRepresentation create = new CreateTableCommandRepresentation(
          createTableStatement.toString(),
          createTableStatement.getTable().getName(),
          createTableStatement.getColumnDefinitions()
        );
        return create;
      }
      else if(stmt instanceof Drop){ //type determines if index or table
        Drop dropTableStatement = (Drop) stmt;
        if(dropTableStatement.getType().equalsIgnoreCase("TABLE")){
          DropTableCommandRepresentation dropTable = new DropTableCommandRepresentation(
            dropTableStatement.toString(),
            dropTableStatement.getName().getName()
          );
          return dropTable;
        }
        else if (dropTableStatement.getType().equalsIgnoreCase("INDEX")) {
          throw new NotImplementedException("DROP INDEX");  // TODO Implement DROP INDEX parsing (project part 2)
          // return dropIndex;
        }
        else {
          throw new ParseException(String.format("DavisBase does not support the '%s' command", dropTableStatement.getType()));
        }

      }
      else if(stmt instanceof CreateIndex){
        CreateIndex createIndexStatement = (CreateIndex) stmt;
        CreateIndexCommandRepresentation createIndex = new CreateIndexCommandRepresentation(
          createIndexStatement.toString(),
          createIndexStatement.getTable().getName(),
          createIndexStatement.getIndex().getName(),
          createIndexStatement.getIndex().getColumnsNames().get(0)
        );
        return createIndex;
      }
      else if (stmt instanceof Insert) {
        Insert insertStatement = (Insert) stmt;
        String vals = insertStatement.getItemsList().toString().replaceAll("[('')]", "");
        InsertCommandRepresentation insert = new InsertCommandRepresentation(
          insertStatement.toString(),
          insertStatement.getTable().getName(),
          insertStatement.getColumns(),
          getExpressions(vals)
        );
        return insert;
      }
      else if (stmt instanceof Delete) {
        Delete deleteStatement = (Delete) stmt;
        DeleteCommandRepresentation delete = new DeleteCommandRepresentation(
          deleteStatement.toString(),
          deleteStatement.getTable().getName(),
          parseWhereExpression(deleteStatement.getWhere())
        );
        return delete;
      }
      else if (stmt instanceof Update) {
        Update updateStatement = (Update) stmt;
        String vals = updateStatement.getExpressions().toString().replaceAll("[\\[''\\]]", "");
        UpdateCommandRepresentation update = new UpdateCommandRepresentation(
          updateStatement.toString(),
          updateStatement.getTables().get(0).getName(),
          updateStatement.getColumns(),
          getExpressions(vals),
          parseWhereExpression(updateStatement.getWhere())
        );
        return update;
      }
      else if(stmt instanceof Select){
        Select selectStatement = (Select) stmt;
        String selectBody = selectStatement.getSelectBody().toString();
        String[] splitStr = selectBody.trim().split("SELECT |FROM ");
        String[] splitValues=splitStr[1].trim().split("\\s*,\\s*");
        Expression exp;
        for(String col:splitValues){
            try {
              exp = CCJSqlParserUtil.parseExpression(col);
              if(!(exp instanceof Column)){
                throw new ParseException(exp + "is not a supported expression in Davisbase");
              }
            }catch(JSQLParserException e){
              if(!col.equalsIgnoreCase("*")){
                throw new ParseException(e);
              }
          }
        }
        SelectCommandRepresentation select = new SelectCommandRepresentation(
          selectStatement.toString(),
          splitStr[2],
          Arrays.asList(splitValues),
          splitValues[0].equalsIgnoreCase("*") ? true : false
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

  private List<Expression> getExpressions(String vals) throws ParseException {
    String[] splitValues=vals.trim().split("\\s*,\\s*");
    List<Expression> valuesExpressions = new ArrayList<>();
    Expression exp;
    try{
      for(String val: splitValues) {
        exp = CCJSqlParserUtil.parseExpression(val);
        valuesExpressions.add(getExpressionInstance(exp));
      }
    }
    catch (JSQLParserException e){
      throw new ParseException(e);
    }
    return valuesExpressions;
  }

  /**
   * @param where clause to parse
   * @return WhereExpression representation of the expression
   */
  public WhereExpression parseWhereExpression(Expression where) throws ParseWhereException{
    WhereExpression whereExpression;
    if(where instanceof EqualsTo){
      EqualsTo equals = (EqualsTo) where;

      whereExpression= new WhereExpression(
        equals.toString(),
        equals.isNot(),
        equals.getLeftExpression(),
        WhereExpression.Operator.EQUALSTO,
        getExpressionInstance(equals.getRightExpression())
      );
      return whereExpression;
    }
    else if(where instanceof NotEqualsTo){
      NotEqualsTo notEqualsTo = (NotEqualsTo) where;
      whereExpression= new WhereExpression(
        notEqualsTo.toString(),
        notEqualsTo.isNot(),
        notEqualsTo.getLeftExpression(),
        WhereExpression.Operator.NOTEQUALTO,
        getExpressionInstance(notEqualsTo.getRightExpression())
      );
      return whereExpression;
    }
    else if(where instanceof GreaterThan){
      GreaterThan greaterThan = (GreaterThan)where;
      whereExpression= new WhereExpression(
        greaterThan.toString(),
        greaterThan.isNot(),
        greaterThan.getLeftExpression(),
        WhereExpression.Operator.GREATERTHAN,
        getExpressionInstance(greaterThan.getRightExpression())
      );
      return whereExpression;
    }
    else if(where instanceof GreaterThanEquals){
      GreaterThanEquals greaterThanEquals = (GreaterThanEquals)where;
      whereExpression= new WhereExpression(
        greaterThanEquals.toString(),
        greaterThanEquals.isNot(),
        greaterThanEquals.getLeftExpression(),
        WhereExpression.Operator.GREATERTHANEQUALS,
        getExpressionInstance(greaterThanEquals.getRightExpression())
      );
      return whereExpression;
    }
    else if(where instanceof MinorThan){
      MinorThan minorThan = (MinorThan)where;
      whereExpression= new WhereExpression(
        minorThan.toString(),
        minorThan.isNot(),
        minorThan.getLeftExpression(),
        WhereExpression.Operator.LESSTHAN,
        getExpressionInstance(minorThan.getRightExpression())
      );
      return whereExpression;
    }
    else if(where instanceof MinorThanEquals){
      MinorThanEquals minorThanEquals = (MinorThanEquals)where;
      whereExpression= new WhereExpression(
        minorThanEquals.toString(),
        minorThanEquals.isNot(),
        minorThanEquals.getLeftExpression(),
        WhereExpression.Operator.LESSTHANEQUALS,
        getExpressionInstance(minorThanEquals.getRightExpression())
      );
      return whereExpression;
    }
    else{
      throw new ParseWhereException("Sorry we do not support that where expression");
    }
  }

  public Expression getExpressionInstance(Expression value) throws ParseWhereException{
    if(value instanceof DoubleValue){
      DoubleValue doubleValue = (DoubleValue) value;
      return doubleValue;
    }
    else if(value instanceof LongValue){
      LongValue longValue = (LongValue) value;
      return longValue;
    }
    else if (value instanceof DateValue){
      DateValue dateValue = (DateValue) value;
      return dateValue;
    }
    else if(value instanceof TimestampValue){
      TimestampValue timestampValue = (TimestampValue) value;
      return timestampValue;
    }
    else if(value instanceof TimeValue){
      TimeValue timeValue = (TimeValue) value;
      return timeValue;
    }
    else if(value instanceof StringValue){
      StringValue stringValue = (StringValue) value;
      return stringValue;

    }
    else if(value instanceof Column){
      Column columnValue = (Column) value;
      return columnValue;
    }
    else{
      throw new ParseWhereException("Invalid value in expression");
    }
  }
}
