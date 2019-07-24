package edu.utdallas.davisbase.executor;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

import edu.utdallas.davisbase.NotImplementedException;
import edu.utdallas.davisbase.command.Command;
import edu.utdallas.davisbase.command.CreateIndexCommand;
import edu.utdallas.davisbase.command.CreateTableCommand;
import edu.utdallas.davisbase.command.DeleteCommand;
import edu.utdallas.davisbase.command.DropTableCommand;
import edu.utdallas.davisbase.command.ExitCommand;
import edu.utdallas.davisbase.command.InsertCommand;
import edu.utdallas.davisbase.command.SelectCommand;
import edu.utdallas.davisbase.command.SelectCommandColumn;
import edu.utdallas.davisbase.command.ShowTablesCommand;
import edu.utdallas.davisbase.command.UpdateCommand;
import edu.utdallas.davisbase.result.CreateIndexResult;
import edu.utdallas.davisbase.result.CreateTableResult;
import edu.utdallas.davisbase.result.DeleteResult;
import edu.utdallas.davisbase.result.DropTableResult;
import edu.utdallas.davisbase.result.ExitResult;
import edu.utdallas.davisbase.result.InsertResult;
import edu.utdallas.davisbase.result.Result;
import edu.utdallas.davisbase.result.SelectResult;
import edu.utdallas.davisbase.result.SelectResultData;
import edu.utdallas.davisbase.result.SelectResultDataRow;
import edu.utdallas.davisbase.result.SelectResultSchema;
import edu.utdallas.davisbase.result.SelectResultSchemaColumn;
import edu.utdallas.davisbase.result.ShowTablesResult;
import edu.utdallas.davisbase.result.UpdateResult;
import edu.utdallas.davisbase.storage.Storage;
import edu.utdallas.davisbase.storage.StorageException;
import edu.utdallas.davisbase.storage.TableFile;

/**
 * An executor of {@link Command}s against a {@link Storage} context, and thereby a producer of
 * {@link Result}s.
 */
public class Executor {

  protected final ExecutorConfiguration configuration;

  public Executor(ExecutorConfiguration configuration) {
    checkNotNull(configuration, "configuration");

    this.configuration = configuration;
  }

  public Result execute(Command command, Storage context) throws ExecuteException, StorageException, IOException {
    checkNotNull(command, "command");
    checkNotNull(context, "context");

    Result result;
    if (command instanceof CreateIndexCommand) {
      result = execute((CreateIndexCommand) command, context);
    }
    else if (command instanceof CreateTableCommand) {
      result = execute((CreateTableCommand) command, context);
    }
    else if (command instanceof DeleteCommand) {
      result = execute((DeleteCommand) command, context);
    }
    else if (command instanceof DropTableCommand) {
      result = execute((DropTableCommand) command, context);
    }
    else if (command instanceof ExitCommand) {
      result = execute((ExitCommand) command, context);
    }
    else if (command instanceof InsertCommand) {
      result = execute((InsertCommand) command, context);
    }
    else if (command instanceof SelectCommand) {
      result = executeSelectCommand((SelectCommand) command, context);
    }
    else if (command instanceof ShowTablesCommand) {
      result = execute((ShowTablesCommand) command, context);
    }
    else if (command instanceof UpdateCommand) {
      result = execute((UpdateCommand) command, context);
    }
    else {
      throw new ExecuteException(format("Unimplemented command type: %s", command.getClass().getName()));
    }
    return result;
  }

  protected CreateIndexResult execute(CreateIndexCommand command, Storage context) throws ExecuteException, StorageException {
    assert command != null : "command should not be null";
    assert context != null : "context should not be null";

    // COMBAK Implement Executor.execute(CreateIndexCommand, Storage)
    throw new NotImplementedException();
  }

  protected CreateTableResult execute(CreateTableCommand command, Storage context) throws ExecuteException, StorageException {
    assert command != null : "command should not be null";
    assert context != null : "context should not be null";

    // TODO Implement Executor.execute(CreateTableCommand, Storage)
    throw new NotImplementedException();
  }

  protected DeleteResult execute(DeleteCommand command, Storage context) throws ExecuteException, StorageException {
    assert command != null : "command should not be null";
    assert context != null : "context should not be null";

    // COMBAK Implement Executor.execute(DeleteCommand, Storage)
    throw new NotImplementedException();
  }

  protected DropTableResult execute(DropTableCommand command, Storage context) throws ExecuteException, StorageException {
    assert command != null : "command should not be null";
    assert context != null : "context should not be null";

    // COMBAK Implement Executor.execute(DropTableCommand, Storage)
    throw new NotImplementedException();
  }

