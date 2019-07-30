package edu.utdallas.davisbase.compiler;

import edu.utdallas.davisbase.BooleanUtils;
import edu.utdallas.davisbase.DataType;
import edu.utdallas.davisbase.catalog.CatalogTable;
import edu.utdallas.davisbase.catalog.DavisBaseColumnsTableColumn;
import edu.utdallas.davisbase.catalog.DavisBaseTablesTableColumn;
import edu.utdallas.davisbase.command.*;
import edu.utdallas.davisbase.representation.*;
import edu.utdallas.davisbase.storage.Storage;
import edu.utdallas.davisbase.storage.StorageException;
import edu.utdallas.davisbase.storage.TableFile;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static org.checkerframework.checker.nullness.NullnessUtil.castNonNull;

/**
 * A compiler of {@link edu.utdallas.davisbase.representation.CommandRepresentation CommandRepresentation} to
 * {@link edu.utdallas.davisbase.command.Command Command}.
 */
public class Compiler {

  protected final Storage context;

  public Compiler(Storage context) {
    checkNotNull(context);
    this.context = context;
  }

  /**
   *
   * @param command CommandRepresentation to compile into Command
   * @return Command from given CommandRepresentation
   * @throws CompileException
   */
  @SuppressWarnings("nullness")  // WARNING Make sure the project builds _without_ this line before pushing!
  public Command compile(CommandRepresentation command) throws CompileException, StorageException,IOException {
    if(command instanceof CreateIndexCommandRepresentation){
      CreateIndexCommandRepresentation createIndex = (CreateIndexCommandRepresentation) command;
      return new CreateIndexCommand(
        createIndex.getTable(),
        createIndex.getColumn(),
        validateIsDavisBaseColumnWithinTable(createIndex.getTable(),createIndex.getColumn()),
        getColumnType(createIndex.getTable(),createIndex.getColumn())
      );
    }
    else if (command instanceof CreateTableCommandRepresentation){
      CreateTableCommandRepresentation createTable = (CreateTableCommandRepresentation)command;
      validateTableDoesNotExist(createTable.getTable());
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
      return new DeleteCommand(
        delete.getTable(),
        compileCommandWhere(delete.getTable(), delete.getWhereClause())
      );
    }
    else if (command instanceof DropTableCommandRepresentation){
      DropTableCommandRepresentation dropTable = (DropTableCommandRepresentation)command;
      return new DropTableCommand(dropTable.getTable());
    }
    else if (command instanceof ExitCommandRepresentation){
      return new ExitCommand();
    }
    else if (command instanceof InsertCommandRepresentation){
      InsertCommandRepresentation insert = (InsertCommandRepresentation)command;
      List<InsertObject> insertObjects = new ArrayList<>();
      if(insert.getColumns().isEmpty()){
        for(int lcv = 0; lcv< insert.getValues().size(); lcv++){
          String columnName = getColumnName(insert.getTable(),lcv+1); //add one to account for rowId
          @Nullable Object obj = validateTypeMatchesSchema(insert.getTable(),insert.getValues().get(lcv), columnName);
          insertObjects.add(new InsertObject(lcv, obj));
        }
      }
      else {
        for (int lcv = 0; lcv < insert.getColumns().size(); lcv++) {
          byte index = getColumnIndex(insert.getColumns().get(lcv), insert.getTable());
          @Nullable Object obj = validateTypeMatchesSchema(insert.getTable(), insert.getValues().get(lcv), insert.getColumns().get(lcv).toString());
          insertObjects.add(new InsertObject(index, obj));
        }
        Collections.sort(insertObjects);
      }
      return new InsertCommand(
        validateIsDavisBaseTable(insert.getTable()),
        insertObjects.stream()
                     .map(InsertObject::getObject)
                     .collect(Collectors.toList())
      );
    }
    else if (command instanceof SelectCommandRepresentation){
      SelectCommandRepresentation select = (SelectCommandRepresentation)command;
      List<SelectCommandColumn> selectColumns = new ArrayList<>();
      //check if * then add all columns
      for(SelectItem item: select.getColumns()){
        if(item instanceof AllColumns){
          selectColumns = getAllColumns(select.getTable());
        }
        else {
          SelectCommandColumn col = new SelectCommandColumn(
            validateIsDavisBaseColumnWithinTable(select.getTable(), item.toString()),
            item.toString(),  // QUESTION If the SelectItem is a plain column reference, does this return _exactly_ the colum name? Or do we need to switch on concrete type, cast, and then invoke a method to get the name?
            getColumnType(select.getTable(), item.toString())
          );
          selectColumns.add(col);
        }
      }
      return new SelectCommand(
        validateIsDavisBaseTable(select.getTable()),
        selectColumns,
        compileCommandWhere(select.getTable(),select.getWhereClause())
      );
    }
    else if (command instanceof ShowTablesCommandRepresentation){
      return new ShowTablesCommand();
    }
    else if (command instanceof UpdateCommandRepresentation){
      UpdateCommandRepresentation update = (UpdateCommandRepresentation)command;
      Column col = update.getColumn();
      byte colIndex =  getColumnIndex(col, update.getTable());
      UpdateCommandColumn updateCommandColumn = new UpdateCommandColumn(
        colIndex,
        validateTypeMatchesSchema(update.getTable(), update.getValue(),getColumnName(update.getTable(), colIndex))
      );
      return new UpdateCommand(
        update.getTable(),
        updateCommandColumn,
        compileCommandWhere(update.getTable(), update.getWhereClause())
      );
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
   * @param value
   * @return valid Object to Insert
   * @throws CompileException
   */
  public @Nullable Object validateTypeMatchesSchema(String tableName, Expression value, String columnName)throws CompileException, StorageException, IOException{
    if (validateNullability(tableName, columnName, value)) {
      return null;
    }

    DataType schemaDefinedColumnType= getColumnType(tableName, columnName);
    DataType convertedType = convertToDavisType(value);

    if (!schemaDefinedColumnType.equals(convertedType)) {  // Supported implicit narrowing conversions from literal values for integral and floating-point types.
      switch (convertedType) {
        case BIGINT:
          final long longValue = ((LongValue) value).getValue();

          switch (schemaDefinedColumnType) {
            case INT:
              if (Integer.MIN_VALUE <= longValue && longValue <= Integer.MAX_VALUE) {
                return (int) longValue;
              }
              throw new CompileException("Expected an INT value, but found a BIGINT value.");

            case SMALLINT:
              if (Short.MIN_VALUE <= longValue && longValue <= Short.MAX_VALUE) {
                return (short) longValue;
              }
              throw new CompileException("Expected an SMALLINT value, but found a BIGINT value.");

            case TINYINT:
              if (Byte.MIN_VALUE <= longValue && longValue <= Byte.MAX_VALUE) {
                return (byte) longValue;
              }
              throw new CompileException("Expected an TINYINT value, but found a BIGINT value.");

            default:
              throw new RuntimeException("This should never happen.");
          }

        case DOUBLE:
          final double doubleValue = ((DoubleValue) value).getValue();

          if (schemaDefinedColumnType == DataType.FLOAT) {
            if (Float.MIN_VALUE <= doubleValue && doubleValue <= Float.MAX_VALUE) {
              return (float) doubleValue;
            }
            throw new CompileException("Expected a FLOAT value, but found a DOUBLE value");
          }

          throw new RuntimeException("This should never happen.");

        default:
          throw new CompileException(
              format("DavisBase does not support implicit type coercion from %s to %s",
                  convertedType.name(),
                  schemaDefinedColumnType.name()));
      }
    }

    if (value instanceof DoubleValue) {
      DoubleValue doubleValue = (DoubleValue) value;
      return doubleValue.getValue();
    }
    else if (value instanceof LongValue) {
      LongValue longValue = (LongValue) value;
      return longValue.getValue();
    }
    else if (value instanceof DateValue) {
      DateValue dateValue = (DateValue) value;
      return dateValue.getValue();
    }
    else if (value instanceof TimestampValue) {
      TimestampValue timestampValue = (TimestampValue) value;
      return timestampValue.getValue();
    }
    else if (value instanceof TimeValue) {
      TimeValue timeValue = (TimeValue) value;
      return timeValue.getValue();
    }
    else if (value instanceof StringValue) {
      StringValue stringValue = (StringValue) value;
      return stringValue.getValue();
    }
    else if (value instanceof NullValue) {
      return null;
    }
    else {
      throw new CompileException("Invalid value in expression");
    }
  }

  /**
   * @param columnSpecs
   * @return whether columnSpecs is NOT NULL
   */
  public boolean checkIsNotNull(List<String> columnSpecs){
    if(null!= columnSpecs){
      for(int lcv = 0; lcv< columnSpecs.size()-1; lcv++){
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
  public byte validateIsDavisBaseColumnWithinTable(String tableName, String columnName)throws CompileException, StorageException, IOException{
    TableFile table  = context.openTableFile(CatalogTable.DAVISBASE_COLUMNS.name());
      while(table.goToNextRow()){
        if(castNonNull(table.readText(DavisBaseColumnsTableColumn.COLUMN_NAME.getOrdinalPosition())).equalsIgnoreCase(columnName)
        && castNonNull(table.readText(DavisBaseColumnsTableColumn.TABLE_NAME.getOrdinalPosition())).equalsIgnoreCase(tableName)){
          return castNonNull(table.readTinyInt(DavisBaseColumnsTableColumn.ORDINAL_POSITION.getOrdinalPosition()));
        }
      }
      throw new CompileException("Column " +columnName + " does not exist within this table");
  }

  /**
   * @param tableName
   * @return table name if valid table name
   * @throws CompileException
   */
  public String validateIsDavisBaseTable(String tableName)throws CompileException, StorageException, IOException{
    TableFile table  = context.openTableFile(CatalogTable.DAVISBASE_TABLES.getName());
    while(table.goToNextRow()){
      if(castNonNull(table.readText(DavisBaseTablesTableColumn.TABLE_NAME.getOrdinalPosition())).equalsIgnoreCase(tableName)){
        return tableName;
      }
    }
    throw new CompileException("Table" +tableName+ " does not exist within DavisBase");
  }

  /**
   * @param tableName
   * @param columnName
   * @return DataType associated with the column for given table
   * @throws CompileException
   */
  public DataType getColumnType(String tableName, String columnName)throws CompileException, StorageException, IOException{
    TableFile table  = context.openTableFile(CatalogTable.DAVISBASE_COLUMNS.getName());
    while(table.goToNextRow()){
      if(castNonNull(table.readText(DavisBaseColumnsTableColumn.COLUMN_NAME.getOrdinalPosition())).equalsIgnoreCase(columnName)
        && castNonNull(table.readText(DavisBaseColumnsTableColumn.TABLE_NAME.getOrdinalPosition())).equalsIgnoreCase(tableName)){
        return DataType.valueOf(castNonNull(table.readText(DavisBaseColumnsTableColumn.DATA_TYPE.getOrdinalPosition())));
      }
    }
    throw new CompileException("Column " +columnName + " does not exist within this table");
  }

  public String getColumnName(String tableName, int columnIndex)throws CompileException, StorageException, IOException{
    TableFile table  = context.openTableFile(CatalogTable.DAVISBASE_COLUMNS.getName());
    while(table.goToNextRow()){
      if(castNonNull(table.readTinyInt(DavisBaseColumnsTableColumn.ORDINAL_POSITION.getOrdinalPosition()))==columnIndex
        && castNonNull(table.readText(DavisBaseColumnsTableColumn.TABLE_NAME.getOrdinalPosition())).equalsIgnoreCase(tableName)){
        return (castNonNull(table.readText(DavisBaseColumnsTableColumn.COLUMN_NAME.getOrdinalPosition())));
      }
    }
    throw new CompileException("Column with index " + columnIndex + " does not exist within this table");
  }

  /**
   * Expression has less DataTypeValues defined than Davisbase. Perform checks to group into DavisBase DataType
   * @param schemaDefinedColumnType
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

  /**
   * @param col Column object to check
   * @param table table name to check column
   * @return byte of column index based on parameters
   * @throws CompileException
   * @throws StorageException
   * @throws IOException
   */
  public byte getColumnIndex(Column col, String table)throws CompileException,StorageException, IOException{
    return (validateIsDavisBaseColumnWithinTable(table, col.getColumnName()));
  }

  /**
   * Validate that given table exists otherwise throws exception
   * @param tableName table to check existence
   * @throws CompileException
   * @throws StorageException
   * @throws IOException
   */
  public void validateTableDoesNotExist(String tableName)throws CompileException, StorageException, IOException{
    TableFile table  = context.openTableFile(CatalogTable.DAVISBASE_TABLES.getName());
    while(table.goToNextRow()){
      if(castNonNull(table.readText(DavisBaseTablesTableColumn.TABLE_NAME.getOrdinalPosition())).equalsIgnoreCase(tableName)) {
        throw new CompileException("Table " +tableName+ " already exists.");
      }
    }
  }

  /**
   * Validate the nullability of an expression
   * @param tableName table of column
   * @param columnName column to check
   * @param value Expression to check value of
   * @return
   * @throws CompileException
   * @throws StorageException
   * @throws IOException
   */
  public boolean validateNullability(String tableName, String columnName, Expression value)throws CompileException, StorageException, IOException{
    if(getIsNullable(tableName, columnName) && value instanceof NullValue){
      return true;
    }
    if(!getIsNullable(tableName,columnName) && value instanceof NullValue){
      throw new CompileException("Column " + columnName + "is not nullable");
    }
    return false;
  }

  /**
   * Checks against DAVISBASE_COLUMNS table to see if column is nullable
   * @param tableName table to check
   * @param columnName column to check
   * @return whether column nullable
   * @throws CompileException
   * @throws StorageException
   * @throws IOException
   */
  public boolean getIsNullable(String tableName, String columnName)throws CompileException, StorageException, IOException {
    TableFile table  = context.openTableFile(CatalogTable.DAVISBASE_COLUMNS.getName());
    while(table.goToNextRow()){
      if(castNonNull(table.readText(DavisBaseColumnsTableColumn.COLUMN_NAME.getOrdinalPosition())).equalsIgnoreCase(columnName)
        && castNonNull(table.readText(DavisBaseColumnsTableColumn.TABLE_NAME.getOrdinalPosition())).equalsIgnoreCase(tableName)){
        return BooleanUtils.fromText(castNonNull(table.readText(DavisBaseColumnsTableColumn.IS_NULLABLE.getOrdinalPosition())));
      }
    }
    throw new CompileException("Column " + columnName + " does not exist within this table");
  }

  /**
   * @param tableName table to get columns
   * @return List of SelectCommandColumn object that represents all columns for given table
   * @throws StorageException
   * @throws IOException
   */
  public List<SelectCommandColumn> getAllColumns(String tableName)throws StorageException, IOException{
    List<SelectCommandColumn> selectColumns = new ArrayList<>();
    TableFile table  = context.openTableFile(CatalogTable.DAVISBASE_COLUMNS.getName());
    while(table.goToNextRow()){
      if(castNonNull(table.readText(DavisBaseColumnsTableColumn.TABLE_NAME.getOrdinalPosition())).equalsIgnoreCase(tableName)){
         SelectCommandColumn select = new SelectCommandColumn(
          castNonNull(table.readTinyInt(DavisBaseColumnsTableColumn.ORDINAL_POSITION.getOrdinalPosition())),
          castNonNull(table.readText(DavisBaseColumnsTableColumn.COLUMN_NAME.getOrdinalPosition())),
          DataType.valueOf(castNonNull(table.readText(DavisBaseColumnsTableColumn.DATA_TYPE.getOrdinalPosition())))
        );
         selectColumns.add(select);
      }
    }
    return selectColumns;
  }

  /**
   * @param tableName name of table
   * @param where WhereExpression representation of where clause
   * @return compiled CommandWhere of where clause
   * @throws IOException
   * @throws StorageException
   * @throws CompileException
   */
  public @Nullable CommandWhere compileCommandWhere(String tableName, @Nullable WhereExpression where)throws IOException, StorageException,CompileException{
    if(null==where){
      return null;
    }
    byte columnIndex = getColumnIndex(where.getColumn(), tableName);
    String columnName = getColumnName(tableName, columnIndex);
    CommandWhereColumn leftColumnReference = new CommandWhereColumn(
      columnIndex,
      columnName,
      getColumnType(tableName,columnName),
      getIsNullable(tableName, columnName ),
      false//TODO: COME BACK AND FIX THIS ONCE INDEX IMPLEMENTED
    );
    return new CommandWhere(
      leftColumnReference,
      returnCommandOperator(where.getOperator(), where.isNot()),
      validateTypeMatchesSchema(tableName, where.getValue(), columnName)
    );
  }

  /**
   * @param op WhereExpression Operator enum
   * @return the CommandWhere Operator Enum given the WhereExpression Operator
   * @throws CompileException
   */
  public CommandWhere.Operator returnCommandOperator(WhereExpression.Operator op, boolean isNot)throws CompileException{
    switch (op){
      case EQUALSTO:
        if(isNot){
          return CommandWhere.Operator.NOT_EQUAL;
        }else{
          return CommandWhere.Operator.EQUAL;
        }
      case NOTEQUALTO:
        if(isNot){
          return CommandWhere.Operator.EQUAL;
        }
        else{
          return CommandWhere.Operator.NOT_EQUAL;
        }
      case GREATERTHAN:
        if(isNot){
          return CommandWhere.Operator.LESS_THAN_OR_EQUAL;
        }
        else{
          return CommandWhere.Operator.GREATER_THAN;
        }
      case GREATERTHANEQUALS:
        if(isNot){
          return CommandWhere.Operator.LESS_THAN;
        }
        else{
          return CommandWhere.Operator.GREATER_THAN_OR_EQUAL;
        }
      case LESSTHAN:
        if(isNot){
          return CommandWhere.Operator.GREATER_THAN_OR_EQUAL;
        }
        else{
          return CommandWhere.Operator.LESS_THAN;
        }
      case LESSTHANEQUALS:
        if(isNot){
          return CommandWhere.Operator.GREATER_THAN;
        }
        else{
          return CommandWhere.Operator.LESS_THAN_OR_EQUAL;
        }
      default:
        throw new CompileException("Unrecognized operator");
    }
  }

}
