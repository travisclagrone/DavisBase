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
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
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

  /**
   *
   * @param command CommandRepresentation to compile into Command
   * @return Command from given CommandRepresentation
   * @throws CompileException
   */
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
      //this is translating
      List<Byte> colIndexes = getColumIndexes(insert.getColumns()); //COLUMN INDEXES
      List<Object> values = validateTypeMatchesSchema(insert.getTable(), insert.getValues()); //VALUES ALREADY IN OBJECTS
      //TODO: ORDER THESE

      return new InsertCommand(
        validateIsDavisBaseTable(insert.getTable()),
        validateTypeMatchesSchema(insert.getTable(), insert.getValues())
      );
    }
    else if (command instanceof SelectCommandRepresentation){
      SelectCommandRepresentation select = (SelectCommandRepresentation)command;
      List<SelectCommandColumn> selectColumns = new ArrayList<>();
      for(SelectItem item: select.getColumns()){
        SelectCommandColumn col = new SelectCommandColumn(
          validateIsDavisBaseColumnWithinTable(select.getTable(), item.toString()),
          item.toString(),
          getColumnType(select.getTable(), item.toString())
        );
        selectColumns.add(col);
      }
      return new SelectCommand(
        validateIsDavisBaseTable(select.getTable()),
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
  }

  /**
   * @param dataType
   * @return DataType from given ColDataType
   * @throws CompileException
   */
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

  /**
   * @param type
   * @return DataType corresponding with Expression
   * @throws CompileException
   */
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
      }
//      else if (type instanceof NullValue) {
        //TODO: WHAT DO I RETURN IF NULL
//      }
      else {
        throw new CompileException("Invalid datatype");
      }
  }

  /**
   * @param tableName
   * @param expressions
   * @return valid list of expressions to Insert
   * @throws CompileException
   */
  public List<Object> validateTypeMatchesSchema(String tableName, List<Expression> expressions)throws CompileException{
    List<Object> objectValues = new ArrayList<>();
    DataType convertedType;
    for(Expression exp: expressions){
      String value = exp.toString();
      DataType schemaDefinedColumnType= getColumnType(tableName, value);
      convertedType = convertToDavisType(exp);
      if(!schemaDefinedColumnType.equals(convertedType)){
        convertedType = checkLongValues(schemaDefinedColumnType,convertedType, value);
      }
      //TODO: ADD NULL?
      if(convertedType.equals(DataType.TINYINT)){
        objectValues.add(Byte.parseByte(value));
      }
      else if(convertedType.equals(DataType.SMALLINT)){
        objectValues.add(Short.parseShort(value));
      }
      else if(convertedType.equals(DataType.INT)){
        objectValues.add(Integer.parseInt(value));
      }
      else if(convertedType.equals(DataType.BIGINT)){
        objectValues.add(Long.parseLong(value));
      }
      else if(convertedType.equals(DataType.FLOAT)){
        objectValues.add(Float.parseFloat(value));
      }
      else if(convertedType.equals(DataType.DOUBLE)){
        objectValues.add(Double.parseDouble(value));
      }
      else if(convertedType.equals(DataType.YEAR)){
        objectValues.add(Year.parse(value));
      }
      else if(convertedType.equals(DataType.TIME)){
        objectValues.add(LocalTime.parse(value));
      }
      else if(convertedType.equals(DataType.DATETIME)){
        objectValues.add(LocalDateTime.parse(value));
      }
      else if(convertedType.equals(DataType.DATE)){
        objectValues.add(LocalDate.parse(value));
      }
      else if(convertedType.equals(DataType.TEXT)){
        objectValues.add(value);
      }
      else{
        throw new CompileException("Not a valid DavisBase data type.");
      }
    }
    return objectValues;
  }

  /**
   * @param columnSpecs
   * @return whether columnSpecs is NOT NULL
   */
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
   * @param tableName
   * @param columnName
   * @return column index if valid column within table
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
   * @param tableName
   * @return table name if valid table name
   * @throws CompileException
   */
  public String validateIsDavisBaseTable(String tableName)throws CompileException{
    try{
      TableFile table  = context.openTableFile(CatalogTable.DAVISBASE_TABLES.name());
      while(table.goToNextRow()){
        if(table.readText(DavisBaseTablesTableColumn.TABLE_NAME.getOrdinalPosition()).equalsIgnoreCase(tableName)){
          return tableName;
        }
      }
      throw new CompileException("Table does not exist within DavisBase");
    }
    catch(IOException e){
      throw new CompileException("Unable to read table file");
    }
  }

  /**
   * @param tableName
   * @param columnName
   * @return DataType associated with the column for given table
   * @throws CompileException
   */
  public DataType getColumnType(String tableName, String columnName)throws CompileException{
    try{
      TableFile table  = context.openTableFile(CatalogTable.DAVISBASE_COLUMNS.name());
      while(table.goToNextRow()){
        if(table.readText(DavisBaseColumnsTableColumn.COLUMN_NAME.getOrdinalPosition()).equalsIgnoreCase(columnName)
          && table.readText(DavisBaseColumnsTableColumn.TABLE_NAME.getOrdinalPosition()).equalsIgnoreCase(tableName)){
          return DavisBaseColumnsTableColumn.DATA_TYPE.getDataType();
        }
      }
      throw new CompileException("Column does not exist within this table");
    }
    catch(IOException e){
      throw new CompileException("Unable to read table file");
    }
  }

  /**
   * Expression has less DataTypeValues defined than Davisbase. Perform checks to group into DavisBase DataType
   * @param schemaDefinedColumnType
   * @param convertedType
   * @param value
   * @return
   * @throws CompileException
   */
  public DataType checkLongValues(DataType schemaDefinedColumnType, DataType convertedType, String value)throws CompileException{
    long parsedVal = Long.parseLong(value);
    if(schemaDefinedColumnType.equals(DataType.TINYINT) && convertedType.equals(DataType.FLOAT)){
      if(parsedVal >= Byte.MIN_VALUE && parsedVal <= Byte.MAX_VALUE){
        return DataType.TINYINT;
      }
    }
    else if(schemaDefinedColumnType.equals(DataType.SMALLINT) && convertedType.equals(DataType.FLOAT)){
      if (parsedVal >= Short.MIN_VALUE && parsedVal <= Short.MAX_VALUE) {
        return DataType.SMALLINT;
      }
    }
    else if(schemaDefinedColumnType.equals(DataType.INT) && convertedType.equals(DataType.FLOAT)){
      if (parsedVal >= Integer.MIN_VALUE && parsedVal <= Integer.MAX_VALUE) {
        return  DataType.SMALLINT;
      }
    }
    else if(schemaDefinedColumnType.equals(DataType.BIGINT) && convertedType.equals(DataType.FLOAT)) {
      if (parsedVal >= Long.MIN_VALUE && parsedVal <= Long.MAX_VALUE) {
        return DataType.SMALLINT;
      }
    }
    else{
      throw new CompileException("Values you are trying to insert does not match the table schema");
    }
    throw new CompileException("Values you are trying to insert does not match the table schema");
  }

  public List<Byte> getColumIndexes(List<Column> columns)throws CompileException{
    List<Byte> colIndexes = new ArrayList<>();
    for(Column col: columns){
      try{
        colIndexes.add(validateIsDavisBaseColumnWithinTable(col.getTable().getName(), col.getColumnName()));
      }
      catch(CompileException e){
        new CompileException(e.getCause());
      }
    }
    return colIndexes;
  }
}
