package edu.utdallas.davisbase.compiler;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static org.checkerframework.checker.nullness.NullnessUtil.castNonNull;

import edu.utdallas.davisbase.BooleanUtils;
import edu.utdallas.davisbase.catalog.CatalogTable;
import edu.utdallas.davisbase.catalog.DavisBaseColumnsTableColumn;
import edu.utdallas.davisbase.catalog.DavisBaseTablesTableColumn;
import edu.utdallas.davisbase.command.*;
import edu.utdallas.davisbase.DataType;
import edu.utdallas.davisbase.YearUtils;
import edu.utdallas.davisbase.representation.*;
import edu.utdallas.davisbase.storage.Storage;
import edu.utdallas.davisbase.storage.StorageException;
import edu.utdallas.davisbase.storage.TableFile;
import java.io.IOException;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A compiler of {@link edu.utdallas.davisbase.representation.CommandRepresentation CommandRepresentation}
 * to {@link edu.utdallas.davisbase.command.Command Command}.
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
  public Command compile(CommandRepresentation command) throws CompileException, StorageException, IOException {
    if (command instanceof CreateIndexCommandRepresentation) {
      CreateIndexCommandRepresentation createIndex = (CreateIndexCommandRepresentation) command;
      return new CreateIndexCommand(createIndex.getTable(), createIndex.getIndex(),
          createIndex.getColumn(),
          getColumnIndex(createIndex.getTable(), createIndex.getColumn()),
          getColumnType(createIndex.getTable(), createIndex.getColumn()));
    }
    else if (command instanceof CreateTableCommandRepresentation) {
      CreateTableCommandRepresentation createTable = (CreateTableCommandRepresentation) command;
      if(isExistingTable(createTable.getTable())){
        throw new CompileException("Table already exists within DavisBase with that name");
      }
      List<CreateTableCommandColumn> columnSchemas = new ArrayList<>();
      for (ColumnDefinition colDef : createTable.getDefinitions()) {
        boolean isPrimaryKey =  isPrimaryKey(colDef.getColumnSpecStrings(), createTable.getIndex(), colDef.getColumnName());
        CreateTableCommandColumn col = new CreateTableCommandColumn(
            colDef.getColumnName(),
            getCorrespondingDataTypeFromColDataType(colDef.getColDataType()),
            isPrimaryKey ? true : checkIsNotNull(colDef.getColumnSpecStrings()),
            isPrimaryKey ? true: isUnique(colDef.getColumnSpecStrings()),
            isPrimaryKey
        );
        columnSchemas.add(col);
      }
      return new CreateTableCommand(createTable.getTable(), columnSchemas);
    }
    else if (command instanceof DeleteCommandRepresentation) {
      DeleteCommandRepresentation delete = (DeleteCommandRepresentation) command;
      checkNotCatalogTable(delete.getTable());
      checkTableExists(delete.getTable());
      return new DeleteCommand(
          delete.getTable(),
          compileCommandWhere(delete.getTable(),
          delete.getWhereClause()));
    }
    else if (command instanceof DropTableCommandRepresentation) {
      DropTableCommandRepresentation dropTable = (DropTableCommandRepresentation) command;
      checkNotCatalogTable(dropTable.getTable());
      checkTableExists(dropTable.getTable());
      return new DropTableCommand(dropTable.getTable());
    }
    else if (command instanceof ExitCommandRepresentation) {
      return new ExitCommand();
    }
    else if (command instanceof InsertCommandRepresentation) {
      InsertCommandRepresentation insert = (InsertCommandRepresentation) command;
      checkNotCatalogTable(insert.getTable());
      checkTableExists(insert.getTable());
      List<InsertObject> insertObjects = new ArrayList<>();
      if (insert.getColumns().isEmpty()) {
        checkInsertValuesMatchesCountColumns(insert.getTable(), insert.getValues().size());
        for (int lcv = 0; lcv < insert.getValues().size(); lcv++) {
          String columnName = getColumnName(insert.getTable(), lcv + 1);  // add one to account for rowId
          @Nullable
          Object obj = getValidObjectMatchingSchema(
              insert.getTable(),
              insert.getValues().get(lcv),
              columnName);
          if(null!=obj){
            checkUniqueness(insert.getTable(), columnName, obj);
          }
          insertObjects.add(new InsertObject(lcv, obj));
        }
      }
      else {
        checkInsertValuesMatchesCountColumns(insert.getTable(), insert.getValues().size());
        for (int lcv = 0; lcv < insert.getColumns().size(); lcv++) {
          byte index = getColumnIndex(insert.getTable(),insert.getColumns().get(lcv).getColumnName());
          String columnName=insert.getColumns().get(lcv).toString();
          @Nullable
          Object obj = getValidObjectMatchingSchema(
              insert.getTable(),
              insert.getValues().get(lcv),
              columnName
          );
          if(null!=obj){
            checkUniqueness(insert.getTable(), columnName, obj);
          }
          insertObjects.add(new InsertObject(index, obj));
        }
        Collections.sort(insertObjects);
      }
      return new InsertCommand(
          getValidatedDavisBaseTableName(insert.getTable()),
          insertObjects.stream()
                       .map(InsertObject::getObject)
                       .collect(Collectors.toList()));
    }
    else if (command instanceof SelectCommandRepresentation) {
      SelectCommandRepresentation select = (SelectCommandRepresentation) command;
      checkTableExists(select.getTable());
      List<SelectCommandColumn> selectColumns = new ArrayList<>();
      // check if * then add all columns
      for (SelectItem item : select.getColumns()) {
        if (item instanceof AllColumns) {
          selectColumns = getAllColumns(select.getTable());
        }
        else {
          SelectCommandColumn col = new SelectCommandColumn(
              getColumnIndex(select.getTable(), item.toString()),
              item.toString(),
              getColumnType(select.getTable(), item.toString()));
          selectColumns.add(col);
        }
      }
      return new SelectCommand(
          getValidatedDavisBaseTableName(select.getTable()),
          selectColumns,
          compileCommandWhere(select.getTable(), select.getWhereClause()));
    }
    else if (command instanceof ShowTablesCommandRepresentation) {
      return new ShowTablesCommand();
    }
    else if (command instanceof UpdateCommandRepresentation) {
      UpdateCommandRepresentation update = (UpdateCommandRepresentation) command;
      checkNotCatalogTable(update.getTable());
      checkTableExists(update.getTable());
      List<UpdateCommandColumn> updateCommandColumns = new ArrayList<>();
      List<Column> columnRepresentations = update.getColumns();
      List<Expression> valuesList = update.getValues();
      for (int lcv = 0; lcv < columnRepresentations.size(); lcv++) {
        Column col = columnRepresentations.get(lcv);
        byte colIndex = getColumnIndex(update.getTable(),col.getColumnName());
        String columnName=getColumnName(update.getTable(), colIndex);
        @Nullable
        Object obj = getValidObjectMatchingSchema(update.getTable(),valuesList.get(lcv), columnName);
        if(null!=obj){
          checkUniqueness(update.getTable(), columnName, obj);
        }
        updateCommandColumns.add(new UpdateCommandColumn(colIndex,obj));
      }
      return new UpdateCommand(
          update.getTable(),
          updateCommandColumns,
          compileCommandWhere(update.getTable(), update.getWhereClause()));
    }
    else {
      throw new CompileException("Unrecognized command. Unable to compile. ");
    }
  }

  /**
   * @param dataType
   * @return DataType from given ColDataType
   * @throws CompileException
   */
  private DataType getCorrespondingDataTypeFromColDataType(ColDataType dataType) throws CompileException {
    String type = dataType.getDataType();
    if (type.equalsIgnoreCase(DataType.TINYINT.name())) {
      return DataType.TINYINT;
    }
    else if (type.equalsIgnoreCase(DataType.SMALLINT.name())) {
      return DataType.SMALLINT;
    }
    else if (type.equalsIgnoreCase(DataType.INT.name())) {
      return DataType.INT;
    }
    else if (type.equalsIgnoreCase(DataType.BIGINT.name())) {
      return DataType.BIGINT;
    }
    else if (type.equalsIgnoreCase(DataType.FLOAT.name())) {
      return DataType.FLOAT;
    }
    else if (type.equalsIgnoreCase(DataType.DOUBLE.name())) {
      return DataType.DOUBLE;
    }
    else if (type.equalsIgnoreCase(DataType.YEAR.name())) {
      return DataType.YEAR;
    }
    else if (type.equalsIgnoreCase(DataType.TIME.name())) {
      return DataType.TIME;
    }
    else if (type.equalsIgnoreCase(DataType.DATETIME.name())) {
      return DataType.DATETIME;
    }
    else if (type.equalsIgnoreCase(DataType.DATE.name())) {
      return DataType.DATE;
    }
    else if (type.equalsIgnoreCase(DataType.TEXT.name())) {
      return DataType.TEXT;
    }
    else {
      throw new CompileException("Not a valid DavisBase data type.");
    }
  }

  /**
   * @param type
   * @return DataType corresponding with Expression
   * @throws CompileException
   */
  private DataType getDataTypeFromExpression(Expression type) throws CompileException {
    checkArgument(!(type instanceof NullValue));

    if (type instanceof DoubleValue) {
      return DataType.DOUBLE;
    }
    else if (type instanceof LongValue) {
      return DataType.BIGINT;
    }
    else if (type instanceof DateValue) {
      return DataType.DATE;
    }
    else if (type instanceof TimestampValue) {
      return DataType.DATETIME;
    }
    else if (type instanceof TimeValue) {
      return DataType.TIME;
    }
    else if (type instanceof StringValue) {
      return DataType.TEXT;
    }
    else {
      throw new CompileException("Invalid datatype");
    }
  }

  /**
   * @param tableName
   * @param value
   * @return valid Object from given Expression value
   * @throws CompileException
   */
  private @Nullable Object getValidObjectMatchingSchema(String tableName, Expression value,
                                                        String columnName) throws CompileException, StorageException, IOException {
    if (isNullValue(tableName, columnName, value)) {
      return null;
    }
    DataType schemaDefinedColumnType = getColumnType(tableName, columnName);
    DataType convertedType = getDataTypeFromExpression(value);

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

            case YEAR:
              if ((YearUtils.YEAR_OFFSET + Byte.MIN_VALUE) <= longValue && longValue <= (YearUtils.YEAR_OFFSET + Byte.MAX_VALUE)) {
                assert Integer.MIN_VALUE <= longValue && longValue <= Integer.MAX_VALUE : "Literal BIGINT value for DavisBase YEAR data type cannot be safely cast to an int";
                return Year.of((int) longValue);
              }
              throw new CompileException("Expected an YEAR value between 1873 and 2127, but found value out of range.");

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
                  convertedType.name(), schemaDefinedColumnType.name()));
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
      return dateValue.getValue().toLocalDate();
    }
    else if (value instanceof TimestampValue) {
      TimestampValue timestampValue = (TimestampValue) value;
      return timestampValue.getValue().toLocalDateTime();
    }
    else if (value instanceof TimeValue) {
      TimeValue timeValue = (TimeValue) value;
      return timeValue.getValue().toLocalTime();
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
   * @param columnSpecs column constraints for some given column
   * @return whether column is NOT NULL
   */
  public boolean checkIsNotNull(List<String> columnSpecs) {
    if (null != columnSpecs) {
      for (int lcv = 0; lcv < columnSpecs.size() - 1; lcv++) {
        if (columnSpecs.get(lcv).equalsIgnoreCase("NOT")
            && columnSpecs.get(lcv + 1).equalsIgnoreCase("NULL")) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * @param columnSpecs column constraints for give column
   * @return whether column is UNIQUE
   */
  private boolean isUnique(List<String> columnSpecs) {
    if (null != columnSpecs) {
      if(columnSpecs.contains("UNIQUE")){
        return true;
      }
    }
    return false;
  }

  /**
   * @param columnSpecs column constraints for give column
   * @return whether column is PRIMARY KEY
   */
  private boolean isPrimaryKey(List<String> columnSpecs, Index index, String columnName) {
    if (null != columnSpecs) {
      for (int lcv = 0; lcv < columnSpecs.size() - 1; lcv++) {
        if (columnSpecs.get(lcv).equalsIgnoreCase("PRIMARY")
          && columnSpecs.get(lcv + 1).equalsIgnoreCase("KEY")) {
          return true;
        }
      }
    }
    if(null!= index && index.getColumnsNames().get(0).equalsIgnoreCase(columnName)){
        return true;
    }
    return false;
  }

  /**
   * @param tableName
   * @param columnName
   * @return column index if valid column within table
   * @throws CompileException
   */
  private byte getColumnIndex(String tableName, String columnName) throws CompileException, StorageException, IOException {
    TableFile table = context.openTableFile(CatalogTable.DAVISBASE_COLUMNS.name());
    while (table.goToNextRow()) {
      if (castNonNull(table.readText(DavisBaseColumnsTableColumn.COLUMN_NAME.getOrdinalPosition()))
          .equalsIgnoreCase(columnName)
          && castNonNull(
              table.readText(DavisBaseColumnsTableColumn.TABLE_NAME.getOrdinalPosition()))
                  .equalsIgnoreCase(tableName)) {
        return castNonNull(
            table.readTinyInt(DavisBaseColumnsTableColumn.ORDINAL_POSITION.getOrdinalPosition()));
      }
    }
    throw new CompileException("Column " + columnName + " does not exist within this table");
  }

  /**
   * @param tableName
   * @return table name if valid table name
   * @throws CompileException
   */
  private String getValidatedDavisBaseTableName(String tableName) throws CompileException, StorageException, IOException {
    TableFile table = context.openTableFile(CatalogTable.DAVISBASE_TABLES.getName());
    while (table.goToNextRow()) {
      if (castNonNull(table.readText(DavisBaseTablesTableColumn.TABLE_NAME.getOrdinalPosition()))
          .equalsIgnoreCase(tableName)) {
        return tableName;
      }
    }
    throw new CompileException("Table" + tableName + " does not exist within DavisBase");
  }

  /**
   * @param tableName
   * @param columnName
   * @return DataType associated with the column for given table
   * @throws CompileException
   */
  private DataType getColumnType(String tableName, String columnName) throws CompileException, StorageException, IOException {
    TableFile table = context.openTableFile(CatalogTable.DAVISBASE_COLUMNS.getName());
    while (table.goToNextRow()) {
      if (castNonNull(table.readText(DavisBaseColumnsTableColumn.COLUMN_NAME.getOrdinalPosition()))
          .equalsIgnoreCase(columnName)
          && castNonNull(
              table.readText(DavisBaseColumnsTableColumn.TABLE_NAME.getOrdinalPosition()))
                  .equalsIgnoreCase(tableName)) {
        return DataType.valueOf(castNonNull(
            table.readText(DavisBaseColumnsTableColumn.DATA_TYPE.getOrdinalPosition())));
      }
    }
    throw new CompileException("Column " + columnName + " does not exist within this table");
  }

  /**
   * @param tableName
   * @param columnIndex
   * @return column name corresponding with given column index within table
   * @throws CompileException
   * @throws StorageException
   * @throws IOException
   */
  private String getColumnName(String tableName, int columnIndex) throws CompileException, StorageException, IOException {
    TableFile table = context.openTableFile(CatalogTable.DAVISBASE_COLUMNS.getName());
    while (table.goToNextRow()) {
      if (castNonNull(table.readTinyInt(
          DavisBaseColumnsTableColumn.ORDINAL_POSITION.getOrdinalPosition())) == columnIndex
          && castNonNull(
              table.readText(DavisBaseColumnsTableColumn.TABLE_NAME.getOrdinalPosition()))
                  .equalsIgnoreCase(tableName)) {
        return (castNonNull(
            table.readText(DavisBaseColumnsTableColumn.COLUMN_NAME.getOrdinalPosition())));
      }
    }
    throw new CompileException(
        "Column with index " + columnIndex + " does not exist within this table");
  }

  /**
   * @param tableName
   * @return whether or not the table exists within DavisBase
   * @throws StorageException
   * @throws IOException
   */
  private boolean isExistingTable(String tableName) throws StorageException, IOException {
    TableFile table = context.openTableFile(CatalogTable.DAVISBASE_TABLES.getName());
    while (table.goToNextRow()) {
      if (castNonNull(table.readText(DavisBaseTablesTableColumn.TABLE_NAME.getOrdinalPosition()))
          .equalsIgnoreCase(tableName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Validates that the table exists
   * @param tableName
   * @throws CompileException
   * @throws IOException
   * @throws StorageException
   */
  private void checkTableExists(String tableName)throws CompileException, IOException, StorageException{
    if(!isExistingTable(tableName)){
      throw new CompileException("Table " + tableName + " does not exist within DavisBase");
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
  private boolean isNullValue(String tableName, String columnName, Expression value) throws CompileException, StorageException, IOException {
    if (isColumnNullable(tableName, columnName) && value instanceof NullValue) {
      return true;
    }
    if (!isColumnNullable(tableName, columnName) && value instanceof NullValue) {
      throw new CompileException("Column " + columnName + " is not nullable");
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
  private boolean isColumnNullable(String tableName, String columnName) throws CompileException, StorageException, IOException {
    TableFile table = context.openTableFile(CatalogTable.DAVISBASE_COLUMNS.getName());
    while (table.goToNextRow()) {
      if (castNonNull(table.readText(DavisBaseColumnsTableColumn.COLUMN_NAME.getOrdinalPosition()))
          .equalsIgnoreCase(columnName)
          && castNonNull(
              table.readText(DavisBaseColumnsTableColumn.TABLE_NAME.getOrdinalPosition()))
                  .equalsIgnoreCase(tableName)) {
        return BooleanUtils.fromText(castNonNull(
            table.readText(DavisBaseColumnsTableColumn.IS_NULLABLE.getOrdinalPosition())));
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
  private List<SelectCommandColumn> getAllColumns(String tableName) throws StorageException, IOException {
    List<SelectCommandColumn> selectColumns = new ArrayList<>();
    TableFile table = context.openTableFile(CatalogTable.DAVISBASE_COLUMNS.getName());
    while (table.goToNextRow()) {
      if (castNonNull(table.readText(DavisBaseColumnsTableColumn.TABLE_NAME.getOrdinalPosition()))
          .equalsIgnoreCase(tableName)) {
        SelectCommandColumn select = new SelectCommandColumn(
            castNonNull(table
                .readTinyInt(DavisBaseColumnsTableColumn.ORDINAL_POSITION.getOrdinalPosition())),
            castNonNull(
                table.readText(DavisBaseColumnsTableColumn.COLUMN_NAME.getOrdinalPosition())),
            DataType.valueOf(castNonNull(
                table.readText(DavisBaseColumnsTableColumn.DATA_TYPE.getOrdinalPosition()))));
        selectColumns.add(select);
      }
    }
    return selectColumns;
  }

  /**
   * Validate the number of values the user is trying to insert matches the amount of columns defined in the schema for the given table
   * @param tableName
   * @param size
   * @throws IOException
   * @throws StorageException
   * @throws CompileException
   */
  private void checkInsertValuesMatchesCountColumns(String tableName, int size) throws IOException, StorageException, CompileException {
    int actualColumns = 0;
    TableFile table = context.openTableFile(CatalogTable.DAVISBASE_COLUMNS.getName());
    while (table.goToNextRow()) {
      assert DavisBaseColumnsTableColumn.ROWID != null : "The table 'davisbase_columns' should include a listing for the 'rowid' column for each table";
      if (castNonNull(table.readText(DavisBaseColumnsTableColumn.TABLE_NAME.getOrdinalPosition()))
          .equalsIgnoreCase(tableName)) {
        actualColumns++;
      }
    }
    actualColumns -= 1;  // subtract 1 for rowid
    if (size < actualColumns) {
      throw new CompileException("Davisbase does not support default column values. Expected " + actualColumns +  " columns. Please insert a value for every column");
    }
    else if (size > actualColumns) {
      throw new CompileException("The amount of columns you are trying to insert are greater than the columns defined for table: " + tableName + ". Expected " + actualColumns +  " columns.");
    }
    else {
      return;
    }
  }

  /**
   * @param tableName name of table
   * @param where WhereExpression representation of where clause
   * @return compiled CommandWhere of where clause
   * @throws IOException
   * @throws StorageException
   * @throws CompileException
   */
  private @Nullable CommandWhere compileCommandWhere(String tableName,
      @Nullable WhereExpression where) throws IOException, StorageException, CompileException {
    if (null == where) {
      return null;
    }
    byte columnIndex = getColumnIndex(tableName,where.getColumn().getColumnName());
    String columnName = getColumnName(tableName, columnIndex);
    CommandWhereColumn leftColumnReference = new CommandWhereColumn(
        columnIndex,
        columnName,
        getColumnType(tableName, columnName),
        isColumnNullable(tableName, columnName),
        false  // TODO: COME BACK AND FIX THIS ONCE INDEX IMPLEMENTED
    );
    return new CommandWhere(
        leftColumnReference,
        returnCommandOperator(where.getOperator(), where.isNot()),
        getValidObjectMatchingSchema(tableName, where.getValue(), columnName));
  }

  /**
   * @param op WhereExpression Operator enum
   * @return the CommandWhere Operator Enum given the WhereExpression Operator
   * @throws CompileException
   */
  private CommandWhere.Operator returnCommandOperator(WhereExpression.Operator op, boolean isNot) throws CompileException {
    switch (op) {
      case EQUALSTO:
        if (isNot) {
          return CommandWhere.Operator.NOT_EQUAL;
        }
        else {
          return CommandWhere.Operator.EQUAL;
        }
      case NOTEQUALTO:
        if (isNot) {
          return CommandWhere.Operator.EQUAL;
        }
        else {
          return CommandWhere.Operator.NOT_EQUAL;
        }
      case GREATERTHAN:
        if (isNot) {
          return CommandWhere.Operator.LESS_THAN_OR_EQUAL;
        }
        else {
          return CommandWhere.Operator.GREATER_THAN;
        }
      case GREATERTHANEQUALS:
        if (isNot) {
          return CommandWhere.Operator.LESS_THAN;
        }
        else {
          return CommandWhere.Operator.GREATER_THAN_OR_EQUAL;
        }
      case LESSTHAN:
        if (isNot) {
          return CommandWhere.Operator.GREATER_THAN_OR_EQUAL;
        }
        else {
          return CommandWhere.Operator.LESS_THAN;
        }
      case LESSTHANEQUALS:
        if (isNot) {
          return CommandWhere.Operator.GREATER_THAN;
        }
        else {
          return CommandWhere.Operator.LESS_THAN_OR_EQUAL;
        }
      default:
        throw new CompileException("Unrecognized operator");
    }
  }

  /**
   * Validate that the table name the command is trying to modify is not one of the catalog tables
   * @param tableName name  of table to check
   * @throws CompileException
   */
  private void checkNotCatalogTable(String tableName) throws CompileException {
    if (tableName.equalsIgnoreCase(CatalogTable.DAVISBASE_COLUMNS.getName())
        || tableName.equalsIgnoreCase(CatalogTable.DAVISBASE_TABLES.getName())) {
      throw new CompileException("Unable to modify catalog tables");
    }
  }

  /**
   * Throws CompileException if column constraint is unique but trying to insert non unique value
   * @param tableName name of table
   * @param columnName name of column with constraint
   * @param value value trying to insert
   * @throws StorageException
   * @throws IOException
   * @throws CompileException
   */
  @SuppressWarnings("nullness")
  private void checkUniqueness(String tableName, String columnName, Object value)throws StorageException, IOException, CompileException{
    //TODO: Add index logic
    if(isColumnUnique(tableName, columnName)){
      byte colIndex = getColumnIndex(tableName, columnName);
      DataType colType = getColumnType(tableName, columnName);
      TableFile table = context.openTableFile(tableName);
      final String UNIQUENESS_EXCEPTION = "Invalid insert. Column " + columnName + " has uniqueness constraint";
      while(table.goToNextRow()){
        if(colType==DataType.TINYINT && Objects.equals(table.readTinyInt(colIndex), value)){
          throw new CompileException(UNIQUENESS_EXCEPTION);
        }
        else if(colType==DataType.SMALLINT && Objects.equals(table.readSmallInt(colIndex), value)){
          throw new CompileException(UNIQUENESS_EXCEPTION);
        }
        else if(colType==DataType.INT && Objects.equals(table.readInt(colIndex), value)){
          throw new CompileException(UNIQUENESS_EXCEPTION);
        }
        else if(colType==DataType.BIGINT && Objects.equals(table.readBigInt(colIndex), value)){
          throw new CompileException(UNIQUENESS_EXCEPTION);
        }
        else if(colType==DataType.FLOAT && Objects.equals(table.readFloat(colIndex), value)){
          throw new CompileException(UNIQUENESS_EXCEPTION);
        }
        else if(colType==DataType.DOUBLE && Objects.equals(table.readDouble(colIndex), value)){
          throw new CompileException(UNIQUENESS_EXCEPTION);
        }
        else if(colType==DataType.YEAR && Objects.equals(table.readYear(colIndex), value)){
          throw new CompileException(UNIQUENESS_EXCEPTION);
        }
        else if(colType==DataType.TIME && Objects.equals(table.readTime(colIndex), value)){
          throw new CompileException(UNIQUENESS_EXCEPTION);
        }
        else if(colType==DataType.DATETIME && Objects.equals(table.readDateTime(colIndex), value)){
          throw new CompileException(UNIQUENESS_EXCEPTION);
        }
        else if(colType==DataType.DATE && Objects.equals(table.readDate(colIndex), value)){
          throw new CompileException(UNIQUENESS_EXCEPTION);
        }
        else if(colType==DataType.TEXT && castNonNull(table.readText(colIndex)).equalsIgnoreCase(value.toString())){
          throw new CompileException(UNIQUENESS_EXCEPTION);
        }
      }
    }
    else{
      return;
    }
  }

  /**
   * Checks DAVISBASE_COLUMNS table to see if given column has UNIQUE constraint
   * @param tableName table name to check
   * @param columnName column name within given table
   * @return whether given column has UNIQUE constraint
   * @throws IOException
   * @throws StorageException
   */
  private boolean isColumnUnique(String tableName, String columnName)throws IOException, StorageException {
    TableFile table = context.openTableFile(CatalogTable.DAVISBASE_COLUMNS.getName());
    while (table.goToNextRow()) {
      if (castNonNull(table.readText(DavisBaseColumnsTableColumn.COLUMN_NAME.getOrdinalPosition()))
        .equalsIgnoreCase(columnName)
        && castNonNull(
        table.readText(DavisBaseColumnsTableColumn.TABLE_NAME.getOrdinalPosition()))
        .equalsIgnoreCase(tableName)) {
        return BooleanUtils.fromText(castNonNull(
          table.readText(DavisBaseColumnsTableColumn.IS_UNIQUE.getOrdinalPosition())));
      }
    }
    throw new IllegalStateException();
  }

}
