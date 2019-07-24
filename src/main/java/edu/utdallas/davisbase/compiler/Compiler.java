package edu.utdallas.davisbase.compiler;

import edu.utdallas.davisbase.DataType;
import edu.utdallas.davisbase.NotImplementedException;
import edu.utdallas.davisbase.catalog.CatalogTable;
import edu.utdallas.davisbase.catalog.DavisBaseColumnsTableColumn;
import edu.utdallas.davisbase.catalog.DavisBaseTablesTableColumn;
import edu.utdallas.davisbase.command.*;
import edu.utdallas.davisbase.representation.*;
import edu.utdallas.davisbase.storage.Storage;
import edu.utdallas.davisbase.storage.TableFile;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A compiler of {@link edu.utdallas.davisbase.representation.CommandRepresentation CommandRepresentation} to
 * {@link edu.utdallas.davisbase.command.Command Command}.
 */
public class Compiler {

  protected final CompilerConfiguration configuration;
  protected final Storage context;

  public Compiler(CompilerConfiguration configuration, Storage context) {
    checkNotNull(configuration);
    this.configuration = configuration;
    this.context = context;
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
//      InsertCommandRepresentation insert = (InsertCommandRepresentation)command;
//      validateIsDavisBaseTable(insert.getTable());
//      return new InsertCommand(
//        insert.getTable(),
//        insert.getValues()
//        //convertToDavisType(((SelectExpressionItem)item).getExpression())
//        //implement validateTypeMatchesSchema
//      );
    }
    else if (command instanceof SelectCommandRepresentation){
      SelectCommandRepresentation select = (SelectCommandRepresentation)command;
      validateIsDavisBaseTable(select.getTable());
      List<SelectCommandColumn> selectColumns = new ArrayList<>();
      for(SelectItem item: select.getColumns()){
        SelectCommandColumn col = new SelectCommandColumn(
          validateIsDavisBaseColumnWithinTable(select.getTable(), item.toString()),
          item.toString(),
          getColumnType(item.toString())
        );
        selectColumns.add(col);
      }
      return new SelectCommand(
        select.getTable(),
        selectColumns
      );
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

  public DataType convertToDavisType(Expression type)throws CompileException{
      if (type instanceof DoubleValue) {
        return DataType.DOUBLE;
      } else if (type instanceof LongValue) {
        return DataType.FLOAT;
      } else if (type instanceof DateValue) {
        return DataType.DATE;
      } else if (type instanceof TimestampValue) {
        return DataType.DATETIME;
      } else if (type instanceof TimeValue) {
        return DataType.TIME;
      } else if (type instanceof StringValue) {
        return DataType.TEXT;
      }else {
        throw new CompileException("Invalid datatype");
      }
  }

  public void validateTypeMatchesSchema(DataType type)throws CompileException{
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

  /**
   * Return column index if valid column within table
   * @param tableName
   * @param columnName
   * @return
   * @throws CompileException
   */
  public byte validateIsDavisBaseColumnWithinTable(String tableName, String columnName)throws CompileException{
    try{
      TableFile table  = context.openTableFile(CatalogTable.DAVISBASE_COLUMNS.name());
      while(table.goToNextRow()){
        if(table.readText(DavisBaseColumnsTableColumn.COLUMN_NAME.getOrdinalPosition()).equalsIgnoreCase(columnName)
        && table.readText(DavisBaseColumnsTableColumn.TABLE_NAME.getOrdinalPosition()).equalsIgnoreCase(tableName)){
          return DavisBaseColumnsTableColumn.COLUMN_NAME.getOrdinalPosition();
        }
      }
      throw new CompileException("Column does not exist within this table");
    }
    catch(IOException e){
      throw new CompileException("Unable to read table file");
    }
  }

  /**
   * Return table ordinal position if valid table
   * @param tableName
   * @return
   * @throws CompileException
   */
  public byte validateIsDavisBaseTable(String tableName)throws CompileException{
    try{
      TableFile table  = context.openTableFile(CatalogTable.DAVISBASE_TABLES.name());
      while(table.goToNextRow()){
        if(table.readText(DavisBaseTablesTableColumn.TABLE_NAME.getOrdinalPosition()).equalsIgnoreCase(tableName)){
          return DavisBaseTablesTableColumn.TABLE_NAME.getOrdinalPosition();
        }
      }
      throw new CompileException("Table does not exist within DavisBase");
    }
    catch(IOException e){
      throw new CompileException("Unable to read table file");
    }
  }

  public DataType getColumnType(String columnName)throws CompileException{
    try{
      TableFile table  = context.openTableFile(CatalogTable.DAVISBASE_COLUMNS.name());
      while(table.goToNextRow()){
        if(table.readText(DavisBaseColumnsTableColumn.COLUMN_NAME.getOrdinalPosition()).equalsIgnoreCase(columnName)){
          return DavisBaseColumnsTableColumn.DATA_TYPE.getDataType();
        }
      }
      throw new CompileException("Column does not exist within this table");
    }
    catch(IOException e){
      throw new CompileException("Unable to read table file");
    }
  }

}
