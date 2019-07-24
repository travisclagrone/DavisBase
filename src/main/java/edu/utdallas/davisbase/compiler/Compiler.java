package edu.utdallas.davisbase.compiler;

import edu.utdallas.davisbase.DataType;
import edu.utdallas.davisbase.NotImplementedException;
import edu.utdallas.davisbase.catalog.CatalogTable;
import edu.utdallas.davisbase.catalog.CatalogTableColumn;
import edu.utdallas.davisbase.command.*;
import edu.utdallas.davisbase.representation.*;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A compiler of {@link edu.utdallas.davisbase.representation.CommandRepresentation CommandRepresentation} to
 * {@link edu.utdallas.davisbase.command.Command Command}.
 */
public class Compiler {

  protected final CompilerConfiguration configuration;

  public Compiler(CompilerConfiguration configuration) {
    checkNotNull(configuration);
    this.configuration = configuration;
  }

  public Command compile(CommandRepresentation command) throws CompileException {
    if(command instanceof CreateIndexCommandRepresentation){
      CreateIndexCommandRepresentation createIndex = (CreateIndexCommandRepresentation) command;
      throw new NotImplementedException();
      //TODO: Implement for part 2
    }
    else if (command instanceof CreateTableCommandRepresentation){
      CreateTableCommandRepresentation createTable = (CreateTableCommandRepresentation)command;
      List<CreateTableCommandColumn> columnSchemas = new ArrayList<>();
      for(ColumnDefinition colDef: createTable.getDefinitions()){
        CreateTableCommandColumn col = new CreateTableCommandColumn(
          colDef.getColumnName(),
          getDavisBaseType(colDef.getColDataType()),
          checkIsNotNull(colDef.getColumnSpecStrings()));
        columnSchemas.add(col);
      }
      return new CreateTableCommand(createTable.getTable(),columnSchemas);
    }
    else if (command instanceof DeleteCommandRepresentation){
      DeleteCommandRepresentation delete= (DeleteCommandRepresentation)command;
      throw new NotImplementedException();
      //TODO: Implement for part 2
    }
    else if (command instanceof DropTableCommandRepresentation){
      DropTableCommandRepresentation dropTable = (DropTableCommandRepresentation)command;
      throw new NotImplementedException();
      //TODO: Implement for part 2
    }
    else if (command instanceof ExitCommandRepresentation){
      return new ExitCommand();
    }
    else if (command instanceof InsertCommandRepresentation){
      InsertCommandRepresentation insert = (InsertCommandRepresentation)command;
//      return new InsertCommand(
//        insert.getTable(),
//        0,//TODO: implement method to do mapping to get rowid
//        convertToDavisType(insert.getValues())
//      );
    }
    else if (command instanceof SelectCommandRepresentation){
      SelectCommandRepresentation select = (SelectCommandRepresentation)command;
      List<SelectCommandColumn> selectColumns = new ArrayList<>();
      for(SelectItem item: select.getColumns()){
//        SelectCommandColumn col = new SelectCommandColumn(
//          ,//TODO: Implement method to getIndex
//          ((SelectExpressionItem)item).getExpression().toString(),//name
//          //TODO: Implement method to get Datatype
//        );
//        selectColumns.add(col);
      }
      return new SelectCommand(
        select.getTable(),
        selectColumns
      ); //TODO
    }
    else if (command instanceof ShowTablesCommandRepresentation){
      return new ShowTablesCommand();
    }
    else if (command instanceof UpdateCommandRepresentation){
      UpdateCommandRepresentation update = (UpdateCommandRepresentation)command;
      throw new NotImplementedException();
      //TODO: Implement for part 2
    }
    else{
      throw new CompileException("Unrecognized command. Unable to compile. ");
    }
    return new ExitCommand(); //TODO: Remove later
  }

  public DataType getDavisBaseType(ColDataType dataType)throws CompileException {
    String type = dataType.getDataType();
    if(type.equalsIgnoreCase(DataType.TINYINT.name())){
      return DataType.TINYINT;
    }
    else if(type.equalsIgnoreCase(DataType.SMALLINT.name())){
      return DataType.SMALLINT;
    }
    else if(type.equalsIgnoreCase(DataType.INT.name())){
      return DataType.INT;
    }
    else if(type.equalsIgnoreCase(DataType.BIGINT.name())){
      return DataType.BIGINT;
    }
    else if(type.equalsIgnoreCase(DataType.FLOAT.name())){
      return DataType.FLOAT;
    }
    else if(type.equalsIgnoreCase(DataType.DOUBLE.name())){
      return DataType.DOUBLE;
    }
    else if(type.equalsIgnoreCase(DataType.YEAR.name())){
      return DataType.YEAR;
    }
    else if(type.equalsIgnoreCase(DataType.TIME.name())){
      return DataType.TIME;
    }
    else if(type.equalsIgnoreCase(DataType.DATETIME.name())){
      return DataType.DATETIME;
    }
    else if(type.equalsIgnoreCase(DataType.DATE.name())){
      return DataType.DATE;
    }
    else if(type.equalsIgnoreCase(DataType.TEXT.name())){
      return DataType.TEXT;
    }
    else{
      throw new CompileException("Not a valid DavisBase data type.");
    }
  }

  public List<DataType> convertToDavisType(List<Expression> types)throws CompileException{
    List<DataType> davisTypes = new ArrayList<>();
    for(Expression value: types){
      if (value instanceof DoubleValue) {
        davisTypes.add(DataType.DOUBLE);
      } else if (value instanceof LongValue) {
        davisTypes.add(DataType.FLOAT);
      } else if (value instanceof DateValue) {
        davisTypes.add(DataType.DATE);
      } else if (value instanceof TimestampValue) {
        davisTypes.add(DataType.DATETIME);
      } else if (value instanceof TimeValue) {
        davisTypes.add(DataType.TIME);
      } else if (value instanceof StringValue) {
        davisTypes.add(DataType.TEXT);
      }else if (value instanceof NullValue) {
        davisTypes.add(DataType.TEXT); //TODO: Fix
        //QUESTION we don't have a null type?
      }else {
        throw new CompileException("Invalid datatype");
      }
    }
    return davisTypes;
  }

  public boolean checkIsNotNull(List<String> columnSpecs){
    if(null!= columnSpecs){
      for(int lcv = 0; lcv< columnSpecs.size()-2; lcv++){
        if (columnSpecs.get(lcv).equalsIgnoreCase("NOT") &&
        columnSpecs.get(lcv+1).equalsIgnoreCase("NULL")){
          return true;
        }
      }
    }
    return false;
  }

  public void validateIsDavisBaseColumn(CatalogTable catalogTable, String columnName)throws CompileException{
    for(CatalogTableColumn catalogTableColumn :catalogTable.getColumns()){
      if(catalogTableColumn.getName().equalsIgnoreCase(columnName)){
        break;
      }
    }
    throw new CompileException("Column does not exist within tabble");
  }

}