  protected ExitResult execute(ExitCommand command, Storage context) throws ExecuteException, StorageException {
    assert command != null : "command should not be null";
    assert context != null : "context should not be null";

    return new ExitResult();
  }

  protected InsertResult execute(InsertCommand command, Storage context) throws ExecuteException, StorageException {
    assert command != null : "command should not be null";
    assert context != null : "context should not be null";

    // TODO Implement Executor.execute(InsertCommand, Storage)
    throw new NotImplementedException();
  }

  protected SelectResult executeSelectCommand(SelectCommand command, Storage context) throws ExecuteException, StorageException, IOException {
    assert command != null : "command should not be null";
    assert context != null : "context should not be null";

    final String tableName = command.getTableName();
    final List<SelectCommandColumn> columns = command.getSelectClauseColumns();
    final int columnCount = columns.size();

    final SelectResultData.Builder dataBuilder = new SelectResultData.Builder();
    try (TableFile tableFile = context.openTableFile(tableName)) {
      while (tableFile.goToNextRow()) {

        final SelectResultDataRow.Builder dataRowBuilder = new SelectResultDataRow.Builder(columnCount);
        for (SelectCommandColumn column : columns) {
          final byte columnIndex = column.getIndex();

          switch (column.getDataType()) {
            case TINYINT:
              final @Nullable Byte maybeTinyInt = tableFile.readTinyInt(columnIndex);
              dataRowBuilder.addTinyInt(maybeTinyInt);
              break;

            case SMALLINT:
              final @Nullable Short maybeSmallInt = tableFile.readSmallInt(columnIndex);
              dataRowBuilder.addSmallInt(maybeSmallInt);
              break;

            case INT:
              final @Nullable Integer maybeInt = tableFile.readInt(columnIndex);
              dataRowBuilder.addInt(maybeInt);
              break;

            case BIGINT:
              final @Nullable Long maybeBigInt = tableFile.readBigInt(columnIndex);
              dataRowBuilder.addBigInt(maybeBigInt);
              break;

            case FLOAT:
              final @Nullable Float maybeFloat = tableFile.readFloat(columnIndex);
              dataRowBuilder.addFloat(maybeFloat);
              break;

            case DOUBLE:
              final @Nullable Double maybeDouble = tableFile.readDouble(columnIndex);
              dataRowBuilder.addDouble(maybeDouble);
              break;

            case YEAR:
              final @Nullable Year maybeYear = tableFile.readYear(columnIndex);
              dataRowBuilder.addYear(maybeYear);
              break;

            case TIME:
              final @Nullable LocalTime maybeTime = tableFile.readTime(columnIndex);
              dataRowBuilder.addTime(maybeTime);
              break;

            case DATETIME:
              final @Nullable LocalDateTime maybeDateTime = tableFile.readDateTime(columnIndex);
              dataRowBuilder.addDateTime(maybeDateTime);
              break;

            case DATE:
              final @Nullable LocalDate maybeDate = tableFile.readDate(columnIndex);
              dataRowBuilder.addDate(maybeDate);
              break;

            case TEXT:
              final @Nullable String maybeText = tableFile.readText(columnIndex);
              dataRowBuilder.addText(maybeText);
              break;

            default:
              throw new NotImplementedException(format(
                  "Execution of a SelectCommand over a SelectCommandColumn of DataType %s",
                      column.getDataType()));
          }
        }

        SelectResultDataRow dataRow = dataRowBuilder.build();
        dataBuilder.writeRow(dataRow);
      }
    }

    final SelectResultData data = dataBuilder.build();
    final SelectResultSchema schema = new SelectResultSchema(
        columns.stream()
               .map(col -> new SelectResultSchemaColumn(col.getName(), col.getDataType()))
               .collect(toList()));
    final SelectResult result = new SelectResult(schema, data);
    return result;
  }

  protected ShowTablesResult execute(ShowTablesCommand command, Storage context) throws ExecuteException, StorageException {
    assert command != null : "command should not be null";
    assert context != null : "context should not be null";

    // TODO Implement Executor.execute(ShowTablesCommand, Storage)
    throw new NotImplementedException();
  }

  protected UpdateResult execute(UpdateCommand command, Storage context) throws ExecuteException, StorageException {
    assert command != null : "command should not be null";
    assert context != null : "context should not be null";

    // COMBAK Implement Executor.execute(UpdateCommand, Storage)
    throw new NotImplementedException();
  }

}
