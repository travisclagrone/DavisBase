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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.Nullable;
import static com.google.common.base.Preconditions.checkArgument;
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
      // COMBAK Implement compile(CreateIndexCommandRepresentation)
    }
    else if (command instanceof CreateTableCommandRepresentation){
      CreateTableCommandRepresentation createTable = (CreateTableCommandRepresentation)command;
      // TODO Validate that the table does not already exist.
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
      // COMBAK Implement compile(DeleteCommandRepresentation)
    }
    else if (command instanceof DropTableCommandRepresentation){
      DropTableCommandRepresentation dropTable = (DropTableCommandRepresentation)command;
      throw new NotImplementedException();
      // COMBAK Implement compile(DropTableCommandRepresentation)
    }
    else if (command instanceof ExitCommandRepresentation){
      return new ExitCommand();
    }
    else if (command instanceof InsertCommandRepresentation){
      InsertCommandRepresentation insert = (InsertCommandRepresentation)command;
      List<InsertObject> insertObjects = new ArrayList<>();
      // FIXME Since we're already using `<` (vs. `<=`), it should be `size()` instead of `size()-1`, right?
      for(int lcv = 0; lcv<insert.getColumns().size()-1; lcv++){
        byte index = getColumnIndex(insert.getColumns().get(lcv)); //get Column index
        @Nullable Object obj = validateTypeMatchesSchema(insert.getTable(), insert.getValues().get(lcv));
        insertObjects.add(new InsertObject(index, obj));
      }
      Collections.sort(insertObjects);
      return new InsertCommand(
        validateIsDavisBaseTable(insert.getTable()),
        insertObjects.stream()
          .map(InsertObject::getObject).collect(Collectors.toList())
      );
    }
    else if (command instanceof SelectCommandRepresentation){
      SelectCommandRepresentation select = (SelectCommandRepresentation)command;
      List<SelectCommandColumn> selectColumns = new ArrayList<>();
      for(SelectItem item: select.getColumns()){
        SelectCommandColumn col = new SelectCommandColumn(
          validateIsDavisBaseColumnWithinTable(select.getTable(), item.toString()),
          item.toString(),  // QUESTION If the SelectItem is a plain column reference, does this return _exactly_ the colum name? Or do we need to switch on concrete type, cast, and then invoke a method to get the name?
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
      // COMBAK Implement compile(UpdateCommandRepresentation)
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
    checkArgument(!(type instanceof NullValue));

    if (type instanceof DoubleValue) {
      return DataType.DOUBLE;
    } else if (type instanceof LongValue) {
      return DataType.BIGINT;
    } else if (type instanceof DateValue) {
      return DataType.DATE;
    } else if (type instanceof TimestampValue) {
      return DataType.DATETIME;
    } else if (type instanceof TimeValue) {
      return DataType.TIME;
    } else if (type instanceof StringValue) {
      return DataType.TEXT;
    }
    else {
      throw new CompileException("Invalid datatype");
    }
  }

  /**
   * @param tableName
   * @param exp
   * @return valid Object to Insert
   * @throws CompileException
   */
  public @Nullable Object validateTypeMatchesSchema(String tableName, Expression exp)throws CompileException{
    String value = exp.toString();
    // TODO Before checking schema-defined column type, first check schema-defined nullability. If nullable, then check if is `NullValue` and return Java `null` if so. If not nullable, then validate that is not `NullValue` and throw an exception otherwise.

    DataType schemaDefinedColumnType= getColumnType(tableName, value);  // FIXME Replace `value` with `columnName` here. Have to add a parameter to `validateTypeMatchesSchema(*)` in order to do so.
    DataType convertedType = convertToDavisType(exp);
    if(!schemaDefinedColumnType.equals(convertedType)){
      convertedType = checkLongValues(schemaDefinedColumnType, value);
      // TODO Handle case where schema-defined type is FLOAT and converted type is DOUBLE.
      // TODO Handle case where schema-defined type is DATETIME and converted type is DATE.
    }

    // FIXME All of these cases should be getting the value (using the #getValue() method) and then casting as necessary, rather than parsing the string. (JSqlParser already did that)
    if(convertedType.equals(DataType.TINYINT)){
      return (Byte.parseByte(value));
    }
    else if(convertedType.equals(DataType.SMALLINT)){
      return(Short.parseShort(value));
    }
    else if(convertedType.equals(DataType.INT)){
      return(Integer.parseInt(value));
    }
    else if(convertedType.equals(DataType.BIGINT)){
      return(Long.parseLong(value));
    }
    else if(convertedType.equals(DataType.FLOAT)){
      return(Float.parseFloat(value));
    }
    else if(convertedType.equals(DataType.DOUBLE)){
      return(Double.parseDouble(value));
    }
    else if(convertedType.equals(DataType.YEAR)){
      return(Year.parse(value));
    }
    else if(convertedType.equals(DataType.TIME)){
      return(LocalTime.parse(value));
    }
    else if(convertedType.equals(DataType.DATETIME)){
      return(LocalDateTime.parse(value));
    }
    else if(convertedType.equals(DataType.DATE)){
      return(LocalDate.parse(value));
    }
    else if(convertedType.equals(DataType.TEXT)){
      return(value);
    }
    else{
      throw new CompileException("Not a valid DavisBase data type.");
    }
  }

  /**
   * @param columnSpecs
   * @return whether columnSpecs is NOT NULL
   */
  public boolean checkIsNotNull(List<String> columnSpecs){
    if(null!= columnSpecs){
      // FIXME Since we're already using `<` (vs. `<=`), it should be `size()-1` instead of `size()-2`, right?
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
    try (TableFile table  = context.openTableFile(CatalogTable.DAVISBASE_COLUMNS.getName())) {
      while(table.goToNextRow()){
        if(table.readText(DavisBaseColumnsTableColumn.COLUMN_NAME.getOrdinalPosition()).equalsIgnoreCase(columnName)
        && table.readText(DavisBaseColumnsTableColumn.TABLE_NAME.getOrdinalPosition()).equalsIgnoreCase(tableName)){
          return table.readTinyInt(DavisBaseColumnsTableColumn.ORDINAL_POSITION.getOrdinalPosition());
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
          return DataType.valueOf(table.readText(DavisBaseColumnsTableColumn.DATA_TYPE.getOrdinalPosition()));
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
  public DataType checkLongValues(DataType schemaDefinedColumnType, String value)throws CompileException{
    long parsedVal = Long.parseLong(value);
    if(schemaDefinedColumnType.equals(DataType.TINYINT)){
      if(parsedVal >= Byte.MIN_VALUE && parsedVal <= Byte.MAX_VALUE){
        return DataType.TINYINT;
      }
    }
    else if(schemaDefinedColumnType.equals(DataType.SMALLINT)){
      if (parsedVal >= Short.MIN_VALUE && parsedVal <= Short.MAX_VALUE) {
        return DataType.SMALLINT;
      }
    }
    else if(schemaDefinedColumnType.equals(DataType.INT)){
      if (parsedVal >= Integer.MIN_VALUE && parsedVal <= Integer.MAX_VALUE) {
        return  DataType.INT;
      }
    }
    else if(schemaDefinedColumnType.equals(DataType.BIGINT)) {
      if (parsedVal >= Long.MIN_VALUE && parsedVal <= Long.MAX_VALUE) {
        return DataType.BIGINT;
      }
    }
    else{
      throw new CompileException("Values you are trying to insert does not match the table schema");
    }
    throw new CompileException("Values you are trying to insert does not match the table schema");
  }

  public byte getColumnIndex(Column col)throws CompileException{
    return (validateIsDavisBaseColumnWithinTable(col.getTable().getName(), col.getColumnName()));
  }
}
